//package com.food.ordering.system.order.service.domain;
//
//import com.food.ordering.system.order.service.domain.ports.outport.repository.CustomerRepository;
//import com.food.ordering.system.order.service.domain.ports.outport.repository.OrderRepository;
//import com.food.ordering.system.order.service.domain.ports.outport.repository.RestaurantRepository;
//import org.mockito.Mockito;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//
//@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
//public class OrderTestConfiguration {
//    @Bean
//    public OrderRepository orderRepository() {
//        return Mockito.mock(OrderRepository.class);
//    }
//
//    @Bean
//    public CustomerRepository customerRepository() {
//        return Mockito.mock(CustomerRepository.class);
//    }
//
//    @Bean
//    public RestaurantRepository restaurantRepository() {
//        return Mockito.mock(RestaurantRepository.class);
//    }
//    @Bean
//    public OrderDomainService orderDomainService() {
//        return new OrderDomainServiceImpl();
//    }
//
//}
