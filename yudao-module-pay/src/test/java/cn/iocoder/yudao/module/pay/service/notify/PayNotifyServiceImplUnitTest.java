package cn.iocoder.yudao.module.pay.service.notify;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.dal.dataobject.notify.PayNotifyTaskDO;
import cn.iocoder.yudao.module.pay.dal.mysql.notify.PayNotifyTaskMapper;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyStatusEnum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link PayNotifyServiceImpl} 的单元测试类
 *
 * 重点覆盖 {@link PayNotifyServiceImpl#processNotifyResult(PayNotifyTaskDO, CommonResult, Throwable)} 的通知次数与间隔：
 * 算上首次的通知，一共 1 + 8 = 9 次（与 maxNotifyTimes 一致）；第 1 ~ 8 次通知失败后，依次使用 8 个通知间隔；第 9 次通知失败后，标记为通知失败
 *
 * @author 芋道源码
 */
public class PayNotifyServiceImplUnitTest extends BaseMockitoUnitTest {

    private static final Long TASK_ID = 1024L;
    /**
     * 最大通知次数，即 {@link PayNotifyServiceImpl#createPayNotifyTask(Integer, Long)} 写入的 maxNotifyTimes
     */
    private static final int MAX_NOTIFY_TIMES = 9;

    @InjectMocks
    private PayNotifyServiceImpl notifyService;

    @Mock
    private PayNotifyTaskMapper notifyTaskMapper;

    @ParameterizedTest(name = "第 {0} 次通知失败（异常：{2}），{1} 秒后重试")
    @MethodSource("retryIntervals")
    public void testProcessNotifyResult_retry(int notifyTimes, long expectedIntervalSeconds, boolean invokeException) {
        // 准备参数：本次是第 notifyTimes 次通知
        PayNotifyTaskDO task = buildTask(notifyTimes - 1);

        // 调用
        LocalDateTime before = LocalDateTime.now();
        Integer status = invokeFailure(task, invokeException);
        LocalDateTime after = LocalDateTime.now();
        // 断言：未超过最大通知次数，按照间隔安排下一次通知
        Integer expectedStatus = invokeException ? PayNotifyStatusEnum.REQUEST_FAILURE.getStatus()
                : PayNotifyStatusEnum.REQUEST_SUCCESS.getStatus();
        assertEquals(expectedStatus, status);
        PayNotifyTaskDO updateTask = captureUpdateTask();
        assertEquals(TASK_ID, updateTask.getId());
        assertEquals(notifyTimes, updateTask.getNotifyTimes());
        assertEquals(expectedStatus, updateTask.getStatus());
        assertBetween(before, after, updateTask.getLastExecuteTime());
        assertBetween(before.plusSeconds(expectedIntervalSeconds), after.plusSeconds(expectedIntervalSeconds),
                updateTask.getNextNotifyTime());
    }

    /**
     * 第 1 ~ 8 次通知失败后的通知间隔（秒），与业务约定的 15s、15s、30s、3m、30m、30m、30m、1h 一致
     */
    private static Stream<Arguments> retryIntervals() {
        Stream<Arguments> intervals = Stream.of(
                Arguments.of(1, 15L), Arguments.of(2, 15L), Arguments.of(3, 30L), Arguments.of(4, 180L),
                Arguments.of(5, 1800L), Arguments.of(6, 1800L), Arguments.of(7, 1800L), Arguments.of(8, 3600L));
        // 分别覆盖：业务返回失败、调用异常
        return intervals.flatMap(arguments -> Stream.of(false, true)
                .map(invokeException -> Arguments.of(arguments.get()[0], arguments.get()[1], invokeException)));
    }

    @ParameterizedTest(name = "第 9 次通知失败（异常：{0}），标记为通知失败")
    @ValueSource(booleans = {false, true})
    public void testProcessNotifyResult_lastFailure(boolean invokeException) {
        // 准备参数：本次是第 9 次（最后一次）通知
        PayNotifyTaskDO task = buildTask(MAX_NOTIFY_TIMES - 1);

        // 调用
        LocalDateTime before = LocalDateTime.now();
        Integer status = invokeFailure(task, invokeException);
        LocalDateTime after = LocalDateTime.now();
        // 断言：标记为通知失败，不再安排下一次通知
        assertEquals(PayNotifyStatusEnum.FAILURE.getStatus(), status);
        PayNotifyTaskDO updateTask = captureUpdateTask();
        assertEquals(TASK_ID, updateTask.getId());
        assertEquals(MAX_NOTIFY_TIMES, updateTask.getNotifyTimes());
        assertEquals(PayNotifyStatusEnum.FAILURE.getStatus(), updateTask.getStatus());
        assertBetween(before, after, updateTask.getLastExecuteTime());
        assertNull(updateTask.getNextNotifyTime());
    }

    @ParameterizedTest(name = "第 {0} 次通知成功")
    @ValueSource(ints = {1, 8, MAX_NOTIFY_TIMES})
    public void testProcessNotifyResult_success(int notifyTimes) {
        // 准备参数：本次是第 notifyTimes 次通知
        PayNotifyTaskDO task = buildTask(notifyTimes - 1);

        // 调用
        LocalDateTime before = LocalDateTime.now();
        Integer status = notifyService.processNotifyResult(task, CommonResult.success(true), null);
        LocalDateTime after = LocalDateTime.now();
        // 断言：标记为通知成功，不再安排下一次通知
        assertEquals(PayNotifyStatusEnum.SUCCESS.getStatus(), status);
        PayNotifyTaskDO updateTask = captureUpdateTask();
        assertEquals(TASK_ID, updateTask.getId());
        assertEquals(notifyTimes, updateTask.getNotifyTimes());
        assertEquals(PayNotifyStatusEnum.SUCCESS.getStatus(), updateTask.getStatus());
        assertBetween(before, after, updateTask.getLastExecuteTime());
        assertNull(updateTask.getNextNotifyTime());
    }

    // ========== 辅助方法 ==========

    /**
     * 构建通知任务
     *
     * @param notifyTimes 已经通知的次数
     * @return 通知任务
     */
    private static PayNotifyTaskDO buildTask(int notifyTimes) {
        return new PayNotifyTaskDO().setId(TASK_ID).setNotifyTimes(notifyTimes).setMaxNotifyTimes(MAX_NOTIFY_TIMES)
                .setStatus(notifyTimes == 0 ? PayNotifyStatusEnum.WAITING.getStatus()
                        : PayNotifyStatusEnum.REQUEST_FAILURE.getStatus());
    }

    /**
     * 处理一次失败的通知结果
     *
     * @param task 通知任务
     * @param invokeException true：调用异常；false：业务返回失败
     * @return 最终任务的状态
     */
    private Integer invokeFailure(PayNotifyTaskDO task, boolean invokeException) {
        if (invokeException) {
            return notifyService.processNotifyResult(task, null, new RuntimeException("模拟请求超时"));
        }
        return notifyService.processNotifyResult(task, CommonResult.error(500, "模拟业务处理失败"), null);
    }

    /**
     * 断言：只更新了一次通知任务，并返回更新的内容
     */
    private PayNotifyTaskDO captureUpdateTask() {
        ArgumentCaptor<PayNotifyTaskDO> updateTaskCaptor = ArgumentCaptor.forClass(PayNotifyTaskDO.class);
        verify(notifyTaskMapper).updateById(updateTaskCaptor.capture());
        verifyNoMoreInteractions(notifyTaskMapper);
        return updateTaskCaptor.getValue();
    }

    private static void assertBetween(LocalDateTime expectedMin, LocalDateTime expectedMax, LocalDateTime actual) {
        assertNotNull(actual);
        assertFalse(actual.isBefore(expectedMin), () -> actual + " 早于 " + expectedMin);
        assertFalse(actual.isAfter(expectedMax), () -> actual + " 晚于 " + expectedMax);
    }

}
