package cn.iocoder.yudao.module.pay.service.notify;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.controller.admin.notify.vo.PayNotifyTaskPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.notify.PayNotifyLogDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.notify.PayNotifyTaskDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.notify.PayNotifyLogMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.notify.PayNotifyTaskMapper;
import cn.iocoder.yudao.module.pay.dal.redis.notify.PayNotifyLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyStatusEnum;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.framework.job.config.PayJobConfiguration;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link PayRefundServiceImpl} 的单元测试类
 *
 * @author 芋艿
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import({PayJobConfiguration.class, PayNotifyServiceImpl.class, PayNotifyLockRedisDAO.class})
public class PayNotifyServiceTest extends BaseDbUnitTest {

    @Resource
    private PayNotifyServiceImpl notifyService;

    @MockBean
    private PayOrderService orderService;
    @MockBean
    private PayRefundService refundService;

    @Resource
    private PayNotifyTaskMapper notifyTaskMapper;
    @Resource
    private PayNotifyLogMapper notifyLogMapper;

    @MockBean
    private RedissonClient redissonClient;

    @Test
    public void testCreatePayNotifyTask_order() {
        PayNotifyServiceImpl payNotifyService = mock(PayNotifyServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayNotifyServiceImpl.class)))
                    .thenReturn(payNotifyService);

            // 准备参数
            Integer type = PayNotifyTypeEnum.ORDER.getType();
            Long dataId = 1L;
            // mock 方法(order)
            PayOrderDO order = randomPojo(PayOrderDO.class);
            when(orderService.getOrder(eq(1L))).thenReturn(order);
            // mock 方法（lock）
            mockLock(null); // null 的原因，是咱没办法拿到 taskId 新增

            // 调用
            notifyService.createPayNotifyTask(type, dataId);
            // 断言，task
            PayNotifyTaskDO dbTask = notifyTaskMapper.selectOne(null);
            assertNotNull(dbTask.getNextNotifyTime());
            assertThat(dbTask)
                    .extracting("type", "dataId", "status", "notifyTimes", "maxNotifyTimes",
                            "appId", "merchantOrderId", "notifyUrl")
                    .containsExactly(type, dataId, PayNotifyStatusEnum.WAITING.getStatus(), 0, 9,
                            order.getAppId(), order.getMerchantOrderId(), order.getNotifyUrl());
            // 断言，调用
            verify(payNotifyService).executeNotify0(eq(dbTask));
        }
    }

    @Test
    public void testCreatePayNotifyTask_refund() {
        PayNotifyServiceImpl payNotifyService = mock(PayNotifyServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayNotifyServiceImpl.class)))
                    .thenReturn(payNotifyService);

            // 准备参数
            Integer type = PayNotifyTypeEnum.REFUND.getType();
            Long dataId = 1L;
            // mock 方法(refund)
            PayRefundDO refund = randomPojo(PayRefundDO.class);
            when(refundService.getRefund(eq(1L))).thenReturn(refund);
            // mock 方法（lock）
            mockLock(null); // null 的原因，是咱没办法拿到 taskId 新增

            // 调用
            notifyService.createPayNotifyTask(type, dataId);
            // 断言，task
            PayNotifyTaskDO dbTask = notifyTaskMapper.selectOne(null);
            assertNotNull(dbTask.getNextNotifyTime());
            assertThat(dbTask)
                    .extracting("type", "dataId", "status", "notifyTimes", "maxNotifyTimes",
                            "appId", "merchantOrderId", "notifyUrl")
                    .containsExactly(type, dataId, PayNotifyStatusEnum.WAITING.getStatus(), 0, 9,
                            refund.getAppId(), refund.getMerchantOrderId(), refund.getNotifyUrl());
            // 断言，调用
            verify(payNotifyService).executeNotify0(eq(dbTask));
        }
    }

    @Test
    public void testExecuteNotify() throws InterruptedException {
        // mock 数据（notify）
        PayNotifyTaskDO dbTask01 = randomPojo(PayNotifyTaskDO.class,
                o -> o.setStatus(PayNotifyStatusEnum.WAITING.getStatus())
                        .setNextNotifyTime(addTime(Duration.ofMinutes(-1))));
        notifyTaskMapper.insert(dbTask01);
        PayNotifyTaskDO dbTask02 = randomPojo(PayNotifyTaskDO.class,
                o -> o.setStatus(PayNotifyStatusEnum.REQUEST_SUCCESS.getStatus())
                        .setNextNotifyTime(addTime(Duration.ofMinutes(-1))));
        notifyTaskMapper.insert(dbTask02);
        PayNotifyTaskDO dbTask03 = randomPojo(PayNotifyTaskDO.class,
                o -> o.setStatus(PayNotifyStatusEnum.REQUEST_FAILURE.getStatus())
                        .setNextNotifyTime(addTime(Duration.ofMinutes(-1))));
        notifyTaskMapper.insert(dbTask03);
        PayNotifyTaskDO dbTask04 = randomPojo(PayNotifyTaskDO.class, // 不满足状态
                o -> o.setStatus(PayNotifyStatusEnum.FAILURE.getStatus())
                        .setNextNotifyTime(addTime(Duration.ofMinutes(-1))));
        notifyTaskMapper.insert(dbTask04);
        PayNotifyTaskDO dbTask05 = randomPojo(PayNotifyTaskDO.class, // 不满足状态
                o -> o.setStatus(PayNotifyStatusEnum.SUCCESS.getStatus())
                        .setNextNotifyTime(addTime(Duration.ofMinutes(-1))));
        notifyTaskMapper.insert(dbTask05);
        PayNotifyTaskDO dbTask06 = randomPojo(PayNotifyTaskDO.class, // 不满足时间
                o -> o.setStatus(PayNotifyStatusEnum.SUCCESS.getStatus())
                        .setNextNotifyTime(addTime(Duration.ofMinutes(1))));
        notifyTaskMapper.insert(dbTask06);
        // mock 方法（lock）
        mockLock(dbTask01.getId());
        mockLock(dbTask02.getId());
        mockLock(dbTask03.getId());

        // 调用
        int count = notifyService.executeNotify();
        // 断言，数量
        assertEquals(count, 3);
    }

    @Test // 由于 HttpUtil 不好 mock，所以只测试异常的情况
    public void testExecuteNotify0_exception() {
        // mock 数据（task）
        PayNotifyTaskDO task = randomPojo(PayNotifyTaskDO.class, o -> o.setType(-1)
                .setNotifyTimes(0).setMaxNotifyTimes(9));
        notifyTaskMapper.insert(task);

        // 调用
        notifyService.executeNotify0(task);
        // 断言，task
        PayNotifyTaskDO dbTask = notifyTaskMapper.selectById(task.getId());
        assertNotEquals(task.getNextNotifyTime(), dbTask.getNextNotifyTime());
        assertNotEquals(task.getLastExecuteTime(), dbTask.getNextNotifyTime());
        assertEquals(dbTask.getNotifyTimes(), 1);
        assertEquals(dbTask.getStatus(), PayNotifyStatusEnum.REQUEST_FAILURE.getStatus());
        // 断言，log
        PayNotifyLogDO dbLog = notifyLogMapper.selectOne(null);
        assertEquals(dbLog.getTaskId(), task.getId());
        assertEquals(dbLog.getNotifyTimes(), 1);
        assertTrue(dbLog.getResponse().contains("未知的通知任务类型："));
        assertEquals(dbLog.getStatus(), PayNotifyStatusEnum.REQUEST_FAILURE.getStatus());
    }

    @Test
    public void testProcessNotifyResult_success() {
        // mock 数据（task）
        PayNotifyTaskDO task = randomPojo(PayNotifyTaskDO.class,
                o -> o.setNotifyTimes(0).setMaxNotifyTimes(9));
        notifyTaskMapper.insert(task);
        // 准备参数
        CommonResult<?> invokeResult = CommonResult.success(randomString());

        // 调用
        notifyService.processNotifyResult(task, invokeResult, null);
        // 断言
        PayNotifyTaskDO dbTask = notifyTaskMapper.selectById(task.getId());
        assertEquals(task.getNextNotifyTime(), dbTask.getNextNotifyTime());
        assertNotEquals(task.getLastExecuteTime(), dbTask.getNextNotifyTime());
        assertEquals(dbTask.getNotifyTimes(), 1);
        assertEquals(dbTask.getStatus(), PayNotifyStatusEnum.SUCCESS.getStatus());
    }

    @Test
    public void testProcessNotifyResult_failure() {
        // mock 数据（task）
        PayNotifyTaskDO task = randomPojo(PayNotifyTaskDO.class,
                o -> o.setNotifyTimes(8).setMaxNotifyTimes(9));
        notifyTaskMapper.insert(task);
        // 准备参数
        CommonResult<?> invokeResult = CommonResult.error(BAD_REQUEST);

        // 调用
        notifyService.processNotifyResult(task, invokeResult, null);
        // 断言
        PayNotifyTaskDO dbTask = notifyTaskMapper.selectById(task.getId());
        assertEquals(task.getNextNotifyTime(), dbTask.getNextNotifyTime());
        assertNotEquals(task.getLastExecuteTime(), dbTask.getNextNotifyTime());
        assertEquals(dbTask.getNotifyTimes(), 9);
        assertEquals(dbTask.getStatus(), PayNotifyStatusEnum.FAILURE.getStatus());
    }

    @Test
    public void testProcessNotifyResult_requestFailure() {
        // mock 数据（task）
        PayNotifyTaskDO task = randomPojo(PayNotifyTaskDO.class,
                o -> o.setNotifyTimes(0).setMaxNotifyTimes(9));
        notifyTaskMapper.insert(task);
        // 准备参数
        CommonResult<?> invokeResult = CommonResult.error(BAD_REQUEST);

        // 调用
        notifyService.processNotifyResult(task, invokeResult, null);
        // 断言
        PayNotifyTaskDO dbTask = notifyTaskMapper.selectById(task.getId());
        assertNotEquals(task.getNextNotifyTime(), dbTask.getNextNotifyTime());
        assertNotEquals(task.getLastExecuteTime(), dbTask.getNextNotifyTime());
        assertEquals(dbTask.getNotifyTimes(), 1);
        assertEquals(dbTask.getStatus(), PayNotifyStatusEnum.REQUEST_SUCCESS.getStatus());
    }

    @Test
    public void testProcessNotifyResult_requestSuccess() {
        // mock 数据（task）
        PayNotifyTaskDO task = randomPojo(PayNotifyTaskDO.class,
                o -> o.setNotifyTimes(0).setMaxNotifyTimes(9));
        notifyTaskMapper.insert(task);
        // 准备参数
        CommonResult<?> invokeResult = CommonResult.error(BAD_REQUEST);
        RuntimeException invokeException = new RuntimeException();

        // 调用
        notifyService.processNotifyResult(task, invokeResult, invokeException);
        // 断言
        PayNotifyTaskDO dbTask = notifyTaskMapper.selectById(task.getId());
        assertNotEquals(task.getNextNotifyTime(), dbTask.getNextNotifyTime());
        assertNotEquals(task.getLastExecuteTime(), dbTask.getNextNotifyTime());
        assertEquals(dbTask.getNotifyTimes(), 1);
        assertEquals(dbTask.getStatus(), PayNotifyStatusEnum.REQUEST_FAILURE.getStatus());
    }

    @Test
    public void testGetNotifyTask() {
        // mock 数据（task）
        PayNotifyTaskDO task = randomPojo(PayNotifyTaskDO.class);
        notifyTaskMapper.insert(task);
        // 准备参数
        Long id = task.getId();

        // 调用
        PayNotifyTaskDO dbTask = notifyService.getNotifyTask(id);
        // 断言
        assertPojoEquals(dbTask, task);
    }

    @Test
    public void testGetNotifyTaskPage() {
        // mock 数据
        PayNotifyTaskDO dbTask = randomPojo(PayNotifyTaskDO.class, o -> { // 等会查询到
            o.setAppId(1L);
            o.setType(PayNotifyTypeEnum.REFUND.getType());
            o.setDataId(100L);
            o.setStatus(PayNotifyStatusEnum.SUCCESS.getStatus());
            o.setMerchantOrderId("P110");
            o.setCreateTime(buildTime(2023, 2, 3));
        });
        notifyTaskMapper.insert(dbTask);
        // 测试 appId 不匹配
        notifyTaskMapper.insert(cloneIgnoreId(dbTask, o -> o.setAppId(2L)));
        // 测试 type 不匹配
        notifyTaskMapper.insert(cloneIgnoreId(dbTask, o -> o.setType(PayNotifyTypeEnum.ORDER.getType())));
        // 测试 dataId 不匹配
        notifyTaskMapper.insert(cloneIgnoreId(dbTask, o -> o.setDataId(200L)));
        // 测试 status 不匹配
        notifyTaskMapper.insert(cloneIgnoreId(dbTask, o -> o.setStatus(PayNotifyStatusEnum.FAILURE.getStatus())));
        // 测试 merchantOrderId 不匹配
        notifyTaskMapper.insert(cloneIgnoreId(dbTask, o -> o.setMerchantOrderId(randomString())));
        // 测试 createTime 不匹配
        notifyTaskMapper.insert(cloneIgnoreId(dbTask, o -> o.setCreateTime(buildTime(2023, 1, 1))));
        // 准备参数
        PayNotifyTaskPageReqVO reqVO = new PayNotifyTaskPageReqVO();
        reqVO.setAppId(1L);
        reqVO.setType(PayNotifyTypeEnum.REFUND.getType());
        reqVO.setDataId(100L);
        reqVO.setStatus(PayNotifyStatusEnum.SUCCESS.getStatus());
        reqVO.setMerchantOrderId("P110");
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<PayNotifyTaskDO> pageResult = notifyService.getNotifyTaskPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbTask, pageResult.getList().get(0));
    }

    @Test
    public void testGetNotifyLogList() {
        // mock 数据
        PayNotifyLogDO dbLog = randomPojo(PayNotifyLogDO.class);
        notifyLogMapper.insert(dbLog);
        PayNotifyLogDO dbLog02 = randomPojo(PayNotifyLogDO.class);
        notifyLogMapper.insert(dbLog02);
        // 准备参数
        Long taskId = dbLog.getTaskId();

        // 调用
        List<PayNotifyLogDO> logList = notifyService.getNotifyLogList(taskId);
        // 断言
        assertEquals(logList.size(), 1);
        assertPojoEquals(dbLog, logList.get(0));
    }

    private void mockLock(Long id) {
        RLock lock = mock(RLock.class);
        if (id == null) {
            when(redissonClient.getLock(anyString()))
                    .thenReturn(lock);
        } else {
            when(redissonClient.getLock(eq("pay_notify:lock:" + id)))
                    .thenReturn(lock);
        }
    }

}
