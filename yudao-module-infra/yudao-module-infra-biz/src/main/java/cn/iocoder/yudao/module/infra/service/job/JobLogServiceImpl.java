package cn.iocoder.yudao.module.infra.service.job;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
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
import java.util.Date;
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

    @Override
    public Integer timingJobCleanLog(Integer jobLogExceedDay,Integer deleteLimit) {
        Integer result;
        int count = 0;
        Date currentDate = DateUtil.date();
        // 计算过期日期：正数向未来偏移，负数向历史偏移
        Date expireDate = DateUtil.offsetDay(currentDate, -jobLogExceedDay);
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            result = jobLogMapper.deleteByCreateTimeLt(expireDate,deleteLimit);
            count += result;
            if (result < deleteLimit) {
                // 达到删除预期条数
                break;
            }
        }
        if(count > 0){
            // ALTER TABLE...FORCE 会导致表重建发生,这会根据主键索引对表空间中的物理页进行排序。
            // 它将行压缩到页面上并消除可用空间，同时确保数据处于主键查找的最佳顺序。
            // 优化表语句官方文档：https://dev.mysql.com/doc/refman/8.0/en/optimize-table.html
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

}
