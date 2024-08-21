package com.somle.erp.model;

import jakarta.persistence.Id;

import lombok.Data;


@Data
public class ErpSale {
    @Id
    private String id;
    private String platformSku;
    private String platformCode;
    private ErpCustomer customer;
    private ErpAddress address;
}
