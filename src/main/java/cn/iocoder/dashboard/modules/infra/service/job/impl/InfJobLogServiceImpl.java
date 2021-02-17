package cn.iocoder.dashboard.modules.infra.service.job.impl;

import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobLogDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.job.InfJobLogMapper;
import cn.iocoder.dashboard.modules.infra.enums.job.InfJobLogStatusEnum;
import cn.iocoder.dashboard.modules.infra.service.job.InfJobLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Job 日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class InfJobLogServiceImpl implements InfJobLogService {

    @Resource
    private InfJobLogMapper jobLogMapper;

    @Override
    public Long createJobLog(Long jobId, Date beginTime, String jobHandlerName, String jobHandlerParam) {
        InfJobLogDO log = InfJobLogDO.builder().jobId(jobId).handlerName(jobHandlerName).handlerParam(jobHandlerParam)
                .beginTime(beginTime).status(InfJobLogStatusEnum.RUNNING.getStatus()).build();
        jobLogMapper.insert(log);
        return log.getId();
    }

    @Override
    @Async
    public void updateJobLogSuccessAsync(Long logId, Date endTime, Integer duration, String result) {
        updateJobLogResult(logId, endTime, duration, result, InfJobLogStatusEnum.SUCCESS);
    }

    @Override
    @Async
    public void updateJobLogErrorAsync(Long logId, Date endTime, Integer duration, String result) {
        updateJobLogResult(logId, endTime, duration, result, InfJobLogStatusEnum.FAILURE);
    }

    private void updateJobLogResult(Long logId, Date endTime, Integer duration, String result, InfJobLogStatusEnum status) {
        try {
            InfJobLogDO updateObj = InfJobLogDO.builder().id(logId).endTime(endTime).duration(duration)
                    .status(status.getStatus()).result(result).build();
            jobLogMapper.updateById(updateObj);
        } catch (Exception ex) {
            log.error("[updateJobLogResult][logId({}) endTime({}) duration({}) result({}) status({})]",
                    logId, endTime, duration, result, status);
        }
    }

}
