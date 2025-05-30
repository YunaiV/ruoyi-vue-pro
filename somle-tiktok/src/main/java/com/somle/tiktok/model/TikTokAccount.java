package com.somle.tiktok.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tiktok_account")
public class TikTokAccount {
    @Id
    private Long id;

    private String appKey;

    private String appSecret;

    private String refreshToken;

    private String grantType;

    private String accessToken;

    private String shopCipher;
}
