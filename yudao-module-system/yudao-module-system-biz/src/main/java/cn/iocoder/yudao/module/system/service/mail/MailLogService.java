package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;

import java.util.Map;

/**
 * 邮件日志 Service 接口
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailLogService {

    /**
     * 邮件日志分页
     *
     * @param pageVO 分页参数
     * @return 分页结果
     */
    PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO);

    /**
     * 获得指定编号的邮件日志
     *
     * @param id 日志编号
     * @return 邮件日志
     */
    MailLogDO getMailLog(Long id);

    /**
     * 创建邮件日志
     *
     * @param userId 用户编码
     * @param userType 用户类型
     * @param toMail 收件人邮件
     * @param account 邮件账号信息
     * @param template      模版信息
     * @param templateContent 模版内容
     * @param templateParams 模版参数
     * @param isSend        是否发送成功
     * @return 日志编号
     */
    Long createMailLog(Long userId, Integer userType, String toMail,
                       MailAccountDO account, MailTemplateDO template ,
                       String templateContent, Map<String, Object> templateParams, Boolean isSend);

    /**
     * 更新邮件发送结果
     *
     * @param logId  日志编号
     * @param messageId 发送后的消息编号
     * @param exception 发送异常
     */
    void updateMailSendResult(Long logId, String messageId, Exception exception);

}
