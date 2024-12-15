package com.food.ordering.system.order.service.domain.ports.outport.repository;

import com.food.ordering.system.order.service.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.Optional;


public interface RestaurantRepository {

   Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
