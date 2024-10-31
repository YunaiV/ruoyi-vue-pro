package com.somle.microsoft.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class PowerbiAccount {
    @Id
    private String username;
    private String password;
}
