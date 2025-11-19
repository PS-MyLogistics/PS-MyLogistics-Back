package com.mylogisticcba.core.service;

import com.mylogisticcba.core.dto.req.CustomerCreationRequest;
import com.mylogisticcba.core.dto.response.CustomerResponse;
import com.mylogisticcba.core.dto.response.CustomerWithLastOrderResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    List<CustomerResponse> getAllCustomers();

    CustomerResponse getCustomerById(UUID id);

    CustomerResponse createCustomer(CustomerCreationRequest request);

    CustomerResponse updateCustomer(UUID id, CustomerCreationRequest request);

    void deleteCustomer(UUID id);

    /**
     * Obtiene todos los clientes con información de su último pedido
     * @param daysSinceLastOrder Filtro de días desde el último pedido (opcional, 0 = todos)
     * @return Lista de clientes con información del último pedido
     */
    List<CustomerWithLastOrderResponse> getCustomersWithLastOrder(Integer daysSinceLastOrder);
}