package com.elastic.elastic_spring.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.elastic.elastic_spring.entity.Employee;

@Repository
public interface EmployeeRepository extends ElasticsearchRepository<Employee, Integer> {

}
