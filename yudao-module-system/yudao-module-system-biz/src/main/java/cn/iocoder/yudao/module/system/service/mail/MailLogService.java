package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;

import java.util.List;

/**
 * 邮箱日志服务类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailLogService {

    /**
     * 邮箱日志分页
     *
     * @param pageVO 分页参数
     * @return 分页结果
     */
    PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO);

    /**
     * 邮箱日志数组信息
     *
     * @param exportReqVO 导出筛选请求
     * @return 导出的日志数据
     */
    List<MailLogDO> getMailLogList(MailLogExportReqVO exportReqVO);

    /**
     * 创建邮箱日志
     *
     * @param mailAccountDO 邮箱账号信息
     * @param template      模版信息
     * @param from          邮箱
     * @param content       内容
     * @param tos           收件人
     * @param title         标题
     * @param isSend        是否发送成功
     */
    Long createMailLog(MailAccountDO mailAccountDO, MailTemplateDO template, String from, String content, List<String> tos, String title, Boolean isSend);

    /**
     * 更新邮件发送结果
     *
     * @param logId  发送日志Id
     * @param result 发送结果 默认返回messageId
     */
    void updateMailSendResult(Long logId, String result);

}
