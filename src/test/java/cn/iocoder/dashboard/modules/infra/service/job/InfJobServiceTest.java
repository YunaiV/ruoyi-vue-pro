package cn.iocoder.dashboard.modules.infra.service.job;

import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Import;
import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.framework.quartz.core.scheduler.SchedulerManager;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobCreateReqVO;
import cn.iocoder.dashboard.modules.infra.convert.job.InfJobConvert;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.job.InfJobMapper;
import cn.iocoder.dashboard.modules.infra.enums.job.InfJobStatusEnum;
import cn.iocoder.dashboard.modules.infra.service.job.impl.InfJobServiceImpl;

/**
 * {@link InfJobServiceImpl} 的单元测试
 *
 * @author neilz
 */
@Import(InfJobServiceImpl.class)
public class InfJobServiceTest extends BaseDbUnitTest {
    @Resource
    private InfJobServiceImpl jobService;

    @Resource
    private InfJobMapper jobMapper;
    @Resource
    private SchedulerManager schedulerManager;

    @Test
    public void testCreateJob_success() throws SchedulerException {
        // 准备参数
        InfJobCreateReqVO reqVO = randomPojo(InfJobCreateReqVO.class);
        reqVO.setCronExpression("0 0/1 * * * ? *");

        // 调用
        Long jobId = jobService.createJob(reqVO);

        // 断言
        assertNotNull(jobId);

        // 校验记录的属性是否正确
        InfJobDO job = jobMapper.selectById(jobId);
        assertPojoEquals(reqVO, job);
        assertEquals(InfJobStatusEnum.NORMAL.getStatus(), job.getStatus());

        // 校验调用
        verify(jobMapper, times(1)).selectByHandlerName(reqVO.getHandlerName());

        InfJobDO insertJob = InfJobConvert.INSTANCE.convert(reqVO);
        insertJob.setStatus(InfJobStatusEnum.INIT.getStatus());
        fillJobMonitorTimeoutEmpty(insertJob);
        verify(jobMapper, times(1)).insert(insertJob);

        verify(schedulerManager, times(1)).addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(),
                job.getRetryCount(), job.getRetryInterval());

        InfJobDO updateObj = InfJobDO.builder().id(insertJob.getId()).status(InfJobStatusEnum.NORMAL.getStatus()).build();
        verify(jobMapper, times(1)).updateById(updateObj);

    }

    private static void fillJobMonitorTimeoutEmpty(InfJobDO job) {
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
    }

}
