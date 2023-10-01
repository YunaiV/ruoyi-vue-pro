package cn.iocoder.yudao.module.infra.service.job;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.JobLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.JobLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.JobLogDO;
import cn.iocoder.yudao.module.infra.dal.mysql.job.JobLogMapper;
import cn.iocoder.yudao.module.infra.enums.job.JobLogStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Job 日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class JobLogServiceImpl implements JobLogService {

    private static final Integer DELETE_LIMIT = 1;

    @Resource
    private JobLogMapper jobLogMapper;

    @Override
    public Long createJobLog(Long jobId, LocalDateTime beginTime, String jobHandlerName, String jobHandlerParam, Integer executeIndex) {
        JobLogDO log = JobLogDO.builder().jobId(jobId).handlerName(jobHandlerName).handlerParam(jobHandlerParam).executeIndex(executeIndex)
                .beginTime(beginTime).status(JobLogStatusEnum.RUNNING.getStatus()).build();
        jobLogMapper.insert(log);
        return log.getId();
    }

    @Override
    @Async
    public void updateJobLogResultAsync(Long logId, LocalDateTime endTime, Integer duration, boolean success, String result) {
        try {
            JobLogDO updateObj = JobLogDO.builder().id(logId).endTime(endTime).duration(duration)
                    .status(success ? JobLogStatusEnum.SUCCESS.getStatus() : JobLogStatusEnum.FAILURE.getStatus()).result(result).build();
            jobLogMapper.updateById(updateObj);
        } catch (Exception ex) {
            log.error("[updateJobLogResultAsync][logId({}) endTime({}) duration({}) success({}) result({})]",
                    logId, endTime, duration, success, result);
        }
    }

    // TODO @j-sentinel：这个 job，也可以忽略租户哈；可以直接使用  @TenantIgnore 注解；
    @Override
    public Integer timingJobCleanLog(Integer jobCleanRetainDay) {
        Integer result = null;
        int count = 0;
        // TODO @j-sentinel：一般我们在写逻辑时，尽量避免用 while true 这种“死循环”，而是  for (int i = 0; i < Short.MAX) 类似这种；避免里面真的发生一些意外的情况，无限执行；
        // 然后 for 里面，可以有个 if count < 100 未到达删除的预期条数，说明已经到底，可以 break 了；
        while (result == null || DELETE_LIMIT.equals(result)){
            result = jobLogMapper.timingJobCleanLog(jobCleanRetainDay);
            count += result;
        }
        if(count > 0){
            // ALTER TABLE...FORCE 会导致表重建发生,这会根据主键索引对表空间中的物理页进行排序。
            // 它将行压缩到页面上并消除可用空间，同时确保数据处于主键查找的最佳顺序。
            jobLogMapper.optimizeTable();
        }
        return count;
    }

    @Override
    public JobLogDO getJobLog(Long id) {
        return jobLogMapper.selectById(id);
    }

    @Override
    public List<JobLogDO> getJobLogList(Collection<Long> ids) {
        return jobLogMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JobLogDO> getJobLogPage(JobLogPageReqVO pageReqVO) {
        return jobLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<JobLogDO> getJobLogList(JobLogExportReqVO exportReqVO) {
        return jobLogMapper.selectList(exportReqVO);
    }

    // TODO @小吉祥：每天 0 点的时候，清理超过 14 天的日志；
}
