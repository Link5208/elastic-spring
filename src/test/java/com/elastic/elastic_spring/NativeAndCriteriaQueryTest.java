package com.elastic.elastic_spring;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import com.elastic.elastic_spring.entity.Garment;
import com.elastic.elastic_spring.repository.GarmentRepository;
import com.fasterxml.jackson.core.type.TypeReference;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

public class NativeAndCriteriaQueryTest extends AbstractTest {
	@Autowired
	private GarmentRepository repository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	@BeforeAll
	public void dataSetup() {
		var garments = this.readResource("garments.json", new TypeReference<List<Garment>>() {

		});
		this.repository.saveAll(garments);
		Assertions.assertEquals(20, this.repository.count());
	}

	@Test
	public void criteriaQuery() {
		var nameIsShirt = Criteria.where("name").is("shirt");
		verify(nameIsShirt, 1);
		var priceAbove100 = Criteria.where("price").greaterThan(100);
		verify(priceAbove100, 5);

		verify(nameIsShirt.or(priceAbove100), 6);

		var brandIsZara = Criteria.where("brand").is("Zara");
		verify(priceAbove100.and(brandIsZara.not()), 3);

		var fuzzyMatchShort = Criteria.where("name").fuzzy("short");
		verify(fuzzyMatchShort, 1);
	}

	@Test
	public void boolQuery() {
		var occasionCasual = Query.of(b -> b.term(
				TermQuery.of(tb -> tb.field("occasion").value("Casual"))));
		var colorBrown = Query.of(b -> b.term(
				TermQuery.of(tb -> tb.field("color").value("Brown"))));

		var priceBelow50 = Query.of(b -> b.range(
				RangeQuery.of(rb -> rb.number(
						NumberRangeQuery.of(nrb -> nrb.field("price").lte(50d))))));
		var query = Query.of(b -> b.bool(BoolQuery.of(bb -> bb.filter(occasionCasual, priceBelow50).should(colorBrown))));
		var nativeQuery = NativeQuery.builder().withQuery(query).build();

		var searchHits = this.elasticsearchOperations.search(nativeQuery, Garment.class);
		searchHits.forEach(this.print());
		Assertions.assertEquals(4, searchHits.getTotalHits());
	}

	/*
	 * {
	 * "query": {
	 * "bool": {
	 * "filter": [
	 * {
	 * "term": {
	 * "occasion": "Casual"
	 * }
	 * },
	 * {
	 * "range": {
	 * "price": {
	 * "lte": 50
	 * }
	 * }
	 * }
	 * ],
	 * "should": [
	 * {
	 * "term": {
	 * "color": "Brown"
	 * }
	 * }
	 * ]
	 * }
	 * }
	 * }
	 */

	private void verify(Criteria criteria, int expectedResultsCount) {
		var query = CriteriaQuery.builder(criteria).build();
		var searchHits = this.elasticsearchOperations.search(query, Garment.class);
		searchHits.forEach(this.print());
		Assertions.assertEquals(expectedResultsCount, searchHits.getTotalHits());

	}
}
