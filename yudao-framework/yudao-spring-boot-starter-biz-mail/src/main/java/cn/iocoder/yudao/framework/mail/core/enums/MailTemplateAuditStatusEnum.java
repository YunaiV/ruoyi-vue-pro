package cn.iocoder.yudao.framework.mail.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信模板的审核状态枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum MailTemplateAuditStatusEnum {

    CHECKING(1),
    SUCCESS(2),
    FAIL(3);

    private final Integer status;

}
