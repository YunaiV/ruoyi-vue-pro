package cn.iocoder.yudao.module.system.api.mail.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.List;
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
     *
     * 如果非空，则加载对应用户的邮箱，添加到 {@link #toMails} 中
     */
    private Long userId;

    /**
     * 收件邮箱
     */
    private List<@Email String> toMails;
    /**
     * 抄送邮箱
     */
    private List<@Email String> ccMails;
    /**
     * 密送邮箱
     */
    private List<@Email String> bccMails;


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
