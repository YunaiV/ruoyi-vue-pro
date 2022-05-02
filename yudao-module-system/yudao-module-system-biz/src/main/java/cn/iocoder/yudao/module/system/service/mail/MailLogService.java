package cn.iocoder.yudao.module.system.service.mail;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;

import java.util.List;

/**
 *  邮箱日志服务类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailLogService {
    /**
     * 邮箱日志分页
     * @param pageVO
     * @return
     */
    PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO);

    /**
     * 邮箱日志数组信息
     * @param exportReqVO
     * @return
     */
    List<MailLogDO> getMailLogList(MailLogExportReqVO exportReqVO);

    /**
     * 创建邮箱日志
     * @param mailAccountDO 邮箱账号信息
     * @param mailTemplateDO 模版信息
     * @param from 邮箱
     * @param content 内容
     * @param tos 收件人
     * @param title 标题
     * @param isSend 是否发送成功
     */
    Long createMailLog(MailAccountDO mailAccountDO, MailTemplateDO mailTemplateDO, String from, String content, List<String> tos, String title, Boolean isSend);

    Long updateSmsSendResult(Long logId, String result);
}
