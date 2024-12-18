package com.food.ordering.system.order.service.domain.ports.outport.repository;

import com.food.ordering.system.order.service.domain.entity.Customer;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


public interface CustomerRepository {

    Optional<Customer> findCustomer(UUID customerId);

    Customer save(Customer customer);
}