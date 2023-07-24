package cn.iocoder.yudao.module.pay.service.notify;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;

import java.time.Duration;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
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
