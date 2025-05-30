package com.somle.lazada.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LazadaAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String appKey;
    private String appSecret;
    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    @Column(columnDefinition = "TEXT")
    private String accessToken;
}
