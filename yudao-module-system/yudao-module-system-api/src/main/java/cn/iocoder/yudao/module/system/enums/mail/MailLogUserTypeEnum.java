package cn.iocoder.yudao.module.system.enums.mail;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邮件日志用户类型
 *
 * @author wangjingyi
 */
@Getter
@AllArgsConstructor
public enum MailLogUserTypeEnum {

    COMMON (10),
    VIP (20);

    /**
     * 类型
     */
    private final int userType;
}
