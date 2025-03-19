package com.somle.amazon.model;


import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sellerId;

    private String regionCode;

    private String clientId;

    @Column(columnDefinition="TEXT")
    private String accessToken;

    @Column(columnDefinition="TEXT")
    private String refreshToken;
}
