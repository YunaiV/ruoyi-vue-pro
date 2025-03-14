package com.somle.cdiscount.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CdiscountToken {

    @Id
    private String clientId;

    private String clientSecret;

    private String grantType;
    //    卖家id
    private String sellerId;
    @Column(columnDefinition="LONGTEXT")
    private String accessToken;
}
