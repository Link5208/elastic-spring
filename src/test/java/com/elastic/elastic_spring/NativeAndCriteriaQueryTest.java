package com.elastic.elastic_spring;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;

import com.elastic.elastic_spring.entity.Garment;
import com.elastic.elastic_spring.entity.Product;
import com.elastic.elastic_spring.repository.GarmentRepository;
import com.fasterxml.jackson.core.type.TypeReference;

public class NativeAndCriteriaQueryTest extends AbstractTest {
	@Autowired
	private GarmentRepository repository;

	@BeforeAll
	public void dataSetup() {
		var garments = this.readResource("garments.json", new TypeReference<List<Garment>>() {

		});
		this.repository.saveAll(garments);
		Assertions.assertEquals(20, this.repository.count());
	}
}
