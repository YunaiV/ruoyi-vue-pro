package cn.iocoder.dashboard.modules.system.service.logger.impl;

import cn.iocoder.dashboard.modules.system.controller.logger.vo.SysOperateLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysOperateLogConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.logger.SysOperateLogMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysOperateLogDO;
import cn.iocoder.dashboard.modules.system.service.logger.SysOperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class SysOperateLogServiceImpl implements SysOperateLogService {

    @Resource
    private SysOperateLogMapper operateLogMapper;

    @Override
    @Async
    public void createOperateLogAsync(SysOperateLogCreateReqVO reqVO) {
        SysOperateLogDO logDO = SysOperateLogConvert.INSTANCE.convert(reqVO);
        try {
            operateLogMapper.insert(logDO);
        } catch (Throwable throwable) {
            // 仅仅打印日志，不对外抛出。原因是，还是要保留现场数据。
            log.error("[createOperateLogAsync][记录操作日志异常，日志为 ({})]", reqVO, throwable);
        }
    }

}
