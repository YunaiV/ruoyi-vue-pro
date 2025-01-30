package com.somle.amazon.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "amazonsp_auth")
public class AmazonSpAuthDO {

    @Id
    private Long id;

    private String sellerId;

    private String regionCode;

    private String clientId;

    @Column(columnDefinition="TEXT")
    private String accessToken;

    @Column(columnDefinition="TEXT")
    private String refreshToken;
}
