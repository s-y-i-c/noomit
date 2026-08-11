package com.noomit.backend.reception.application;

import com.noomit.backend.customer.UpsertCustomerCommand;

public record UpdateServiceRequestCommand(
        long id,
        String customerName,
        String customerPhoneNumber,
        String customerZipCode,
        String customerAddress,
        String customerDetailAddress,
        String customerMemo,
        Long productId,
        Long selectedSubCategoryId,
        String selectedModelName,
        String symptom,
        String remarks,
        long version) {

    public UpsertCustomerCommand toUpsertCustomerCommand() {
        return new UpsertCustomerCommand(customerName, customerPhoneNumber, customerZipCode,
                customerAddress, customerDetailAddress, customerMemo);
    }
}