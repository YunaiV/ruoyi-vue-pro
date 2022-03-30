package cn.iocoder.yudao.module.system.service.mail;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;

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
}
