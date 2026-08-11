package com.noomit.backend.reception.application;

import com.noomit.backend.customer.UpsertCustomerCommand;

public record CreateServiceRequestCommand(
        String customerName,
        String customerPhoneNumber,
        String customerZipCode,
        String customerAddress,
        String customerDetailAddress,
        String customerMemo,
        Long productId,
        Long selectedSubCategoryId,
        String selectedModelName,
        long receptionistId,
        String symptom,
        String remarks) {

    public UpsertCustomerCommand toUpsertCustomerCommand() {
        return new UpsertCustomerCommand(customerName, customerPhoneNumber, customerZipCode,
                customerAddress, customerDetailAddress, customerMemo);
    }
}