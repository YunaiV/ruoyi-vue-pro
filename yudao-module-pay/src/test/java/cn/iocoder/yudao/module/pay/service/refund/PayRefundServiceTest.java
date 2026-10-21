package cn.iocoder.yudao.module.pay.service.refund;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.dal.redis.refund.PayRefundLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link PayRefundServiceImpl} 的单元测试类
 *
 * @author 芋艿
 */
@Import({PayRefundServiceImpl.class, PayNoRedisDAO.class, PayRefundLockRedisDAO.class})
public class PayRefundServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private PayRefundServiceImpl refundService;

    @Resource
    private PayRefundMapper refundMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private PayProperties payProperties;
    @MockitoBean
    private PayOrderService orderService;
    @MockitoBean
    private PayAppService appService;
    @MockitoBean
    private PayChannelService channelService;
    @MockitoBean
    private PayNotifyService notifyService;

    @BeforeEach
    public void setUp() {
        when(payProperties.getRefundNotifyUrl()).thenReturn("http://127.0.0.1");
    }

    @Test
    public void testGetRefund() {
        // mock 数据
        PayRefundDO refund = randomPojo(PayRefundDO.class);
        refundMapper.insert(refund);
        // 准备参数
        Long id = refund.getId();

        // 调用
        PayRefundDO dbRefund = refundService.getRefund(id);
        // 断言
        assertPojoEquals(dbRefund, refund);
    }

    @Test
    public void testGetRefundCountByAppId() {
        // mock 数据
        PayRefundDO refund01 = randomPojo(PayRefundDO.class);
        refundMapper.insert(refund01);
        PayRefundDO refund02 = randomPojo(PayRefundDO.class);
        refundMapper.insert(refund02);
        // 准备参数
        Long appId = refund01.getAppId();

        // 调用
        Long count = refundService.getRefundCountByAppId(appId);
        // 断言
        assertEquals(count, 1);
    }

    @Test
    public void testGetRefundPage() {
        // mock 数据
        PayRefundDO dbRefund = randomPojo(PayRefundDO.class, o -> { // 等会查询到
            o.setAppId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setMerchantOrderId("MOT0000001");
            o.setMerchantRefundId("MRF0000001");
            o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
            o.setChannelOrderNo("CH0000001");
            o.setChannelRefundNo("CHR0000001");
            o.setCreateTime(buildTime(2021, 1, 10));
        });
        refundMapper.insert(dbRefund);
        // 测试 appId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setAppId(2L)));
        // 测试 channelCode 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantOrderId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantOrderId(randomString())));
        // 测试 merchantRefundId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantRefundId(randomString())));
        // 测试 channelOrderNo 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelOrderNo(randomString())));
        // 测试 channelRefundNo 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelRefundNo(randomString())));
        // 测试 status 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())));
        // 测试 createTime 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        // 准备参数
        PayRefundPageReqVO reqVO = new PayRefundPageReqVO();
        reqVO.setAppId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantOrderId("MOT0000001");
        reqVO.setMerchantRefundId("MRF0000001");
        reqVO.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        reqVO.setChannelOrderNo("CH0000001");
        reqVO.setChannelRefundNo("CHR0000001");
        reqVO.setCreateTime(buildBetweenTime(2021, 1, 9, 2021, 1, 11));

        // 调用
        PageResult<PayRefundDO> pageResult = refundService.getRefundPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbRefund, pageResult.getList().get(0));
    }

    @Test
    public void testGetRefundList() {
        // mock 数据
        PayRefundDO dbRefund = randomPojo(PayRefundDO.class, o -> { // 等会查询到
            o.setAppId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setMerchantOrderId("MOT0000001");
            o.setMerchantRefundId("MRF0000001");
            o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
            o.setChannelOrderNo("CH0000001");
            o.setChannelRefundNo("CHR0000001");
            o.setCreateTime(buildTime(2021, 1, 10));
        });
        refundMapper.insert(dbRefund);
        // 测试 appId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setAppId(2L)));
        // 测试 channelCode 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantOrderId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantOrderId(randomString())));
        // 测试 merchantRefundId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantRefundId(randomString())));
        // 测试 channelOrderNo 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelOrderNo(randomString())));
        // 测试 channelRefundNo 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelRefundNo(randomString())));
        // 测试 status 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())));
        // 测试 createTime 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        // 准备参数
        PayRefundExportReqVO reqVO = new PayRefundExportReqVO();
        reqVO.setAppId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantOrderId("MOT0000001");
        reqVO.setMerchantRefundId("MRF0000001");
        reqVO.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        reqVO.setChannelOrderNo("CH0000001");
        reqVO.setChannelRefundNo("CHR0000001");
        reqVO.setCreateTime(buildBetweenTime(2021, 1, 9, 2021, 1, 11));

        // 调用
        List<PayRefundDO> list = refundService.getRefundList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbRefund, list.get(0));
    }

    @Test
    public void testCreateRefund_orderNotFound() {
        PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                o -> o.setAppKey("demo"));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createRefund(reqDTO),
                PAY_ORDER_NOT_FOUND);
    }

    @Test
    public void testCreateRefund_orderWaiting() {
        testCreateRefund_orderWaitingOrClosed(PayOrderStatusEnum.WAITING.getStatus());
    }

    @Test
    public void testCreateRefund_orderClosed() {
        testCreateRefund_orderWaitingOrClosed(PayOrderStatusEnum.CLOSED.getStatus());
    }

    private void testCreateRefund_orderWaitingOrClosed(Integer status) {
        // 准备参数
        PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("100"));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(status));
        when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createRefund(reqDTO),
                PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
    }

    @Test
    public void testCreateRefund_refundPriceExceed() {
        // 准备参数
        PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("100").setPrice(10));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o ->
                o.setStatus(PayOrderStatusEnum.REFUND.getStatus())
                        .setPrice(10).setRefundPrice(1));
        when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createRefund(reqDTO),
                REFUND_PRICE_EXCEED);
    }

    @Test
    public void testCreateRefund_orderHasRefunding() {
        // 准备参数
        PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("100").setPrice(10));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o ->
                o.setStatus(PayOrderStatusEnum.REFUND.getStatus())
                        .setPrice(10).setRefundPrice(1));
        when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);
        // mock 数据（refund 在退款中）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o ->
                o.setOrderId(order.getId()).setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        refundMapper.insert(refund);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createRefund(reqDTO),
                REFUND_PRICE_EXCEED);
    }

    @Test
    public void testCreateRefund_channelNotFound() {
        // 准备参数
        PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("100").setPrice(9));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o ->
                o.setStatus(PayOrderStatusEnum.REFUND.getStatus())
                        .setPrice(10).setRefundPrice(1)
                        .setChannelId(1L).setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);
        // mock 方法（channel）
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L)
                .setCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(channelService.validPayChannel(eq(1L))).thenReturn(channel);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createRefund(reqDTO),
                CHANNEL_NOT_FOUND);
    }

    @Test
    public void testCreateRefund_refundExists() {
        // 准备参数
        PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("100").setPrice(9)
                        .setMerchantRefundId("200").setReason("测试退款"));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o ->
                o.setStatus(PayOrderStatusEnum.REFUND.getStatus())
                        .setPrice(10).setRefundPrice(1)
                        .setChannelId(1L).setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);
        // mock 方法（channel）
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L)
                .setCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(channelService.validPayChannel(eq(1L))).thenReturn(channel);
        // mock 方法（client）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 数据（refund 已存在，且不是退款失败）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o ->
                o.setAppId(1L).setMerchantRefundId("200").setStatus(PayRefundStatusEnum.SUCCESS.getStatus()));
        refundMapper.insert(refund);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createRefund(reqDTO),
                REFUND_EXISTS);
    }

    @Test
    public void testCreateRefund_invokeException() {
        // 准备参数
        PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("100").setPrice(9)
                        .setMerchantRefundId("200").setReason("测试退款"));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o ->
                o.setStatus(PayOrderStatusEnum.REFUND.getStatus())
                        .setPrice(10).setRefundPrice(1)
                        .setChannelId(10L).setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);
        // mock 方法（channel）
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L)
                .setCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(channelService.validPayChannel(eq(10L))).thenReturn(channel);
        // mock 方法（client）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（client 调用发生异常）
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenThrow(new RuntimeException());

        // 调用
        Long refundId = refundService.createRefund(reqDTO);
        // 断言
        PayRefundDO refundDO = refundMapper.selectById(refundId);
        assertPojoEquals(reqDTO, refundDO);
        assertNotNull(refundDO.getNo());
        assertThat(refundDO)
                .extracting("orderId", "orderNo", "channelId", "channelCode",
                        "notifyUrl", "channelOrderNo", "status", "payPrice", "refundPrice")
                .containsExactly(order.getId(), order.getNo(), channel.getId(), channel.getCode(),
                        app.getRefundNotifyUrl(), order.getChannelOrderNo(), PayRefundStatusEnum.WAITING.getStatus(),
                        order.getPrice(), reqDTO.getPrice());
        // 断言：查询一次渠道退款单，查询不到结果时保留 WAITING
        verify(client).getRefund(eq(order.getNo()), eq(refundDO.getNo()));
    }

    @Test // 调用 unifiedRefund 接口发生异常，但查询确认渠道退款成功
    public void testCreateRefund_invokeException_querySuccess() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getRefund(any(), any())).thenAnswer(invocation -> PayRefundRespDTO.successOf("CHANNEL_R001",
                LocalDateTime.now(), invocation.getArgument(1), "success"));

        // 调用
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言：按查询结果更新为退款成功
        PayRefundDO refund = refundMapper.selectById(refundId);
        assertEquals(PayRefundStatusEnum.SUCCESS.getStatus(), refund.getStatus());
        assertEquals("CHANNEL_R001", refund.getChannelRefundNo());
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.REFUND.getType()), eq(refundId));
    }

    @Test // 调用 unifiedRefund 接口发生异常，查询返回失败（例如说：微信查询不到退款单）
    public void testCreateRefund_invokeException_queryFailure() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getRefund(any(), any())).thenAnswer(invocation -> PayRefundRespDTO.failureOf(
                "REFUND_NOT_EXISTS", "退款单不存在", invocation.getArgument(1), "failure"));

        // 调用：一次查询的失败（可能是渠道退款单尚未生成），不作为最终结果，保留 WAITING 交给退款轮询
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言
        PayRefundDO refund = refundMapper.selectById(refundId);
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refund.getStatus());
        assertNull(refund.getChannelErrorCode());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedRefund 接口发生异常，查询返回 WAITING 且无渠道退款单号（例如说：支付宝查询不到退款单）
    public void testCreateRefund_invokeException_queryWaitingWithoutChannelRefundNo() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getRefund(any(), any())).thenAnswer(invocation -> PayRefundRespDTO.waitingOf(
                null, invocation.getArgument(1), "waiting"));

        // 调用：结果未知，保留 WAITING 交给退款轮询
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refundMapper.selectById(refundId).getStatus());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedRefund 接口发生异常，查询渠道退款单也发生异常
    public void testCreateRefund_invokeException_queryException() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getRefund(any(), any())).thenThrow(new RuntimeException("模拟查询超时"));

        // 调用：结果未知，保留 WAITING 交给退款轮询
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refundMapper.selectById(refundId).getStatus());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedRefund 接口返回失败，查询也确认渠道退款失败
    public void testCreateRefund_invokeFailure_queryFailure() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        when(client.getRefund(any(), any())).thenAnswer(invocation -> PayRefundRespDTO.failureOf(
                "REFUND_NOT_EXISTS", "退款单不存在", invocation.getArgument(1), "failure"));

        // 调用，并断言异常：以发起退款的失败原因为准
        assertServiceException(() -> refundService.createRefund(buildRefundCreateReqDTO()),
                REFUND_SUBMIT_CHANNEL_ERROR, "BALANCE_NOT_ENOUGH", "余额不足");
        // 断言：退款单已更新为失败
        assertRefundFailure("BALANCE_NOT_ENOUGH");
    }

    @Test // 调用 unifiedRefund 接口返回失败，查询返回 WAITING 且无渠道退款单号（例如说：支付宝查询不到退款单）
    public void testCreateRefund_invokeFailure_queryWaitingWithoutChannelRefundNo() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        when(client.getRefund(any(), any())).thenAnswer(invocation -> PayRefundRespDTO.waitingOf(
                null, invocation.getArgument(1), "waiting"));

        // 调用，并断言异常：以发起退款的失败结果为准，避免一直 WAITING
        assertServiceException(() -> refundService.createRefund(buildRefundCreateReqDTO()),
                REFUND_SUBMIT_CHANNEL_ERROR, "BALANCE_NOT_ENOUGH", "余额不足");
        // 断言：退款单已更新为失败
        assertRefundFailure("BALANCE_NOT_ENOUGH");
    }

    @Test // 调用 unifiedRefund 接口返回失败，查询渠道退款单发生异常（例如说：钱包查询 WAITING 退款单）
    public void testCreateRefund_invokeFailure_queryException() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        when(client.getRefund(any(), any())).thenThrow(new RuntimeException("模拟查询异常"));

        // 调用，并断言异常：以发起退款的失败结果为准，避免一直 WAITING
        assertServiceException(() -> refundService.createRefund(buildRefundCreateReqDTO()),
                REFUND_SUBMIT_CHANNEL_ERROR, "BALANCE_NOT_ENOUGH", "余额不足");
        // 断言：退款单已更新为失败
        assertRefundFailure("BALANCE_NOT_ENOUGH");
    }

    @Test // 调用 unifiedRefund 接口返回失败，但查询到渠道退款单处理中
    public void testCreateRefund_invokeFailure_queryProcessing() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        when(client.getRefund(any(), any())).thenAnswer(invocation -> PayRefundRespDTO.waitingOf(
                "CHANNEL_R001", invocation.getArgument(1), "processing"));

        // 调用：渠道实际已受理退款，保留 WAITING 交给退款回调、或者退款轮询
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言：保留渠道已受理的退款单号
        PayRefundDO refund = refundMapper.selectById(refundId);
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refund.getStatus());
        assertEquals("CHANNEL_R001", refund.getChannelRefundNo());
        assertNull(refund.getChannelErrorCode());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedRefund 接口返回退款中，并返回渠道退款单号（例如说：微信退款受理）
    public void testCreateRefund_invokeWaitingWithChannelRefundNo() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenAnswer(invocation -> PayRefundRespDTO.waitingOf(
                "CHANNEL_R001", ((PayRefundUnifiedReqDTO) invocation.getArgument(0)).getOutRefundNo(), "processing"));

        // 调用
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言：仍是 WAITING，并保存渠道退款单号、渠道返回数据
        PayRefundDO refund = refundMapper.selectById(refundId);
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refund.getStatus());
        assertEquals("CHANNEL_R001", refund.getChannelRefundNo());
        assertNotNull(refund.getChannelNotifyData());
        verify(client, never()).getRefund(any(), any());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 退款中的通知晚于退款成功到达，不能覆盖已成功的退款单
    public void testNotifyRefund_waitingWhenSuccess() {
        // mock 数据（refund 已退款成功）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setNo("R001")
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus()).setChannelRefundNo("CHANNEL_R001"));
        refundMapper.insert(refund);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setAppId(1L));

        // 调用
        refundService.notifyRefund(channel, PayRefundRespDTO.waitingOf("CHANNEL_R002", "R001", "processing"));
        // 断言：不更新
        PayRefundDO dbRefund = refundMapper.selectById(refund.getId());
        assertEquals(PayRefundStatusEnum.SUCCESS.getStatus(), dbRefund.getStatus());
        assertEquals("CHANNEL_R001", dbRefund.getChannelRefundNo());
        assertEquals(refund.getChannelNotifyData(), dbRefund.getChannelNotifyData());
    }

    @Test // 并发使用相同的商户退款编号发起退款，只能创建一个退款单，避免重复退款
    public void testCreateRefund_concurrentCreate() throws Exception {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenAnswer(invocation -> PayRefundRespDTO.waitingOf(
                "CHANNEL_R001", ((PayRefundUnifiedReqDTO) invocation.getArgument(0)).getOutRefundNo(), "processing"));
        // mock 方法（mapper）：查询后等待一段时间，放大 select 后 insert 的时间窗口
        PayRefundMapper refundMapperSpy = mock(PayRefundMapper.class, delegatesTo(refundMapper));
        doAnswer(invocation -> {
            Long count = refundMapper.selectCountByAppIdAndOrderId(invocation.getArgument(0),
                    invocation.getArgument(1), invocation.getArgument(2));
            Thread.sleep(300);
            return count;
        }).when(refundMapperSpy).selectCountByAppIdAndOrderId(any(), any(), any());
        Object target = AopTestUtils.getTargetObject(refundService);
        ReflectionTestUtils.setField(target, "refundMapper", refundMapperSpy);
        List<Throwable> exceptions = new ArrayList<>();
        try {
            // 调用：两个请求并发发起退款
            List<CompletableFuture<Long>> futures = List.of(
                    CompletableFuture.supplyAsync(() -> refundService.createRefund(buildRefundCreateReqDTO())),
                    CompletableFuture.supplyAsync(() -> refundService.createRefund(buildRefundCreateReqDTO())));
            for (CompletableFuture<Long> future : futures) {
                try {
                    future.get();
                } catch (Exception ex) {
                    exceptions.add(ex.getCause());
                }
            }
        } finally {
            ReflectionTestUtils.setField(target, "refundMapper", refundMapper);
        }
        // 断言：只有一个请求创建退款单，另一个请求提示存在退款中的退款单
        assertEquals(1, exceptions.size());
        assertEquals(REFUND_HAS_REFUNDING.getCode(), ((ServiceException) exceptions.get(0)).getCode());
        assertEquals(1, refundMapper.selectList().size());
        verify(client, times(1)).unifiedRefund(any());
    }

    @Test // 调用 unifiedRefund 接口返回失败，但查询确认渠道退款成功
    public void testCreateRefund_invokeFailure_querySuccess() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        when(client.getRefund(any(), any())).thenAnswer(invocation -> PayRefundRespDTO.successOf("CHANNEL_R001",
                LocalDateTime.now(), invocation.getArgument(1), "success"));

        // 调用
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言：按查询结果更新为退款成功
        PayRefundDO refund = refundMapper.selectById(refundId);
        assertEquals(PayRefundStatusEnum.SUCCESS.getStatus(), refund.getStatus());
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.REFUND.getType()), eq(refundId));
    }

    @Test // 调用 unifiedRefund 接口返回成功，但处理退款结果时发生异常
    public void testCreateRefund_invokeSuccess_notifyException() {
        // mock 方法（client）：返回不存在的退款单号，使 notifyRefund 抛出异常
        PayClient<?> client = mockPayClient();
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenReturn(PayRefundRespDTO.successOf(
                "CHANNEL_R001", LocalDateTime.now(), "NOT_EXISTS", "success"));

        // 调用：渠道已退款成功，不向调用方抛出异常，避免调用方回滚退款单
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refundMapper.selectById(refundId).getStatus());
        verify(client, never()).getRefund(any(), any());
    }

    @Test // 调用 unifiedRefund 接口返回存在渠道错误，但退款单已被并发更新为成功（例如说：退款回调）
    public void testCreateRefund_channelErrorWhenConcurrentSuccess() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        when(client.getRefund(any(), any())).thenAnswer(invocation -> {
            // 模拟：退款回调并发更新退款单为退款成功
            PayRefundDO refund = refundMapper.selectByNo(invocation.getArgument(1));
            refundMapper.updateById(new PayRefundDO().setId(refund.getId())
                    .setStatus(PayRefundStatusEnum.SUCCESS.getStatus()));
            return PayRefundRespDTO.failureOf("REFUND_NOT_EXISTS", "退款单不存在", invocation.getArgument(1), "failure");
        });

        // 调用：以最新的退款成功为准，不向调用方误报失败
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言
        assertEquals(PayRefundStatusEnum.SUCCESS.getStatus(), refundMapper.selectById(refundId).getStatus());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedRefund 接口返回存在渠道错误，通知时发生状态竞争，但退款单仍是待退款
    public void testCreateRefund_channelErrorWhenConcurrentWaiting() {
        PayRefundServiceImpl payRefundServiceImpl = mock(PayRefundServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayRefundServiceImpl.class)))
                    .thenReturn(payRefundServiceImpl);
            doThrow(exception(REFUND_STATUS_IS_NOT_WAITING))
                    .when(payRefundServiceImpl).notifyRefund(any(PayChannelDO.class), any());
            // mock 方法（client）
            PayClient<?> client = mockPayClient();
            mockUnifiedRefundFailure(client);

            // 调用，并断言异常：无法确认最新状态，抛出原通知异常
            assertServiceException(() -> refundService.createRefund(buildRefundCreateReqDTO()),
                    REFUND_STATUS_IS_NOT_WAITING);
            // 断言
            PayRefundDO refund = refundMapper.selectByAppIdAndMerchantRefundId(1L, "200");
            assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refund.getStatus());
        }
    }

    @Test // 调用 unifiedRefund 接口返回存在渠道错误，但创建退款通知任务时发生异常
    public void testCreateRefund_channelErrorAndNotifyTaskException() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        // mock 方法（notify）
        RuntimeException notifyTaskException = new RuntimeException("模拟创建通知任务异常");
        doThrow(notifyTaskException).when(notifyService).createPayNotifyTask(any(), any());

        // 调用，并断言异常：抛出真实的通知异常，而不是渠道错误
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> refundService.createRefund(buildRefundCreateReqDTO()));
        assertSame(notifyTaskException, ex);
        // 断言：失败状态随通知事务回滚
        PayRefundDO refund = refundMapper.selectByAppIdAndMerchantRefundId(1L, "200");
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refund.getStatus());
        assertNull(refund.getChannelErrorCode());
    }

    @Test // 调用 unifiedRefund 接口，返回失败，但不存在渠道错误
    public void testCreateRefund_failureWithoutChannelError() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenAnswer(invocation ->
                PayRefundRespDTO.failureOf(null, null,
                        ((PayRefundUnifiedReqDTO) invocation.getArgument(0)).getOutRefundNo(), "failure"));

        // 调用：不抛出异常，由退款通知告知调用方
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言
        assertEquals(PayRefundStatusEnum.FAILURE.getStatus(), refundMapper.selectById(refundId).getStatus());
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.REFUND.getType()), eq(refundId));
    }

    @Test // 退款失败（例如说：余额不足）后，使用相同的商户退款编号再次发起，退款成功
    public void testCreateRefund_retryAfterFailure_success() {
        // 准备参数：首次发起退款失败
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        when(client.getRefund(any(), any())).thenAnswer(invocation -> PayRefundRespDTO.failureOf(
                "REFUND_NOT_EXISTS", "退款单不存在", invocation.getArgument(1), "failure"));
        assertServiceException(() -> refundService.createRefund(buildRefundCreateReqDTO()),
                REFUND_SUBMIT_CHANNEL_ERROR, "BALANCE_NOT_ENOUGH", "余额不足");
        PayRefundDO failureRefund = refundMapper.selectByAppIdAndMerchantRefundId(1L, "200");
        // mock 方法（client 再次发起时，退款成功）
        doAnswer(invocation -> PayRefundRespDTO.successOf("CHANNEL_R001", LocalDateTime.now(),
                ((PayRefundUnifiedReqDTO) invocation.getArgument(0)).getOutRefundNo(), "success"))
                .when(client).unifiedRefund(any(PayRefundUnifiedReqDTO.class));

        // 调用
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO().setReason("再次退款"));
        // 断言：复用原退款单及原退款单号，并清空上一次的失败原因
        assertEquals(failureRefund.getId(), refundId);
        assertEquals(1, refundMapper.selectList().size());
        PayRefundDO refund = refundMapper.selectById(refundId);
        assertEquals(failureRefund.getNo(), refund.getNo());
        assertEquals(PayRefundStatusEnum.SUCCESS.getStatus(), refund.getStatus());
        assertEquals("CHANNEL_R001", refund.getChannelRefundNo());
        assertEquals("再次退款", refund.getReason());
        assertNull(refund.getChannelErrorCode());
        assertNull(refund.getChannelErrorMsg());
        // 断言：两次都使用原退款单号向渠道发起退款，并分别通知退款失败、退款成功
        verify(client, times(2)).unifiedRefund(argThat(unifiedReqDTO ->
                failureRefund.getNo().equals(unifiedReqDTO.getOutRefundNo())));
        verify(notifyService, times(2)).createPayNotifyTask(eq(PayNotifyTypeEnum.REFUND.getType()), eq(refundId));
    }

    @Test // 退款失败后再次发起，渠道结果未知，保留 WAITING 并清空上一次的渠道结果
    public void testCreateRefund_retryAfterFailure_unknown() {
        // 准备参数：已存在退款失败的退款单
        PayClient<?> client = mockPayClient();
        PayRefundDO failureRefund = insertFailureRefund(9);
        // mock 方法（client 调用发生异常，查询也发生异常）
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getRefund(any(), any())).thenThrow(new RuntimeException("模拟查询超时"));

        // 调用
        Long refundId = refundService.createRefund(buildRefundCreateReqDTO());
        // 断言：复用原退款单号，状态重置为 WAITING，交给退款回调、或者退款轮询
        assertEquals(failureRefund.getId(), refundId);
        PayRefundDO refund = refundMapper.selectById(refundId);
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refund.getStatus());
        assertNull(refund.getChannelRefundNo());
        assertNull(refund.getChannelErrorCode());
        assertNull(refund.getChannelErrorMsg());
        assertNull(refund.getChannelNotifyData());
        verify(client).getRefund(eq(failureRefund.getOrderNo()), eq(failureRefund.getNo()));
    }

    @Test // 退款失败后再次发起，但退款单已被并发重新发起
    public void testCreateRefund_retryAfterFailure_concurrentRetry() {
        // 准备参数：已存在退款失败的退款单
        PayClient<?> client = mockPayClient();
        PayRefundDO failureRefund = insertFailureRefund(9);
        // mock 方法（mapper）：校验通过后、更新前，其它请求已将退款单重新发起为 WAITING
        PayRefundMapper refundMapperSpy = mock(PayRefundMapper.class, delegatesTo(refundMapper));
        doAnswer(invocation -> {
            refundMapper.updateById(new PayRefundDO().setId(failureRefund.getId())
                    .setStatus(PayRefundStatusEnum.WAITING.getStatus()));
            return refundMapper.updateByIdAndStatusAndClearChannelResult(invocation.getArgument(0),
                    invocation.getArgument(1), invocation.getArgument(2));
        }).when(refundMapperSpy).updateByIdAndStatusAndClearChannelResult(any(), any(), any());
        Object target = AopTestUtils.getTargetObject(refundService);
        ReflectionTestUtils.setField(target, "refundMapper", refundMapperSpy);
        try {
            // 调用，并断言异常
            assertServiceException(() -> refundService.createRefund(buildRefundCreateReqDTO()),
                    REFUND_HAS_REFUNDING);
        } finally {
            ReflectionTestUtils.setField(target, "refundMapper", refundMapper);
        }
        // 断言：不会再次向渠道发起退款
        verify(client, never()).unifiedRefund(any());
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), refundMapper.selectById(failureRefund.getId()).getStatus());
    }

    @Test // 调用方存在事务，退款失败后调用方事务回滚，退款失败的结果仍需保存，并可使用原退款单号重新发起
    public void testCreateRefund_outerTransactionRollback() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        mockUnifiedRefundFailure(client);
        PayRefundDO outerRefund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setMerchantRefundId("300"));

        // 调用：调用方事务中，先写入自己的数据，再发起退款，渠道错误导致调用方事务回滚
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        assertServiceException(() -> transactionTemplate.executeWithoutResult(status -> {
            refundMapper.insert(outerRefund);
            refundService.createRefund(buildRefundCreateReqDTO());
        }), REFUND_SUBMIT_CHANNEL_ERROR, "BALANCE_NOT_ENOUGH", "余额不足");
        // 断言：调用方事务已回滚，但退款失败的结果（包括退款通知）已保存
        assertNull(refundMapper.selectById(outerRefund.getId()));
        assertRefundFailure("BALANCE_NOT_ENOUGH");
        PayRefundDO failureRefund = refundMapper.selectByAppIdAndMerchantRefundId(1L, "200");

        // mock 方法（client 再次发起时，退款成功）
        doAnswer(invocation -> PayRefundRespDTO.successOf("CHANNEL_R001", LocalDateTime.now(),
                ((PayRefundUnifiedReqDTO) invocation.getArgument(0)).getOutRefundNo(), "success"))
                .when(client).unifiedRefund(any(PayRefundUnifiedReqDTO.class));
        // 调用：使用相同的商户退款编号再次发起
        Long refundId = transactionTemplate.execute(status -> refundService.createRefund(buildRefundCreateReqDTO()));
        // 断言：复用原退款单号
        assertEquals(failureRefund.getId(), refundId);
        PayRefundDO refund = refundMapper.selectById(refundId);
        assertEquals(failureRefund.getNo(), refund.getNo());
        assertEquals(PayRefundStatusEnum.SUCCESS.getStatus(), refund.getStatus());
    }

    @Test // 退款失败后再次发起，但支付订单不一致
    public void testCreateRefund_retryAfterFailure_orderNotMatch() {
        // 准备参数：已存在退款失败的退款单，关联其它支付订单
        PayClient<?> client = mockPayClient();
        PayRefundDO failureRefund = insertFailureRefund(9);
        refundMapper.updateById(new PayRefundDO().setId(failureRefund.getId()).setOrderId(-1L));

        // 调用，并断言异常
        assertServiceException(() -> refundService.createRefund(buildRefundCreateReqDTO()),
                REFUND_CREATE_FAIL_ORDER_NOT_MATCH);
        assertEquals(PayRefundStatusEnum.FAILURE.getStatus(), refundMapper.selectById(failureRefund.getId()).getStatus());
        verify(client, never()).unifiedRefund(any());
    }

    @Test // 退款失败后再次发起，但退款金额不一致
    public void testCreateRefund_retryAfterFailure_priceNotMatch() {
        // 准备参数：已存在退款金额为 5 的退款失败的退款单
        PayClient<?> client = mockPayClient();
        PayRefundDO failureRefund = insertFailureRefund(5);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createRefund(buildRefundCreateReqDTO()),
                REFUND_CREATE_FAIL_PRICE_NOT_MATCH);
        assertEquals(PayRefundStatusEnum.FAILURE.getStatus(), refundMapper.selectById(failureRefund.getId()).getStatus());
        verify(client, never()).unifiedRefund(any());
    }

    @Test
    public void testCreateRefund_invokeSuccess() {
        PayRefundServiceImpl payRefundServiceImpl = mock(PayRefundServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayRefundServiceImpl.class)))
                    .thenReturn(payRefundServiceImpl);

            // 准备参数
            PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                    o -> o.setAppKey("demo").setMerchantOrderId("100").setPrice(9)
                            .setMerchantRefundId("200").setReason("测试退款"));
            // mock 方法（app）
            PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
            when(appService.validPayApp(eq("demo"))).thenReturn(app);
            // mock 数据（order）
            PayOrderDO order = randomPojo(PayOrderDO.class, o ->
                    o.setStatus(PayOrderStatusEnum.REFUND.getStatus())
                            .setPrice(10).setRefundPrice(1)
                            .setChannelId(10L).setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
            when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);
            // mock 方法（channel）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L)
                    .setCode(PayChannelEnum.ALIPAY_APP.getCode()));
            when(channelService.validPayChannel(eq(10L))).thenReturn(channel);
            // mock 方法（client）
            PayClient<?> client = mock(PayClient.class);
            when(channelService.getPayClient(eq(10L))).thenReturn(client);
            // mock 方法（client 成功）
            PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class);
            when(client.unifiedRefund(argThat(unifiedReqDTO -> {
                assertNotNull(unifiedReqDTO.getOutRefundNo());
                assertThat(unifiedReqDTO)
                        .extracting("payPrice", "refundPrice", "outTradeNo",
                                 "notifyUrl", "reason")
                        .containsExactly(order.getPrice(), reqDTO.getPrice(), order.getNo(),
                                "http://127.0.0.1/10", reqDTO.getReason());
                return true;
            }))).thenReturn(refundRespDTO);

            // 调用
            Long refundId = refundService.createRefund(reqDTO);
            // 断言
            PayRefundDO refundDO = refundMapper.selectById(refundId);
            assertPojoEquals(reqDTO, refundDO);
            assertNotNull(refundDO.getNo());
            assertThat(refundDO)
                    .extracting("orderId", "orderNo", "channelId", "channelCode",
                            "notifyUrl", "channelOrderNo", "status", "payPrice", "refundPrice")
                    .containsExactly(order.getId(), order.getNo(), channel.getId(), channel.getCode(),
                            app.getRefundNotifyUrl(), order.getChannelOrderNo(), PayRefundStatusEnum.WAITING.getStatus(),
                            order.getPrice(), reqDTO.getPrice());
            // 断言调用
            verify(payRefundServiceImpl).notifyRefund(same(channel), same(refundRespDTO));
        }
    }

    @Test
    public void testNotifyRefund() {
        PayRefundServiceImpl payRefundServiceImpl = mock(PayRefundServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayRefundServiceImpl.class)))
                    .thenReturn(payRefundServiceImpl);

            // 准备参数
            Long channelId = 10L;
            PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class);
            // mock 方法（channel）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
            when(channelService.validPayChannel(eq(10L))).thenReturn(channel);

            // 调用
            refundService.notifyRefund(channelId, refundRespDTO);
            // 断言
            verify(payRefundServiceImpl).notifyRefund(same(channel), same(refundRespDTO));
        }
    }

    @Test
    public void testNotifyRefundSuccess_notFound() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));

        // 调用，并断言异常
        assertServiceException(() -> refundService.notifyRefund(channel, refundRespDTO),
                REFUND_NOT_FOUND);
    }

    @Test
    public void testNotifyRefundSuccess_isSuccess() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));
        // mock 数据（refund + 已支付）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setNo("R100")
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus()));
        refundMapper.insert(refund);

        // 调用
        refundService.notifyRefund(channel, refundRespDTO);
        // 断言，refund 没有更新，因为已经退款成功
        assertPojoEquals(refund, refundMapper.selectById(refund.getId()));
    }

    @Test
    public void testNotifyRefundSuccess_failure() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));
        // mock 数据（refund + 已支付）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setNo("R100")
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus()));
        refundMapper.insert(refund);

        // 调用，并断言异常
        assertServiceException(() -> refundService.notifyRefund(channel, refundRespDTO),
                REFUND_STATUS_IS_NOT_WAITING);
    }

    @Test
    public void testNotifyRefundSuccess_updateOrderException() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));
        // mock 数据（refund + 已支付）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setNo("R100")
                .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                .setOrderId(100L).setRefundPrice(23));
        refundMapper.insert(refund);
        // mock 方法（order + 更新异常）
        doThrow(new RuntimeException()).when(orderService)
                .updateOrderRefundPrice(eq(100L), eq(23));

        // 调用，并断言异常
        assertThrows(RuntimeException.class, () -> refundService.notifyRefund(channel, refundRespDTO));
        // 断言，refund 没有更新，因为事务回滚了
        assertPojoEquals(refund, refundMapper.selectById(refund.getId()));
    }

    @Test
    public void testNotifyRefundSuccess_success() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));
        // mock 数据（refund + 已支付）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setNo("R100")
                .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                .setOrderId(100L).setRefundPrice(23));
        refundMapper.insert(refund);

        // 调用
        refundService.notifyRefund(channel, refundRespDTO);
        // 断言，refund
        refund.setSuccessTime(refundRespDTO.getSuccessTime())
                .setChannelRefundNo(refundRespDTO.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setChannelNotifyData(toJsonString(refundRespDTO));
        assertPojoEquals(refund, refundMapper.selectById(refund.getId()),
                "updateTime", "updater");
        // 断言，调用
        verify(orderService).updateOrderRefundPrice(eq(100L), eq(23));
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.REFUND.getType()),
                eq(refund.getId()));
    }

    @Test
    public void testNotifyRefundFailure_notFound() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.FAILURE.getStatus()).setOutRefundNo("R100"));

        // 调用，并断言异常
        assertServiceException(() -> refundService.notifyRefund(channel, refundRespDTO),
                REFUND_NOT_FOUND);
    }

    @Test
    public void testNotifyRefundFailure_isFailure() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.FAILURE.getStatus()).setOutRefundNo("R100"));
        // mock 数据（refund + 退款失败）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setNo("R100")
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus()));
        refundMapper.insert(refund);

        // 调用
        refundService.notifyRefund(channel, refundRespDTO);
        // 断言，refund 没有更新，因为已经退款失败
        assertPojoEquals(refund, refundMapper.selectById(refund.getId()));
    }

    @Test
    public void testNotifyRefundFailure_isSuccess() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.FAILURE.getStatus()).setOutRefundNo("R100"));
        // mock 数据（refund + 已支付）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setNo("R100")
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus()));
        refundMapper.insert(refund);

        // 调用，并断言异常
        assertServiceException(() -> refundService.notifyRefund(channel, refundRespDTO),
                REFUND_STATUS_IS_NOT_WAITING);
    }

    @Test
    public void testNotifyRefundFailure_success() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusEnum.FAILURE.getStatus()).setOutRefundNo("R100"));
        // mock 数据（refund + 已支付）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setNo("R100")
                .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                .setOrderId(100L).setRefundPrice(23));
        refundMapper.insert(refund);

        // 调用
        refundService.notifyRefund(channel, refundRespDTO);
        // 断言，refund
        refund.setChannelRefundNo(refundRespDTO.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus())
                .setChannelNotifyData(toJsonString(refundRespDTO))
                .setChannelErrorCode(refundRespDTO.getChannelErrorCode())
                .setChannelErrorMsg(refundRespDTO.getChannelErrorMsg());
        assertPojoEquals(refund, refundMapper.selectById(refund.getId()),
                "updateTime", "updater");
        // 断言，调用
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.REFUND.getType()),
                eq(refund.getId()));
    }

    @Test
    public void testSyncRefund_notFound() {
        // 准备参数
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L)
                .setStatus(PayRefundStatusEnum.WAITING.getStatus()));
        refundMapper.insert(refund);

        // 调用
        int count = refundService.syncRefund();
        // 断言
        assertEquals(count, 0);
    }

    @Test
    public void testSyncRefund_waiting() {
        assertEquals(testSyncRefund_waitingOrSuccessOrFailure(PayRefundStatusEnum.WAITING.getStatus()), 0);
    }

    @Test
    public void testSyncRefund_success() {
        assertEquals(testSyncRefund_waitingOrSuccessOrFailure(PayRefundStatusEnum.SUCCESS.getStatus()), 1);
    }

    @Test
    public void testSyncRefund_failure() {
        assertEquals(testSyncRefund_waitingOrSuccessOrFailure(PayRefundStatusEnum.FAILURE.getStatus()), 1);
    }

    private int testSyncRefund_waitingOrSuccessOrFailure(Integer status) {
        PayRefundServiceImpl payRefundServiceImpl = mock(PayRefundServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayRefundServiceImpl.class)))
                    .thenReturn(payRefundServiceImpl);

            // 准备参数
            PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setChannelId(10L)
                    .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                    .setOrderNo("P110").setNo("R220"));
            refundMapper.insert(refund);
            // mock 方法（client）
            PayClient<?> client = mock(PayClient.class);
            when(channelService.getPayClient(eq(10L))).thenReturn(client);
            // mock 方法（client 返回指定状态）
            PayRefundRespDTO respDTO = randomPojo(PayRefundRespDTO.class, o -> o.setStatus(status));
            when(client.getRefund(eq("P110"), eq("R220"))).thenReturn(respDTO);
            // mock 方法（channel）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
            when(channelService.validPayChannel(eq(10L))).thenReturn(channel);

            // 调用
            return refundService.syncRefund();
        }
    }

    @Test
    public void testSyncRefund_exception() {
        // 准备参数
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setChannelId(10L)
                .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                .setOrderNo("P110").setNo("R220"));
        refundMapper.insert(refund);
        // mock 方法（client）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（client 抛出异常）
        when(client.getRefund(eq("P110"), eq("R220"))).thenThrow(new RuntimeException());

        // 调用
        int count = refundService.syncRefund();
        // 断言
        assertEquals(count, 0);
    }

    private PayClient<?> mockPayClient() {
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o ->
                o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setPrice(10).setRefundPrice(0)
                        .setChannelId(10L).setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);
        // mock 方法（channel）
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L)
                .setCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(channelService.validPayChannel(eq(10L))).thenReturn(channel);
        // mock 方法（client）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        return client;
    }

    private static void mockUnifiedRefundFailure(PayClient<?> client) {
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenAnswer(invocation -> {
            PayRefundUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayRefundRespDTO.failureOf("BALANCE_NOT_ENOUGH", "余额不足",
                    unifiedReqDTO.getOutRefundNo(), "failure");
        });
    }

    private static PayRefundCreateReqDTO buildRefundCreateReqDTO() {
        return new PayRefundCreateReqDTO().setAppKey("demo").setUserIp("127.0.0.1")
                .setUserId(1L).setUserType(1)
                .setMerchantOrderId("100").setMerchantRefundId("200")
                .setReason("测试退款").setPrice(9);
    }

    /**
     * 插入退款失败的退款单，关联 {@link #mockPayClient()} 的支付订单
     */
    private PayRefundDO insertFailureRefund(Integer refundPrice) {
        PayOrderDO order = orderService.getOrder(1L, "100");
        PayRefundDO refund = randomPojo(PayRefundDO.class, o -> o.setAppId(1L).setMerchantRefundId("200")
                .setOrderId(order.getId()).setOrderNo(order.getNo()).setRefundPrice(refundPrice)
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus())
                .setChannelErrorCode("BALANCE_NOT_ENOUGH").setChannelErrorMsg("余额不足"));
        refundMapper.insert(refund);
        return refund;
    }

    private void assertRefundFailure(String channelErrorCode) {
        PayRefundDO refund = refundMapper.selectByAppIdAndMerchantRefundId(1L, "200");
        assertEquals(PayRefundStatusEnum.FAILURE.getStatus(), refund.getStatus());
        assertEquals(channelErrorCode, refund.getChannelErrorCode());
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.REFUND.getType()), eq(refund.getId()));
    }

}
