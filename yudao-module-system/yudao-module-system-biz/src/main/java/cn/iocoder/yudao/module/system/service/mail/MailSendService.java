package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;

import java.util.Map;

/**
 * 邮件发送 Service 接口
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailSendService {

    /**
     * 发送单条邮件给管理后台的用户
     *
     * @param mail 邮箱
     * @param userId 用户编码
     * @param templateCode 邮件模版编码
     * @param templateParams 邮件模版参数
     * @return 发送日志编号
     */
    Long sendSingleMailToAdmin(String mail, Long userId,
                               String templateCode, Map<String, Object> templateParams);

    /**
     * 发送单条邮件给用户 APP 的用户
     *
     * @param mail 邮箱
     * @param userId 用户编码
     * @param templateCode 邮件模版编码
     * @param templateParams 邮件模版参数
     * @return 发送日志编号
     */
    Long sendSingleMailToMember(String mail, Long userId,
                                String templateCode, Map<String, Object> templateParams);

    /**
     * 发送单条邮件给用户
     *
     * @param mail 邮箱
     * @param userId 用户编码
     * @param userType 用户类型
     * @param templateCode 邮件模版编码
     * @param templateParams 邮件模版参数
     * @return 发送日志编号
     */
    Long sendSingleMail(String mail, Long userId, Integer userType,
                        String templateCode, Map<String, Object> templateParams);

    /**
     * 执行真正的邮件发送
     * 注意，该方法仅仅提供给 MQ Consumer 使用
     *
     * @param message 邮件
     */
    void doSendMail(MailSendMessage message);

}
