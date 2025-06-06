package com.somle.otto.model.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 令牌响应类
 * 用于表示从 OAuth 服务返回的令牌和相关信息。
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OttoAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 访问令牌
     */
    @Column(length = 2048)
    private String accessToken;

    /**
     * 令牌有效期，单位为秒
     */
    private int expiresIn;

    /**
     * 刷新令牌的有效期，单位为秒
     */
    private int refreshExpiresIn;

    /**
     * 令牌类型（通常为 "Bearer"）
     */
    private String tokenType;

    /**
     * 令牌生效前的时间戳
     */
    private long notBeforePolicy;

    /**
     * 权限范围
     */
    private String scope;

//    @OneToOne(mappedBy = "oauthToken")
//    private OttoAccount ottoAccount;

}

