package com.elastic.elastic_spring;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import com.elastic.elastic_spring.entity.Review;

public class IndexOperationsTest extends AbstractTest {
	private static final Logger log = LoggerFactory.getLogger(IndexOperationsTest.class);

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	@Test
	public void createIndex() {
		var indexOperations = this.elasticsearchOperations.indexOps(IndexCoordinates.of("albums"));
		Assertions.assertTrue(indexOperations.create());
		this.verify(indexOperations, 1, 1);
	}

	@Test
	public void createIndexWithSettings() {
		var indexOperations = this.elasticsearchOperations.indexOps(Review.class);
		Assertions.assertTrue(indexOperations.create());
		this.verify(indexOperations, 2, 2);
	}

	private void verify(IndexOperations indexOperations, int expectedShards, int expectedReplicas) {
		var settings = indexOperations.getSettings();
		log.info("settings: {}", settings);
		log.info("mapping: {}", indexOperations.getMapping());

		Assertions.assertEquals(String.valueOf(expectedShards), settings.get("index.number_of_shards"));
		Assertions.assertEquals(String.valueOf(expectedReplicas), settings.get("index.number_of_replicas"));

		// delete the index
		Assertions.assertTrue(indexOperations.delete());

	}
}
