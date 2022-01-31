package cn.iocoder.yudao.module.infra.service.job;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.module.infra.enums.InfErrorCodeConstants.JOB_CHANGE_STATUS_EQUALS;
import static cn.iocoder.yudao.module.infra.enums.InfErrorCodeConstants.JOB_CHANGE_STATUS_INVALID;
import static cn.iocoder.yudao.module.infra.enums.InfErrorCodeConstants.JOB_CRON_EXPRESSION_VALID;
import static cn.iocoder.yudao.module.infra.enums.InfErrorCodeConstants.JOB_HANDLER_EXISTS;
import static cn.iocoder.yudao.module.infra.enums.InfErrorCodeConstants.JOB_NOT_EXISTS;
import static cn.iocoder.yudao.module.infra.enums.InfErrorCodeConstants.JOB_UPDATE_ONLY_NORMAL_STATUS;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.quartz.core.scheduler.SchedulerManager;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.InfJobCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.InfJobExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.InfJobPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.InfJobUpdateReqVO;
import cn.iocoder.yudao.module.infra.convert.job.InfJobConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.InfJobDO;
import cn.iocoder.yudao.module.infra.dal.mysql.job.InfJobMapper;
import cn.iocoder.yudao.module.infra.enums.job.InfJobStatusEnum;
import cn.iocoder.yudao.module.infra.service.job.impl.InfJobServiceImpl;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;

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
    @MockBean
    private SchedulerManager schedulerManager;

    @Test
    public void testCreateJob_cronExpressionValid() {
        // 准备参数。Cron 表达式为 String 类型，默认随机字符串。
        InfJobCreateReqVO reqVO = randomPojo(InfJobCreateReqVO.class);
        // 调用，并断言异常
        assertServiceException(() -> jobService.createJob(reqVO), JOB_CRON_EXPRESSION_VALID);
    }

    @Test
    public void testCreateJob_jobHandlerExists() throws SchedulerException {
        // 准备参数 指定 Cron 表达式
        InfJobCreateReqVO reqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // 调用
        jobService.createJob(reqVO);
        // 调用，并断言异常
        assertServiceException(() -> jobService.createJob(reqVO), JOB_HANDLER_EXISTS);
    }

    @Test
    public void testCreateJob_success() throws SchedulerException {
        // 准备参数 指定 Cron 表达式
        InfJobCreateReqVO reqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // 调用
        Long jobId = jobService.createJob(reqVO);
        // 断言
        assertNotNull(jobId);
        // 校验记录的属性是否正确
        InfJobDO job = jobMapper.selectById(jobId);
        assertPojoEquals(reqVO, job);
        assertEquals(InfJobStatusEnum.NORMAL.getStatus(), job.getStatus());
        // 校验调用
        verify(schedulerManager, times(1)).addJob(eq(job.getId()), eq(job.getHandlerName()), eq(job.getHandlerParam()), eq(job.getCronExpression()),
                eq(reqVO.getRetryCount()), eq(reqVO.getRetryInterval()));
    }

    @Test
    public void testUpdateJob_jobNotExists(){
        // 准备参数
        InfJobUpdateReqVO reqVO = randomPojo(InfJobUpdateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // 调用，并断言异常
        assertServiceException(() -> jobService.updateJob(reqVO), JOB_NOT_EXISTS);
    }

    @Test
    public void testUpdateJob_onlyNormalStatus(){
        // mock 数据
        InfJobCreateReqVO createReqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        InfJobDO job = InfJobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(InfJobStatusEnum.INIT.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 准备参数
        InfJobUpdateReqVO updateReqVO = randomPojo(InfJobUpdateReqVO.class, o -> {
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
        InfJobCreateReqVO createReqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        InfJobDO job = InfJobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(InfJobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 准备参数
        InfJobUpdateReqVO updateReqVO = randomPojo(InfJobUpdateReqVO.class, o -> {
            o.setId(job.getId());
            o.setName(createReqVO.getName());
            o.setCronExpression(createReqVO.getCronExpression());
        });
        // 调用
        jobService.updateJob(updateReqVO);
        // 校验记录的属性是否正确
        InfJobDO updateJob = jobMapper.selectById(updateReqVO.getId());
        assertPojoEquals(updateReqVO, updateJob);
        // 校验调用
        verify(schedulerManager, times(1)).updateJob(eq(job.getHandlerName()), eq(updateReqVO.getHandlerParam()), eq(updateReqVO.getCronExpression()),
                eq(updateReqVO.getRetryCount()), eq(updateReqVO.getRetryInterval()));
    }

    @Test
    public void testUpdateJobStatus_changeStatusInvalid() {
        // 调用，并断言异常
        assertServiceException(() -> jobService.updateJobStatus(1l, InfJobStatusEnum.INIT.getStatus()), JOB_CHANGE_STATUS_INVALID);
    }

    @Test
    public void testUpdateJobStatus_changeStatusEquals() {
        // mock 数据
        InfJobCreateReqVO createReqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        InfJobDO job = InfJobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(InfJobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 调用，并断言异常
        assertServiceException(() -> jobService.updateJobStatus(job.getId(), job.getStatus()), JOB_CHANGE_STATUS_EQUALS);
    }

    @Test
    public void testUpdateJobStatus_NormalToStop_success() throws SchedulerException {
        // mock 数据
        InfJobCreateReqVO createReqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        InfJobDO job = InfJobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(InfJobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 调用
        jobService.updateJobStatus(job.getId(), InfJobStatusEnum.STOP.getStatus());
        // 校验记录的属性是否正确
        InfJobDO updateJob = jobMapper.selectById(job.getId());
        assertEquals(InfJobStatusEnum.STOP.getStatus(), updateJob.getStatus());
        // 校验调用
        verify(schedulerManager, times(1)).pauseJob(eq(job.getHandlerName()));
    }

    @Test
    public void testUpdateJobStatus_StopToNormal_success() throws SchedulerException {
        // mock 数据
        InfJobCreateReqVO createReqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        InfJobDO job = InfJobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(InfJobStatusEnum.STOP.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // 调用
        jobService.updateJobStatus(job.getId(), InfJobStatusEnum.NORMAL.getStatus());
        // 校验记录的属性是否正确
        InfJobDO updateJob = jobMapper.selectById(job.getId());
        assertEquals(InfJobStatusEnum.NORMAL.getStatus(), updateJob.getStatus());
        // 校验调用
        verify(schedulerManager, times(1)).resumeJob(eq(job.getHandlerName()));
    }

    @Test
    public void testTriggerJob_success() throws SchedulerException {
        // mock 数据
        InfJobCreateReqVO createReqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        InfJobDO job = InfJobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(InfJobStatusEnum.NORMAL.getStatus());
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
        InfJobCreateReqVO createReqVO = randomPojo(InfJobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        InfJobDO job = InfJobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(InfJobStatusEnum.NORMAL.getStatus());
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
        InfJobDO dbJob = randomPojo(InfJobDO.class, o -> {
            o.setStatus(randomEle(InfJobStatusEnum.values()).getStatus()); // 保证 status 的范围
        });
        InfJobDO cloneJob = ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString()));
        jobMapper.insert(dbJob);
        // 测试 handlerName 不匹配
        jobMapper.insert(cloneJob);
        // 准备参数
        ArrayList ids = new ArrayList<>();
        ids.add(dbJob.getId());
        ids.add(cloneJob.getId());
        // 调用
        List<InfJobDO> list = jobService.getJobList(ids);
        // 断言
        assertEquals(2, list.size());
        assertPojoEquals(dbJob, list.get(0));
    }

    @Test
    public void testGetJobPage_success() {
        // mock 数据
        InfJobDO dbJob = randomPojo(InfJobDO.class, o -> {
            o.setName("定时任务测试");
            o.setHandlerName("handlerName 单元测试");
            o.setStatus(InfJobStatusEnum.INIT.getStatus());
        });
        jobMapper.insert(dbJob);
        // 测试 name 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setName("土豆")));
        // 测试 status 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setStatus(InfJobStatusEnum.NORMAL.getStatus())));
        // 测试 handlerName 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString())));
        // 准备参数
        InfJobPageReqVO reqVo = new InfJobPageReqVO();
        reqVo.setName("定时");
        reqVo.setStatus(InfJobStatusEnum.INIT.getStatus());
        reqVo.setHandlerName("单元");
        // 调用
        PageResult<InfJobDO> pageResult = jobService.getJobPage(reqVo);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbJob, pageResult.getList().get(0));
    }

    @Test
    public void testGetJobListForExport_success() {
        // mock 数据
        InfJobDO dbJob = randomPojo(InfJobDO.class, o -> {
            o.setName("定时任务测试");
            o.setHandlerName("handlerName 单元测试");
            o.setStatus(InfJobStatusEnum.INIT.getStatus());
        });
        jobMapper.insert(dbJob);
        // 测试 name 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setName("土豆")));
        // 测试 status 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setStatus(InfJobStatusEnum.NORMAL.getStatus())));
        // 测试 handlerName 不匹配
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString())));
        // 准备参数
        InfJobExportReqVO reqVo = new InfJobExportReqVO();
        reqVo.setName("定时");
        reqVo.setStatus(InfJobStatusEnum.INIT.getStatus());
        reqVo.setHandlerName("单元");
        // 调用
        List<InfJobDO> list = jobService.getJobList(reqVo);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbJob, list.get(0));
    }

    private static void fillJobMonitorTimeoutEmpty(InfJobDO job) {
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
    }

}
