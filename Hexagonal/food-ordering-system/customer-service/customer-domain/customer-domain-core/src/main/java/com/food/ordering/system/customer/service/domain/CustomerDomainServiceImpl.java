package com.food.ordering.system.customer.service.domain;

import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
public class CustomerDomainServiceImpl implements CustomerDomainService {

    @Override
    public CustomerCreatedEvent validateAndInitiateCustomer(Customer customer) {
        log.info("customer id: {} 해당 아이디를 가진 고객은 초기화(값생성)" , customer.getId().getValue());
        return  new CustomerCreatedEvent(customer , ZonedDateTime.now(ZoneId.of("UTC")));
    }
}
