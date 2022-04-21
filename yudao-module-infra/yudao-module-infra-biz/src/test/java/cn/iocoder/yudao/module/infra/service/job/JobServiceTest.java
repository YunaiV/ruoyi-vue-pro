package cn.iocoder.yudao.module.infra.service.job;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.quartz.core.scheduler.SchedulerManager;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobUpdateReqVO;
import cn.iocoder.yudao.module.infra.convert.job.JobConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.JobDO;
import cn.iocoder.yudao.module.infra.dal.mysql.job.JobMapper;
import cn.iocoder.yudao.module.infra.enums.job.JobStatusEnum;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Import(JobServiceImpl.class)
public class JobServiceTest extends BaseDbUnitTest {

    @Resource
    private JobServiceImpl jobService;
    @Resource
    private JobMapper jobMapper;
    @MockBean
    private SchedulerManager schedulerManager;

    @Test
    public void testCreateJob_cronExpressionValid() {
        // 准备参数。Cron 表达式为 String 类型，默认随机字符串。
        JobCreateReqVO reqVO = randomPojo(JobCreateReqVO.class);
        // 调用，并断言异常
        assertServiceException(() -> jobService.createJob(reqVO), JOB_CRON_EXPRESSION_VALID);
    }

    @Test
    public void testCreateJob_jobHandlerExists() throws SchedulerException {
        // 准备参数 指定 Cron 表达式
        JobCreateReqVO reqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // 调用
        jobService.createJob(reqVO);
        // 调用，并断言异常
        assertServiceException(() -> jobService.createJob(reqVO), JOB_HANDLER_EXISTS);
    }

    @Test
    public void testCreateJob_success() throws SchedulerException {
        // 准备参数 指定 Cron 表达式
        JobCreateReqVO reqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // 调用
        Long jobId = jobService.createJob(reqVO);
        // 断言
        assertNotNull(jobId);
        // 校验记录的属性是否正确
        JobDO job = jobMapper.selectById(jobId);
        assertPojoEquals(reqVO, job);
        assertEquals(JobStatusEnum.NORMAL.getStatus(), job.getStatus());
        // 校验调用
        verify(schedulerManager, times(1)).addJob(eq(job.getId()), eq(job.getHandlerName()), eq(job.getHandlerParam()), eq(job.getCronExpression()),
                eq(reqVO.getRetryCount()), eq(reqVO.getRetryInterval()));
    }

    @Test
    public void testUpdateJob_jobNotExists(){
        // 准备参数
        JobUpdateReqVO reqVO = randomPojo(JobUpdateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // 调用，并断言异常
        assertServiceException(() -> jobService.updateJob(reqVO), JOB_NOT_EXISTS);
    }

    @Test
    public void testUpdateJob_onlyNormalStatus(){
        // mock 数据
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.INIT.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 准备参数
        JobUpdateReqVO updateReqVO = randomPojo(JobUpdateReqVO.class, o -> {
            o.setId(job.getId());
            o.setName(createReqVO.getName());
            o.setCronExpression(createReqVO.getCronExpression());
        });
        // 调用，并断言异常
        assertServiceException(() -> jobService.updateJob(updateReqVO), JOB_UPDATE_ONLY_NORMAL_STATUS);
    }

    @Test
    public void testUpdateJob_success() throws SchedulerException {
        // mock 数据
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 准备参数
        JobUpdateReqVO updateReqVO = randomPojo(JobUpdateReqVO.class, o -> {
            o.setId(job.getId());
            o.setName(createReqVO.getName());
            o.setCronExpression(createReqVO.getCronExpression());
        });
        // 调用
        jobService.updateJob(updateReqVO);
        // 校验记录的属性是否正确
        JobDO updateJob = jobMapper.selectById(updateReqVO.getId());
        assertPojoEquals(updateReqVO, updateJob);
        // 校验调用
        verify(schedulerManager, times(1)).updateJob(eq(job.getHandlerName()), eq(updateReqVO.getHandlerParam()), eq(updateReqVO.getCronExpression()),
                eq(updateReqVO.getRetryCount()), eq(updateReqVO.getRetryInterval()));
    }

    @Test
    public void testUpdateJobStatus_changeStatusInvalid() {
        // 调用，并断言异常
        assertServiceException(() -> jobService.updateJobStatus(1L, JobStatusEnum.INIT.getStatus()), JOB_CHANGE_STATUS_INVALID);
    }

    @Test
    public void testUpdateJobStatus_changeStatusEquals() {
        // mock 数据
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 调用，并断言异常
        assertServiceException(() -> jobService.updateJobStatus(job.getId(), job.getStatus()), JOB_CHANGE_STATUS_EQUALS);
    }

    @Test
    public void testUpdateJobStatus_NormalToStop_success() throws SchedulerException {
        // mock 数据
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 调用
        jobService.updateJobStatus(job.getId(), JobStatusEnum.STOP.getStatus());
        // 校验记录的属性是否正确
        JobDO updateJob = jobMapper.selectById(job.getId());
        assertEquals(JobStatusEnum.STOP.getStatus(), updateJob.getStatus());
        // 校验调用
        verify(schedulerManager, times(1)).pauseJob(eq(job.getHandlerName()));
    }

    @Test
    public void testUpdateJobStatus_StopToNormal_success() throws SchedulerException {
        // mock 数据
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.STOP.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 调用
        jobService.updateJobStatus(job.getId(), JobStatusEnum.NORMAL.getStatus());
        // 校验记录的属性是否正确
        JobDO updateJob = jobMapper.selectById(job.getId());
        assertEquals(JobStatusEnum.NORMAL.getStatus(), updateJob.getStatus());
        // 校验调用
        verify(schedulerManager, times(1)).resumeJob(eq(job.getHandlerName()));
    }

    @Test
    public void testTriggerJob_success() throws SchedulerException {
        // mock 数据
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 调用
        jobService.triggerJob(job.getId());
        // 校验调用
        verify(schedulerManager, times(1)).triggerJob(eq(job.getId()), eq(job.getHandlerName()), eq(job.getHandlerParam()));
    }

    @Test
    public void testDeleteJob_success() throws SchedulerException {
        // mock 数据
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 调用 UPDATE inf_job SET deleted=1 WHERE id=? AND deleted=0
        jobService.deleteJob(job.getId());
        // 校验数据不存在了  WHERE id=? AND deleted=0 查询为空正常
        assertNull(jobMapper.selectById(job.getId()));
        // 校验调用
        verify(schedulerManager, times(1)).deleteJob(eq(job.getHandlerName()));
    }

    @Test
    public void testGetJobListByIds_success() {
        // mock 数据
        JobDO dbJob = randomPojo(JobDO.class, o -> {
            o.setStatus(randomEle(JobStatusEnum.values()).getStatus()); // 保证 status 的范围
        });
        JobDO cloneJob = ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString()));
        jobMapper.insert(dbJob);
        // 测试 handlerName 不匹配
        jobMapper.insert(cloneJob);
        // 准备参数
        ArrayList<Long> ids = new ArrayList<>();
        ids.add(dbJob.getId());
        ids.add(cloneJob.getId());
        // 调用
        List<JobDO> list = jobService.getJobList(ids);
        // 断言
        assertEquals(2, list.size());
        assertPojoEquals(dbJob, list.get(0));
    }

    @Test
    public void testGetJobPage_success() {
        // mock 数据
        JobDO dbJob = randomPojo(JobDO.class, o -> {
            o.setName("定时任务测试");
            o.setHandlerName("handlerName 单元测试");
            o.setStatus(JobStatusEnum.INIT.getStatus());
        });
        jobMapper.insert(dbJob);
        // 测试 name 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setName("土豆")));
        // 测试 status 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setStatus(JobStatusEnum.NORMAL.getStatus())));
        // 测试 handlerName 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString())));
        // 准备参数
        JobPageReqVO reqVo = new JobPageReqVO();
        reqVo.setName("定时");
        reqVo.setStatus(JobStatusEnum.INIT.getStatus());
        reqVo.setHandlerName("单元");
        // 调用
        PageResult<JobDO> pageResult = jobService.getJobPage(reqVo);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbJob, pageResult.getList().get(0));
    }

    @Test
    public void testGetJobListForExport_success() {
        // mock 数据
        JobDO dbJob = randomPojo(JobDO.class, o -> {
            o.setName("定时任务测试");
            o.setHandlerName("handlerName 单元测试");
            o.setStatus(JobStatusEnum.INIT.getStatus());
        });
        jobMapper.insert(dbJob);
        // 测试 name 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setName("土豆")));
        // 测试 status 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setStatus(JobStatusEnum.NORMAL.getStatus())));
        // 测试 handlerName 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString())));
        // 准备参数
        JobExportReqVO reqVo = new JobExportReqVO();
        reqVo.setName("定时");
        reqVo.setStatus(JobStatusEnum.INIT.getStatus());
        reqVo.setHandlerName("单元");
        // 调用
        List<JobDO> list = jobService.getJobList(reqVo);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbJob, list.get(0));
    }

    private static void fillJobMonitorTimeoutEmpty(JobDO job) {
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
    }

}
