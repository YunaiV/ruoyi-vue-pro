package com.somle.rakuten.model.polo;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import jakarta.persistence.Entity;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RakutenTokenEntity {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置自增策略
    private String clientId;
    private String serviceSecret;
    private String licenseKey;



}
