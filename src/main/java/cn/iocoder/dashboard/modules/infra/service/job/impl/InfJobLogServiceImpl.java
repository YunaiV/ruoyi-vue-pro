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
    public Long createJobLog(Long jobId, Date beginTime, String jobHandlerName, String jobHandlerParam, Integer executeIndex) {
        InfJobLogDO log = InfJobLogDO.builder().jobId(jobId).handlerName(jobHandlerName).handlerParam(jobHandlerParam).executeIndex(executeIndex)
                .beginTime(beginTime).status(InfJobLogStatusEnum.RUNNING.getStatus()).build();
        jobLogMapper.insert(log);
        return log.getId();
    }

    @Override
    @Async
    public void updateJobLogResultAsync(Long logId, Date endTime, Integer duration, boolean success, String result) {
        try {
            InfJobLogDO updateObj = InfJobLogDO.builder().id(logId).endTime(endTime).duration(duration)
                    .status(success ? InfJobLogStatusEnum.SUCCESS.getStatus() : InfJobLogStatusEnum.FAILURE.getStatus()).result(result).build();
            jobLogMapper.updateById(updateObj);
        } catch (Exception ex) {
            log.error("[updateJobLogResultAsync][logId({}) endTime({}) duration({}) success({}) result({})]",
                    logId, endTime, duration, success, result);
        }
    }

}
