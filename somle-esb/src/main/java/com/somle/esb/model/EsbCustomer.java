package com.somle.esb.model;

import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class EsbCustomer {
    @Id
    private String id;
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private int birthYear;
    private String language;
}