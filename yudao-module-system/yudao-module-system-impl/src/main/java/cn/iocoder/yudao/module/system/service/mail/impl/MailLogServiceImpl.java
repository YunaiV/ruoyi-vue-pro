package cn.iocoder.yudao.module.system.service.mail.impl;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailLogMapper;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 *  邮箱日志实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
public class MailLogServiceImpl implements MailLogService {

    @Autowired
    MailLogMapper mailLogMapper;

    @Override
    public PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO) {
        return mailLogMapper.selectPage(pageVO);
    }

    @Override
    public List<MailLogDO> getMailLogList(MailLogExportReqVO exportReqVO) {
        return mailLogMapper.selectList(exportReqVO);
    }
}
