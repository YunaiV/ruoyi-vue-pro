package com.somle.eccang.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class EccangToken {
    @Id
    private String userName;
    private String userToken;
}