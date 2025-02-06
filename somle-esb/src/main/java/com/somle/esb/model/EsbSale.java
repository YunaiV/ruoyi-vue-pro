package com.somle.esb.model;

import jakarta.persistence.Id;

import lombok.Data;


@Data
public class EsbSale {
    @Id
    private String id;
    private String platformSku;
    private String platformCode;
    private EsbCustomer customer;
    private EsbAddress address;
}
