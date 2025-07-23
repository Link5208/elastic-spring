package com.elastic.elastic_spring;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import com.elastic.elastic_spring.entity.Garment;
import com.elastic.elastic_spring.entity.Product;
import com.elastic.elastic_spring.repository.GarmentRepository;
import com.fasterxml.jackson.core.type.TypeReference;

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

	private void verify(Criteria criteria, int expectedResultsCount) {
		var query = CriteriaQuery.builder(criteria).build();
		var searchHits = this.elasticsearchOperations.search(query, Garment.class);
		searchHits.forEach(this.print());
		Assertions.assertEquals(expectedResultsCount, searchHits.getTotalHits());

	}
}
