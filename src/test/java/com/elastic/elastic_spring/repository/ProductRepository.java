package com.elastic.elastic_spring.repository;

import java.util.List;

import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.elastic.elastic_spring.entity.Product;

@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, Integer> {
	SearchHits<Product> findByCategory(String category);

	SearchHits<Product> findByCategoryIn(List<String> categories);
}
