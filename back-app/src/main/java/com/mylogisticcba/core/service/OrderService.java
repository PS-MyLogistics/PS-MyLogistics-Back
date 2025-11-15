package com.mylogisticcba.core.service;


import com.mylogisticcba.core.dto.req.OrderCreationRequest;
import com.mylogisticcba.core.dto.response.OrderCreatedResponse;
import com.mylogisticcba.core.dto.response.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

   OrderCreatedResponse createOrder(OrderCreationRequest orderCreationRequest);

   List<OrderResponse> getAllOrders();

   OrderResponse getOrderById(UUID id);
}
