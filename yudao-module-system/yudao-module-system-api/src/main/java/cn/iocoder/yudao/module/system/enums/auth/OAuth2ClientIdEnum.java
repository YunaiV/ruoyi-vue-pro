package cn.iocoder.yudao.module.system.enums.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2.0 客户端的编号枚举
 */
@AllArgsConstructor
@Getter
public enum OAuth2ClientIdEnum {

    DEFAULT(1L); // 系统默认

    private final Long id;

}
