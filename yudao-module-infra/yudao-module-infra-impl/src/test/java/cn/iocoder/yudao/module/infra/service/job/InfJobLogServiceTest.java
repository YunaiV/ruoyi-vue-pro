package cn.iocoder.yudao.module.infra.service.job;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.InfJobLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.InfJobLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.InfJobLogDO;
import cn.iocoder.yudao.module.infra.dal.mysql.job.InfJobLogMapper;
import cn.iocoder.yudao.module.infra.enums.job.InfJobLogStatusEnum;
import cn.iocoder.yudao.module.infra.service.job.impl.InfJobLogServiceImpl;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;

/**
 * {@link InfJobLogServiceImpl} 的单元测试
 *
 * @author neilz
 */
@Import(InfJobLogServiceImpl.class)
public class InfJobLogServiceTest extends BaseDbUnitTest {

    @Resource
    private InfJobLogServiceImpl jobLogService;
    @Resource
    private InfJobLogMapper jobLogMapper;

    @Test
    public void testCreateJobLog_success() {
        // 准备参数
        InfJobLogDO reqVO = randomPojo(InfJobLogDO.class, o -> {
            o.setExecuteIndex(1);
        });
        // 调用
        Long jobLogId = jobLogService.createJobLog(reqVO.getJobId(), reqVO.getBeginTime(), reqVO.getHandlerName(), reqVO.getHandlerParam(), reqVO.getExecuteIndex());
        // 断言
        assertNotNull(jobLogId);
        // 校验记录的属性是否正确
        InfJobLogDO job = jobLogMapper.selectById(jobLogId);
        assertEquals(InfJobLogStatusEnum.RUNNING.getStatus(), job.getStatus());
    }

    @Test
    public void testUpdateJobLogResultAsync_success() {
        // 准备参数
        InfJobLogDO reqVO = randomPojo(InfJobLogDO.class, o -> {
            o.setExecuteIndex(1);
        });
        InfJobLogDO log = InfJobLogDO.builder().jobId(reqVO.getJobId()).handlerName(reqVO.getHandlerName()).handlerParam(reqVO.getHandlerParam()).executeIndex(reqVO.getExecuteIndex())
                .beginTime(reqVO.getBeginTime()).status(InfJobLogStatusEnum.RUNNING.getStatus()).build();
        jobLogMapper.insert(log);
        // 调用
        jobLogService.updateJobLogResultAsync(log.getId(), reqVO.getBeginTime(), reqVO.getDuration(), true,reqVO.getResult());
        // 校验记录的属性是否正确
        InfJobLogDO job = jobLogMapper.selectById(log.getId());
        assertEquals(InfJobLogStatusEnum.SUCCESS.getStatus(), job.getStatus());

        // 调用
        jobLogService.updateJobLogResultAsync(log.getId(), reqVO.getBeginTime(), reqVO.getDuration(), false,reqVO.getResult());
        // 校验记录的属性是否正确
        InfJobLogDO job2 = jobLogMapper.selectById(log.getId());
        assertEquals(InfJobLogStatusEnum.FAILURE.getStatus(), job2.getStatus());
    }

    @Test
    public void testGetJobLogListByIds_success() {
        // mock 数据
        InfJobLogDO dbJobLog = randomPojo(InfJobLogDO.class, o -> {
            o.setExecuteIndex(1);
            o.setStatus(randomEle(InfJobLogStatusEnum.values()).getStatus()); // 保证 status 的范围
        });
        InfJobLogDO cloneJobLog = ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setHandlerName(randomString()));
        jobLogMapper.insert(dbJobLog);
        // 测试 handlerName 不匹配
        jobLogMapper.insert(cloneJobLog);
        // 准备参数
        ArrayList ids = new ArrayList<>();
        ids.add(dbJobLog.getId());
        ids.add(cloneJobLog.getId());
        // 调用
        List<InfJobLogDO> list = jobLogService.getJobLogList(ids);
        // 断言
        assertEquals(2, list.size());
        assertPojoEquals(dbJobLog, list.get(0));
    }

    @Test
    public void testGetJobPage_success() {
        // mock 数据
        InfJobLogDO dbJobLog = randomPojo(InfJobLogDO.class, o -> {
            o.setExecuteIndex(1);
            o.setHandlerName("handlerName 单元测试");
            o.setStatus(InfJobLogStatusEnum.SUCCESS.getStatus());
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
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setStatus(InfJobLogStatusEnum.FAILURE.getStatus())));
        // 准备参数
        InfJobLogPageReqVO reqVo = new InfJobLogPageReqVO();
        reqVo.setJobId(dbJobLog.getJobId());
        reqVo.setHandlerName("单元");
        reqVo.setBeginTime(dbJobLog.getBeginTime());
        reqVo.setEndTime(dbJobLog.getEndTime());
        reqVo.setStatus(InfJobLogStatusEnum.SUCCESS.getStatus());
        // 调用
        PageResult<InfJobLogDO> pageResult = jobLogService.getJobLogPage(reqVo);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbJobLog, pageResult.getList().get(0));
    }

    @Test
    public void testGetJobListForExport_success() {
        // mock 数据
        InfJobLogDO dbJobLog = randomPojo(InfJobLogDO.class, o -> {
            o.setExecuteIndex(1);
            o.setHandlerName("handlerName 单元测试");
            o.setStatus(InfJobLogStatusEnum.SUCCESS.getStatus());
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
        jobLogMapper.insert(ObjectUtils.cloneIgnoreId(dbJobLog, o -> o.setStatus(InfJobLogStatusEnum.FAILURE.getStatus())));
        // 准备参数
        InfJobLogExportReqVO reqVo = new InfJobLogExportReqVO();
        reqVo.setJobId(dbJobLog.getJobId());
        reqVo.setHandlerName("单元");
        reqVo.setBeginTime(dbJobLog.getBeginTime());
        reqVo.setEndTime(dbJobLog.getEndTime());
        reqVo.setStatus(InfJobLogStatusEnum.SUCCESS.getStatus());
        // 调用
        List<InfJobLogDO> list = jobLogService.getJobLogList(reqVo);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbJobLog, list.get(0));
    }

}
