package com.somle.rakuten.model.polo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RakutenTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置自增策略
    private Integer clientId;
    private String serviceSecret;
    private String licenseKey;


}
