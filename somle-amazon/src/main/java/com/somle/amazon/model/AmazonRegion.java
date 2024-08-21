package com.somle.amazon.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmazonRegion {
    @Id
    private String code;
    private String spEndPoint;
    private String adEndPoint;

    // private String adRefreshToken;
    // private String spRefreshToken;
    // private String adAccessToken;
    // private String spAccessToken;
}