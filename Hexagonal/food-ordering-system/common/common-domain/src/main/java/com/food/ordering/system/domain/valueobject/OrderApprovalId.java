package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

public class OrderApprovalId extends BaseId<UUID>{
    public OrderApprovalId(UUID value) {
        super(value);
    }
}