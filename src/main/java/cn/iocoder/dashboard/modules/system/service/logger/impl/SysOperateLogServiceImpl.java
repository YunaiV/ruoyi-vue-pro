package cn.iocoder.dashboard.modules.system.service.logger.impl;

import cn.iocoder.dashboard.modules.system.controller.logger.vo.SysOperateLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysOperateLogConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.logger.SysOperateLogMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysOperateLogDO;
import cn.iocoder.dashboard.modules.system.service.logger.SysOperateLogService;
import cn.iocoder.dashboard.util.string.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysOperateLogDO.*;

@Service
@Slf4j
public class SysOperateLogServiceImpl implements SysOperateLogService {

    @Resource
    private SysOperateLogMapper operateLogMapper;

    @Override
    @Async
    public void createOperateLogAsync(SysOperateLogCreateReqVO reqVO) {
        try {
            SysOperateLogDO logDO = SysOperateLogConvert.INSTANCE.convert(reqVO);
            logDO.setJavaMethodArgs(StrUtils.maxLength(logDO.getJavaMethodArgs(), JAVA_METHOD_ARGS_MAX_LENGTH));
            logDO.setResultData(StrUtils.maxLength(logDO.getResultData(), RESULT_MAX_LENGTH));
            operateLogMapper.insert(logDO);
        } catch (Throwable throwable) {
            // 仅仅打印日志，不对外抛出。原因是，还是要保留现场数据。
            log.error("[createOperateLogAsync][记录操作日志异常，日志为 ({})]", reqVO, throwable);
        }
    }

}
