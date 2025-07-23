package com.elastic.elastic_spring;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;

import com.elastic.elastic_spring.entity.Employee;
import com.elastic.elastic_spring.repository.EmployeeRepository;

public class CrudOperationsTest extends AbstractTest {
	private static final Logger log = LoggerFactory.getLogger(CrudOperationsTest.class);

	@Autowired
	private EmployeeRepository employeeRepository;

	@Test
	public void crud() {
		var employee = this.createEmployee(1, "Sam", 30);
		this.employeeRepository.save(employee);
		this.printAll();

		this.employeeRepository.findById(1).orElseThrow();
		Assertions.assertEquals(30, employee.getAge());
		Assertions.assertEquals("Sam", employee.getName());

		employee.setAge(32);
		employee = this.employeeRepository.save(employee);
		this.printAll();
		Assertions.assertEquals(32, employee.getAge());

		this.employeeRepository.deleteById(1);
		this.printAll();
		Assertions.assertTrue(this.employeeRepository.findById(1).isEmpty());
	}

	@Test
	public void bulkCrub() {
		var list = IntStream.rangeClosed(1, 10)
				.mapToObj(i -> this.createEmployee(i, "name-" + i, 30 + i)).toList();
		this.employeeRepository.saveAll(list);
		this.printAll();

		Assertions.assertEquals(10, this.employeeRepository.count());

		var ids = List.of(2, 4, 6);
		var iterable = this.employeeRepository.findAllById(ids);
		list = Streamable.of(iterable).toList();
		Assertions.assertEquals(3, list.size());

		list.forEach(e -> e.setAge(e.getAge() + 10));
		this.employeeRepository.saveAll(list);
		this.printAll();
		this.employeeRepository.findAllById(ids)
				.forEach(e -> Assertions.assertEquals(e.getId() + 40, e.getAge()));

		this.employeeRepository.deleteAllById(ids);
		this.printAll();
		Assertions.assertEquals(7, this.employeeRepository.count());

	}

	private Employee createEmployee(int id, String name, int age) {
		Employee employee = new Employee();
		employee.setId(id);
		employee.setName(name);
		employee.setAge(age);
		return employee;
	}

	private void printAll() {
		this.employeeRepository.findAll().forEach(e -> log.info("Employee: {}", e));
	}
}
