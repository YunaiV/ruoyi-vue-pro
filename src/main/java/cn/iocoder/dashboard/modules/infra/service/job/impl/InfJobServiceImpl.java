package cn.iocoder.dashboard.modules.infra.service.job.impl;

import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobCreateReqVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobPageReqVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobUpdateReqVO;
import cn.iocoder.dashboard.modules.infra.convert.job.InfJobConvert;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.job.InfJobMapper;
import cn.iocoder.dashboard.modules.infra.service.job.InfJobService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.JOB_NOT_EXISTS;

/**
 * 定时任务 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class InfJobServiceImpl implements InfJobService {

    @Resource
    private InfJobMapper jobMapper;

    @Override
    public Long createJob(InfJobCreateReqVO createReqVO) {
        // 插入
        InfJobDO job = InfJobConvert.INSTANCE.convert(createReqVO);
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
        jobMapper.insert(job);
        // 返回
        return job.getId();
    }

    @Override
    public void updateJob(InfJobUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateJobExists(updateReqVO.getId());
        // 更新
        InfJobDO updateObj = InfJobConvert.INSTANCE.convert(updateReqVO);
        if (updateObj.getMonitorTimeout() == null) {
            updateObj.setMonitorTimeout(0);
        }
        jobMapper.updateById(updateObj);
    }

    @Override
    public void deleteJob(Long id) {
        // 校验存在
        this.validateJobExists(id);
        // 更新
        jobMapper.deleteById(id);
    }

    private void validateJobExists(Long id) {
        if (jobMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(JOB_NOT_EXISTS);
        }
    }

    @Override
    public InfJobDO getJob(Long id) {
        return jobMapper.selectById(id);
    }

    @Override
    public List<InfJobDO> getJobList(Collection<Long> ids) {
        return jobMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<InfJobDO> getJobPage(InfJobPageReqVO pageReqVO) {
		return jobMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InfJobDO> getJobList(InfJobExportReqVO exportReqVO) {
		return jobMapper.selectList(exportReqVO);
    }

}
