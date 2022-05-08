package cn.iocoder.yudao.module.system.api.auth.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * OAuth2.0 访问令牌的校验 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class OAuth2AccessTokenCheckRespDTO implements Serializable {

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 租户编号
     */
    private Long tenantId;

}
