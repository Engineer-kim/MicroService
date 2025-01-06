package com.food.ordering.system.customer.service.domain.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateCustomerResponse {
    //클라이언트에 반환할 객체
    @NotNull
    private final UUID customerId;
    @NotNull
    private final String message;
}