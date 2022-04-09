package cn.iocoder.yudao.module.infra.service.job;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.JobLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.JobLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.JobLogDO;
import cn.iocoder.yudao.module.infra.dal.mysql.job.JobLogMapper;
import cn.iocoder.yudao.module.infra.enums.job.JobLogStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(JobLogServiceImpl.class)
public class JobLogServiceTest extends BaseDbUnitTest {

    @Resource
    private JobLogServiceImpl jobLogService;
    @Resource
    private JobLogMapper jobLogMapper;

    @Test
    public void testCreateJobLog_success() {
        // 准备参数
        JobLogDO reqVO = randomPojo(JobLogDO.class, o -> {
            o.setExecuteIndex(1);
        });
        // 调用
        Long jobLogId = jobLogService.createJobLog(reqVO.getJobId(), reqVO.getBeginTime(), reqVO.getHandlerName(), reqVO.getHandlerParam(), reqVO.getExecuteIndex());
        // 断言
        assertNotNull(jobLogId);
        // 校验记录的属性是否正确
        JobLogDO job = jobLogMapper.selectById(jobLogId);
        assertEquals(JobLogStatusEnum.RUNNING.getStatus(), job.getStatus());
    }

    @Test
    public void testUpdateJobLogResultAsync_success() {
        // 准备参数
        JobLogDO reqVO = randomPojo(JobLogDO.class, o -> {
            o.setExecuteIndex(1);
        });
        JobLogDO log = JobLogDO.builder().jobId(reqVO.getJobId()).handlerName(reqVO.getHandlerName()).handlerParam(reqVO.getHandlerParam()).executeIndex(reqVO.getExecuteIndex())
                .beginTime(reqVO.getBeginTime()).status(JobLogStatusEnum.RUNNING.getStatus()).build();
        jobLogMapper.insert(log);
        // 调用
        jobLogService.updateJobLogResultAsync(log.getId(), reqVO.getBeginTime(), reqVO.getDuration(), true,reqVO.getResult());
        // 校验记录的属性是否正确
        JobLogDO job = jobLogMapper.selectById(log.getId());
        assertEquals(JobLogStatusEnum.SUCCESS.getStatus(), job.getStatus());

        // 调用
        jobLogService.updateJobLogResultAsync(log.getId(), reqVO.getBeginTime(), reqVO.getDuration(), false,reqVO.getResult());
        // 校验记录的属性是否正确
        JobLogDO job2 = jobLogMapper.selectById(log.getId());
        assertEquals(JobLogStatusEnum.FAILURE.getStatus(), job2.getStatus());
    }

    @Test
    public void testGetJobLogListByIds_success() {
        // mock 数据
        JobLogDO dbJobLog = randomPojo(JobLogDO.class, o -> {
            o.setExecuteIndex(1);
            o.setStatus(randomEle(JobLogStatusEnum.values()).getStatus()); // 保证 status 的范围
        });
        JobLogDO cloneJobLog = ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setHandlerName(randomString()));
        jobLogMapper.insert(dbJobLog);
        // 测试 handlerName 不匹配
        jobLogMapper.insert(cloneJobLog);
        // 准备参数
        ArrayList ids = new ArrayList<>();
        ids.add(dbJobLog.getId());
        ids.add(cloneJobLog.getId());
        // 调用
        List<JobLogDO> list = jobLogService.getJobLogList(ids);
        // 断言
        assertEquals(2, list.size());
        assertPojoEquals(dbJobLog, list.get(0));
    }

    @Test
    public void testGetJobPage_success() {
        // mock 数据
        JobLogDO dbJobLog = randomPojo(JobLogDO.class, o -> {
            o.setExecuteIndex(1);
            o.setHandlerName("handlerName 单元测试");
            o.setStatus(JobLogStatusEnum.SUCCESS.getStatus());
            o.setBeginTime(buildTime(2021, 1, 8));
            o.setEndTime(buildTime(2021, 1, 8));
        });
        jobLogMapper.insert(dbJobLog);
        // 测试 jobId 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setJobId(randomLongId())));
        // 测试 handlerName 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setHandlerName(randomString())));
        // 测试 beginTime 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setBeginTime(buildTime(2021, 1, 7))));
        // 测试 endTime 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setEndTime(buildTime(2021, 1, 9))));
        // 测试 status 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setStatus(JobLogStatusEnum.FAILURE.getStatus())));
        // 准备参数
        JobLogPageReqVO reqVo = new JobLogPageReqVO();
        reqVo.setJobId(dbJobLog.getJobId());
        reqVo.setHandlerName("单元");
        reqVo.setBeginTime(dbJobLog.getBeginTime());
        reqVo.setEndTime(dbJobLog.getEndTime());
        reqVo.setStatus(JobLogStatusEnum.SUCCESS.getStatus());
        // 调用
        PageResult<JobLogDO> pageResult = jobLogService.getJobLogPage(reqVo);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbJobLog, pageResult.getList().get(0));
    }

    @Test
    public void testGetJobListForExport_success() {
        // mock 数据
        JobLogDO dbJobLog = randomPojo(JobLogDO.class, o -> {
            o.setExecuteIndex(1);
            o.setHandlerName("handlerName 单元测试");
            o.setStatus(JobLogStatusEnum.SUCCESS.getStatus());
            o.setBeginTime(buildTime(2021, 1, 8));
            o.setEndTime(buildTime(2021, 1, 8));
        });
        jobLogMapper.insert(dbJobLog);
        // 测试 jobId 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setJobId(randomLongId())));
        // 测试 handlerName 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setHandlerName(randomString())));
        // 测试 beginTime 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setBeginTime(buildTime(2021, 1, 7))));
        // 测试 endTime 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setEndTime(buildTime(2021, 1, 9))));
        // 测试 status 不匹配
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setStatus(JobLogStatusEnum.FAILURE.getStatus())));
        // 准备参数
        JobLogExportReqVO reqVo = new JobLogExportReqVO();
        reqVo.setJobId(dbJobLog.getJobId());
        reqVo.setHandlerName("单元");
        reqVo.setBeginTime(dbJobLog.getBeginTime());
        reqVo.setEndTime(dbJobLog.getEndTime());
        reqVo.setStatus(JobLogStatusEnum.SUCCESS.getStatus());
        // 调用
        List<JobLogDO> list = jobLogService.getJobLogList(reqVo);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbJobLog, list.get(0));
    }

}
