package cn.iocoder.yudao.framework.mail.core.client;

import java.util.List;

/**
 * 邮件客户端，用于对接各邮箱平台的 SDK，实现邮件发送等功能
 *
 * @author wangjingyi
 * @date 2021/4/19 19:21
 */
public interface MailClient {

    /**
     * 发送邮件
     *
     * @param from 邮箱账号
     * @param content 内容
     * @param title 标题
     * @param tos 收件人
     * @return 邮件发送结果
     */
    String sendMail(String from, String content, String title, List<String> tos);
}
