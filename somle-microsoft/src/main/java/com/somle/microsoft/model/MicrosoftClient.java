package com.somle.microsoft.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class MicrosoftClient {
    @Id
    private String clientId;
    private String clientSecret;
}
