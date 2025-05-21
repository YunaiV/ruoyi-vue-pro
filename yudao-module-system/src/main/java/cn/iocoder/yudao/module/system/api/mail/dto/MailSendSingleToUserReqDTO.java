package cn.iocoder.yudao.module.system.api.mail.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 邮件发送 Request DTO
 *
 * @author wangjingqi
 */
@Data
public class MailSendSingleToUserReqDTO {

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 邮箱
     */
    @Email
    private String mail;

    /**
     * 邮件模板编号
     */
    @NotNull(message = "邮件模板编号不能为空")
    private String templateCode;
    /**
     * 邮件模板参数
     */
    private Map<String, Object> templateParams;

}
