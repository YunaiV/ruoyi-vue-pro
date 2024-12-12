package com.somle.rakuten.model.pojo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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

    //过期时间
    private Date expires;

}
