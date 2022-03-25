package cn.iocoder.yudao.module.system.service.mail;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailLogService {

    PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO);

    List<MailLogDO> getMailLogList(MailLogExportReqVO exportReqVO);
}
