package com.elastic.elastic_spring;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;

import com.elastic.elastic_spring.entity.Product;
import com.elastic.elastic_spring.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;

public class QueryMethodsTest extends AbstractTest {
	private static final Logger log = LoggerFactory.getLogger(QueryMethodsTest.class);

	@Autowired
	private ProductRepository repository;

	@BeforeAll
	public void dataSetup() {
		var products = this.readResource("products.json", new TypeReference<List<Product>>() {

		});
		this.repository.saveAll(products);
		Assertions.assertEquals(20, this.repository.count());
	}

	@Test
	public void findByCategory() {
		var searchHits = this.repository.findByCategory("Furniture");
		searchHits.forEach(this.print());
		Assertions.assertEquals(4, searchHits.getTotalHits());
	}

	@Test
	public void findByCategories() {
		var searchHits = this.repository.findByCategoryIn(List.of("Furniture", "Beauty"));
		searchHits.forEach(this.print());
		Assertions.assertEquals(8, searchHits.getTotalHits());
	}

	@Test
	public void findByCategoryAndBrand() {
		var searchHits = this.repository.findByCategoryAndBrand("Furniture", "Ikea");
		searchHits.forEach(this.print());
		Assertions.assertEquals(2, searchHits.getTotalHits());
	}

	@Test
	public void findByName() {
		var searchHits = this.repository.findByName("Coffee table");
		searchHits.forEach(this.print());
		Assertions.assertEquals(2, searchHits.getTotalHits());
	}

	@Test
	public void findByPriceLessThan() {
		var searchHits = this.repository.findByPriceLessThan(80);
		searchHits.forEach(this.print());
		Assertions.assertEquals(5, searchHits.getTotalHits());
	}

	@Test
	public void findByPriceBetween() {
		var searchHits = this.repository.findByPriceBetween(10, 120, Sort.by("price"));
		searchHits.forEach(this.print());
		Assertions.assertEquals(8, searchHits.getTotalHits());
	}

	@Test
	public void findAllSortByQuantity() {
		var searchHits = this.repository.findAll(Sort.by("quantity").descending());
		searchHits.forEach(this.print());
		Assertions.assertEquals(8, Streamable.of(searchHits).toList().size());
	}
}
