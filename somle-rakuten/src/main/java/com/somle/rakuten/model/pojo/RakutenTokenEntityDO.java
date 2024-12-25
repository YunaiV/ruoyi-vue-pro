package com.somle.rakuten.model.pojo;

import jakarta.persistence.*;
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
@Table(name = "rakuten_token_entity")
public class RakutenTokenEntityDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置自增策略
    private Integer clientId;
    private String serviceSecret;
    private String licenseKey;

    //过期时间
    private Date expires;

}
