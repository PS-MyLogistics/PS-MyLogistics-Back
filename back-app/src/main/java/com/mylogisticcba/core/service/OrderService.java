package com.mylogisticcba.core.service;


import com.mylogisticcba.core.dto.req.OrderCreationRequest;
import com.mylogisticcba.core.dto.response.OrderCreatedResponse;

public interface OrderService {

   OrderCreatedResponse createOrder(OrderCreationRequest orderCreationRequest);
}
