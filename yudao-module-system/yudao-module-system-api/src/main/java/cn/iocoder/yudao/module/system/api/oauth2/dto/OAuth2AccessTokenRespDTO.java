package cn.iocoder.yudao.module.system.api.oauth2.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OAuth2.0 访问令牌的信息 Response DTO
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class OAuth2AccessTokenRespDTO implements Serializable {

    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 刷新令牌
     */
    private String refreshToken;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 过期时间
     */
    private LocalDateTime expiresTime;

}
