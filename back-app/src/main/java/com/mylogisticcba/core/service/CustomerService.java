package com.mylogisticcba.core.service;

import com.mylogisticcba.core.dto.req.CustomerCreationRequest;
import com.mylogisticcba.core.dto.response.CustomerResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    List<CustomerResponse> getAllCustomers();

    CustomerResponse getCustomerById(UUID id);

    CustomerResponse createCustomer(CustomerCreationRequest request);

    CustomerResponse updateCustomer(UUID id, CustomerCreationRequest request);

    void deleteCustomer(UUID id);
}