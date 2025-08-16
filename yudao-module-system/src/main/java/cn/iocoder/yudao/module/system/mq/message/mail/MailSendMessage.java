package cn.iocoder.yudao.module.system.mq.message.mail;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * 邮箱发送消息
 *
 * @author 芋道源码
 */
@Data
public class MailSendMessage {

    /**
     * 邮件日志编号
     */
    @NotNull(message = "邮件日志编号不能为空")
    private Long logId;
    /**
     * 接收邮件地址
     */
    @NotEmpty(message = "接收邮件地址不能为空")
    private Collection<String> toMails;
    /**
     * 抄送邮件地址
     */
    private Collection<String> ccMails;
    /**
     * 密送邮件地址
     */
    private Collection<String> bccMails;
    /**
     * 邮件账号编号
     */
    @NotNull(message = "邮件账号编号不能为空")
    private Long accountId;

    /**
     * 邮件发件人
     */
    private String nickname;
    /**
     * 邮件标题
     */
    @NotEmpty(message = "邮件标题不能为空")
    private String title;
    /**
     * 邮件内容
     */
    @NotEmpty(message = "邮件内容不能为空")
    private String content;

}
