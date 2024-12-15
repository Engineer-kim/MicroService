package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.ports.outport.repository.CustomerRepository;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {

    @Autowired
    private CustomerRepository customerRepository;
}
