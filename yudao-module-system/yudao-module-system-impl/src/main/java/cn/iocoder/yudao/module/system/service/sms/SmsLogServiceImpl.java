package cn.iocoder.yudao.module.system.service.sms;

import cn.iocoder.yudao.module.system.controller.admin.sms.vo.log.SmsLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.log.SmsLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.mysql.sms.SysSmsLogMapper;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 短信日志 Service 实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Slf4j
@Service
public class SmsLogServiceImpl implements SmsLogService {

    @Resource
    private SysSmsLogMapper smsLogMapper;

    @Override
    public PageResult<SysSmsLogDO> getSmsLogPage(SmsLogPageReqVO pageReqVO) {
        return smsLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SysSmsLogDO> getSmsLogList(SmsLogExportReqVO exportReqVO) {
        return smsLogMapper.selectList(exportReqVO);
    }

}
