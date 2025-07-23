package com.elastic.elastic_spring;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elastic.elastic_spring.entity.Article;
import com.elastic.elastic_spring.entity.Product;
import com.elastic.elastic_spring.repository.ArticleRepository;
import com.fasterxml.jackson.core.type.TypeReference;

public class QueryAnnotationTest extends AbstractTest {
	@Autowired
	private ArticleRepository repository;

	@BeforeAll
	public void dataSetup() {
		var articles = this.readResource("articles.json", new TypeReference<List<Article>>() {

		});
		this.repository.saveAll(articles);
		Assertions.assertEquals(11, this.repository.count());
	}

	@Test
	public void searchArticles() {
		var searchHits = this.repository.search("spring seasen");
		searchHits.forEach(this.print());
		Assertions.assertEquals(4, searchHits.getTotalHits());
	}
}
