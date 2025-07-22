package com.elastic.elastic_spring;

import org.springframework.boot.SpringApplication;

public class TestElasticSpringApplication {

	public static void main(String[] args) {
		SpringApplication.from(ElasticSpringApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
