package com.elastic.elastic_spring;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;

import com.elastic.elastic_spring.entity.Garment;
import com.elastic.elastic_spring.repository.GarmentRepository;
import com.fasterxml.jackson.core.type.TypeReference;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationRange;
import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StatsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;

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

	/*
	 * {
	 * "size": 0,
	 * "aggs": {
	 * "price-stats": {
	 * "stats": {
	 * "field": "price"
	 * }
	 * },
	 * "group-by-brand": {
	 * "terms": {
	 * "field": "brand"
	 * }
	 * },
	 * "group-by-color": {
	 * "terms": {
	 * "field": "color"
	 * }
	 * },
	 * "price-range": {
	 * "range": {
	 * "field": "price",
	 * "ranges": [
	 * {
	 * "to": 50
	 * },
	 * {
	 * "from": 50,
	 * "to": 100
	 * },
	 * {
	 * "from": 100,
	 * "to": 150
	 * },
	 * {
	 * "from": 150
	 * }
	 * ]
	 * }
	 * }
	 * }
	 * }
	 */

	@Test
	public void aggregation() {
		var priceStats = Aggregation.of(b -> b.stats(
				StatsAggregation.of(sb -> sb.field("price"))));
		var brandTerms = Aggregation.of(b -> b.terms(
				TermsAggregation.of(tb -> tb.field("brand"))));
		var colorTerms = Aggregation.of(b -> b.terms(
				TermsAggregation.of(tb -> tb.field("color"))));
		var ranges = List.of(AggregationRange.of(b -> b.to(50d)),
				AggregationRange.of(b -> b.from(50d).to(100d)),
				AggregationRange.of(b -> b.from(100d).to(150d)),
				AggregationRange.of(b -> b.from(150d)));
		var priceRange = Aggregation.of(b -> b.range(
				RangeAggregation.of(rb -> rb.field("price").ranges(ranges))));
		var nativeQuery = NativeQuery.builder().withMaxResults(0)
				.withAggregation("price-stats", priceStats)
				.withAggregation("group-by-brand", brandTerms)
				.withAggregation("group-by-color", colorTerms)
				.withAggregation("price-range", priceRange)
				.build();
		var searchHits = this.elasticsearchOperations.search(nativeQuery, Garment.class);
		var aggregations = (List<ElasticsearchAggregation>) searchHits.getAggregations().aggregations();

		var map = aggregations.stream()
				.map(ElasticsearchAggregation::aggregation).collect(Collectors.toMap(
						a -> a.getName(),
						a -> a.getAggregate()));

		this.print().accept(map);
		Assertions.assertEquals(4, map.size());

		Assertions.assertTrue(map.get("price-stats").isStats());
		Assertions.assertTrue(map.get("price-range").isRange());
		Assertions.assertTrue(map.get("group-by-brand").isSterms());
		Assertions.assertTrue(map.get("group-by-color").isSterms());

		if (map.get("group-by-brand").isSterms()) {
			map.get("group-by-brand")
					.sterms()
					.buckets()
					.array()
					.stream()
					.map(b -> b.key().stringValue() + ":" + b.docCount())
					.forEach(this.print());
		}

	}
	/*
	 * {
	 * "suggest": {
	 * "product-suggest": {
	 * "prefix": "ca",
	 * "completion": {
	 * "field": "name.completion"
	 * }
	 * }
	 * },
	 * "_source": false
	 * }
	 */

	@Test
	public void suggestion() {
		var fieldSuggester = FieldSuggester.of(b -> b
				.prefix("ca")
				.completion(CompletionSuggester.of(csb -> csb.field("name.completion").skipDuplicates(true).size(10))));
		var suggester = Suggester.of(b -> b.suggesters("product-suggest", fieldSuggester));
		var query = NativeQuery.builder()
				.withSuggester(suggester)
				.withMaxResults(0)
				.withSourceFilter(FetchSourceFilter.of(b -> b.withExcludes("*"))).build();
		var searchHits = this.elasticsearchOperations.search(query, Garment.class);
		Assertions.assertNotNull(searchHits.getSuggest());
		var suggestions = searchHits.getSuggest().getSuggestion("product-suggest")
				.getEntries()
				.getFirst()
				.getOptions()
				.stream()
				.map(Suggest.Suggestion.Entry.Option::getText)
				.collect(Collectors.toSet());
		Assertions.assertEquals(Set.of("Casual Wrap", "Casual Maxi"), suggestions);

	}
}
