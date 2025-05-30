package com.somle.shopee.model;

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
@Table(name = "shopee_account")
public class ShopeeAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long partnerId;
    private String partnerKey;
    private Long shopId;
    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    @Column(columnDefinition = "TEXT")
    private String accessToken;
}
