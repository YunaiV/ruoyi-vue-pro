package cn.iocoder.yudao.module.pay.service.order;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderExtensionMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.dal.redis.order.PayOrderLockRedisDAO;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.exception.PayClientException;
import cn.iocoder.yudao.module.pay.framework.pay.core.enums.PayOrderDisplayModeEnum;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link PayOrderServiceImpl} 的单元测试类
 *
 * @author 芋艿
 */
@Import({PayOrderServiceImpl.class, PayNoRedisDAO.class, PayOrderLockRedisDAO.class})
public class PayOrderServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private PayOrderServiceImpl orderService;

    @Resource
    private PayOrderMapper orderMapper;
    @Resource
    private PayOrderExtensionMapper orderExtensionMapper;

    @MockitoBean
    private PayProperties properties;
    @MockitoBean
    private PayAppService appService;
    @MockitoBean
    private PayChannelService channelService;
    @MockitoBean
    private PayNotifyService notifyService;

    @BeforeEach
    public void setUp() {
        when(properties.getOrderNotifyUrl()).thenReturn("http://127.0.0.1");
    }

    @Test
    public void testGetOrder_id() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class);
        orderMapper.insert(order);
        // 准备参数
        Long id = order.getId();

        // 调用
        PayOrderDO dbOrder = orderService.getOrder(id);
        // 断言
        assertPojoEquals(dbOrder, order);
    }

    @Test
    public void testGetOrder_appIdAndMerchantOrderId() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class);
        orderMapper.insert(order);
        // 准备参数
        Long appId = order.getAppId();
        String merchantOrderId = order.getMerchantOrderId();

        // 调用
        PayOrderDO dbOrder = orderService.getOrder(appId, merchantOrderId);
        // 断言
        assertPojoEquals(dbOrder, order);
    }

    @Test
    public void testGetOrderCountByAppId() {
        // mock 数据（PayOrderDO）
        PayOrderDO order01 = randomPojo(PayOrderDO.class);
        orderMapper.insert(order01);
        PayOrderDO order02 = randomPojo(PayOrderDO.class);
        orderMapper.insert(order02);
        // 准备参数
        Long appId = order01.getAppId();

        // 调用
        Long count = orderService.getOrderCountByAppId(appId);
        // 断言
        assertEquals(count, 1L);
    }

    @Test
    public void testGetOrderPage() {
        // mock 数据
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class, o -> { // 等会查询到
            o.setAppId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setMerchantOrderId("110");
            o.setChannelOrderNo("220");
            o.setNo("330");
            o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
            o.setCreateTime(buildTime(2018, 1, 15));
        });
        orderMapper.insert(dbOrder);
        // 测试 appId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setAppId(2L)));
        // 测试 channelCode 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantOrderId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setMerchantOrderId(randomString())));
        // 测试 channelOrderNo 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelOrderNo(randomString())));
        // 测试 no 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setNo(randomString())));
        // 测试 status 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())));
        // 测试 createTime 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setCreateTime(buildTime(2019, 1, 1))));
        // 准备参数
        PayOrderPageReqVO reqVO = new PayOrderPageReqVO();
        reqVO.setAppId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantOrderId("11");
        reqVO.setChannelOrderNo("22");
        reqVO.setNo("33");
        reqVO.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2018, 1, 10, 2018, 1, 30));

        // 调用
        PageResult<PayOrderDO> pageResult = orderService.getOrderPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbOrder, pageResult.getList().get(0));
    }

    @Test
    public void testGetOrderList() {
        // mock 数据
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class, o -> { // 等会查询到
            o.setAppId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setMerchantOrderId("110");
            o.setChannelOrderNo("220");
            o.setNo("330");
            o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
            o.setCreateTime(buildTime(2018, 1, 15));
        });
        orderMapper.insert(dbOrder);
        // 测试 appId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setAppId(2L)));
        // 测试 channelCode 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantOrderId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setMerchantOrderId(randomString())));
        // 测试 channelOrderNo 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelOrderNo(randomString())));
        // 测试 no 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setNo(randomString())));
        // 测试 status 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())));
        // 测试 createTime 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setCreateTime(buildTime(2019, 1, 1))));
        // 准备参数
        PayOrderExportReqVO reqVO = new PayOrderExportReqVO();
        reqVO.setAppId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantOrderId("11");
        reqVO.setChannelOrderNo("22");
        reqVO.setNo("33");
        reqVO.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2018, 1, 10, 2018, 1, 30));

        // 调用
        List<PayOrderDO> list = orderService.getOrderList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbOrder, list.get(0));
    }

    @Test
    public void testCreateOrder_success() {
        // mock 参数
        PayOrderCreateReqDTO reqDTO = randomPojo(PayOrderCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("10")
                        .setSubject(randomString()).setBody(randomString()));
        // mock 方法
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L).setOrderNotifyUrl("http://127.0.0.1"));
        when(appService.validPayApp(eq(reqDTO.getAppKey()))).thenReturn(app);

        // 调用
        Long orderId = orderService.createOrder(reqDTO);
        // 断言
        PayOrderDO order = orderMapper.selectById(orderId);
        assertPojoEquals(order, reqDTO);
        assertEquals(order.getAppId(), 1L);
        assertEquals(order.getNotifyUrl(), "http://127.0.0.1");
        assertEquals(order.getStatus(), PayOrderStatusEnum.WAITING.getStatus());
        assertEquals(order.getRefundPrice(), 0);
    }

    @Test
    public void testCreateOrder_exists() {
        // mock 参数
        PayOrderCreateReqDTO reqDTO = randomPojo(PayOrderCreateReqDTO.class,
                o -> o.setAppKey("demo").setMerchantOrderId("10"));
        // mock 数据
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class,  o -> o.setAppId(1L).setMerchantOrderId("10"));
        orderMapper.insert(dbOrder);
        // mock 方法
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L).setOrderNotifyUrl("http://127.0.0.1"));
        when(appService.validPayApp(eq(reqDTO.getAppKey()))).thenReturn(app);

        // 调用
        Long orderId = orderService.createOrder(reqDTO);
        // 断言
        PayOrderDO order = orderMapper.selectById(orderId);
        assertPojoEquals(dbOrder, order);
    }

    @Test
    public void testSubmitOrder_notFound() {
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class);
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), PAY_ORDER_NOT_FOUND);
    }

    @Test
    public void testSubmitOrder_notWaiting() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.REFUND.getStatus()));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId()));
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), PAY_ORDER_STATUS_IS_NOT_WAITING);
    }

    @Test
    public void testSubmitOrder_isSuccess() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId()));
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), PAY_ORDER_STATUS_IS_SUCCESS);
    }

    @Test
    public void testSubmitOrder_expired() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setExpireTime(addTime(Duration.ofDays(-1))));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId()));
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), PAY_ORDER_IS_EXPIRED);
    }

    @Test
    public void testSubmitOrder_channelNotFound() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setAppId(1L).setExpireTime(addTime(Duration.ofDays(1))));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                .setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
        String userIp = randomString();
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq(1L))).thenReturn(app);
        // mock 方法（channel）
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setCode(PayChannelEnum.ALIPAY_APP.getCode()));
        when(channelService.validPayChannel(eq(1L), eq(PayChannelEnum.ALIPAY_APP.getCode())))
                .thenReturn(channel);

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), CHANNEL_NOT_FOUND);
    }

    @Test // 调用 unifiedOrder 接口，返回存在渠道错误
    public void testSubmitOrder_channelError() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order）
            PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                    .setAppId(1L).setExpireTime(addTime(Duration.ofDays(1))));
            orderMapper.insert(order);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
            String userIp = randomString();
            // mock 方法（app）
            PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
            when(appService.validPayApp(eq(1L))).thenReturn(app);
            // mock 方法（channel）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L)
                    .setCode(PayChannelEnum.ALIPAY_APP.getCode()));
            when(channelService.validPayChannel(eq(1L), eq(PayChannelEnum.ALIPAY_APP.getCode())))
                    .thenReturn(channel);
            // mock 方法（client）
            PayClient<?> client = mock(PayClient.class);
            when(channelService.getPayClient(eq(10L))).thenReturn(client);
            // mock 方法（）
            PayOrderRespDTO unifiedOrderResp = randomPojo(PayOrderRespDTO.class, o ->
                    o.setChannelErrorCode("001").setChannelErrorMsg("模拟异常"));
            when(client.unifiedOrder(argThat(payOrderUnifiedReqDTO -> {
                assertNotNull(payOrderUnifiedReqDTO.getOutTradeNo());
                assertThat(payOrderUnifiedReqDTO)
//                        .extracting("subject", "body", "notifyUrl", "returnUrl", "price", "expireTime") // TODO @芋艿：win11 下，时间不太准
                        .extracting("subject", "body", "notifyUrl", "returnUrl", "price")
                        .containsExactly(order.getSubject(), order.getBody(), "http://127.0.0.1/10",
//                                reqVO.getReturnUrl(), order.getPrice(), order.getExpireTime());
                                reqVO.getReturnUrl(), order.getPrice());
                return true;
            }))).thenReturn(unifiedOrderResp);

            // 调用，并断言异常
            assertServiceException(() -> orderService.submitOrder(reqVO, userIp),
                    PAY_ORDER_SUBMIT_CHANNEL_ERROR, "001", "模拟异常");
            // 断言，数据记录（PayOrderExtensionDO）
            PayOrderExtensionDO orderExtension = orderExtensionMapper.selectOne(null);
            assertNotNull(orderExtension);
            assertThat(orderExtension).extracting("no", "orderId").isNotNull();
            assertThat(orderExtension)
                    .extracting("channelId", "channelCode","userIp" ,"status", "channelExtras",
                            "channelErrorCode", "channelErrorMsg", "channelNotifyData")
                    .containsExactly(10L, PayChannelEnum.ALIPAY_APP.getCode(), userIp,
                            PayOrderStatusEnum.WAITING.getStatus(), reqVO.getChannelExtras(),
                            null, null, null);
        }
    }

    @Test
    public void testSubmitOrder_success() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order）
            PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                    .setAppId(1L).setExpireTime(addTime(Duration.ofDays(1))));
            orderMapper.insert(order);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()));
            String userIp = randomString();
            // mock 方法（app）
            PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
            when(appService.validPayApp(eq(1L))).thenReturn(app);
            // mock 方法（channel）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L)
                    .setCode(PayChannelEnum.ALIPAY_APP.getCode()));
            when(channelService.validPayChannel(eq(1L), eq(PayChannelEnum.ALIPAY_APP.getCode())))
                    .thenReturn(channel);
            // mock 方法（client）
            PayClient<?> client = mock(PayClient.class);
            when(channelService.getPayClient(eq(10L))).thenReturn(client);
            // mock 方法（支付渠道的调用）
            PayOrderRespDTO unifiedOrderResp = randomPojo(PayOrderRespDTO.class, o -> o.setChannelErrorCode(null).setChannelErrorMsg(null)
                    .setDisplayMode(PayOrderDisplayModeEnum.URL.getMode()).setDisplayContent("tudou"));
            when(client.unifiedOrder(argThat(payOrderUnifiedReqDTO -> {
                assertNotNull(payOrderUnifiedReqDTO.getOutTradeNo());
                assertThat(payOrderUnifiedReqDTO)
//                        .extracting("subject", "body", "notifyUrl", "returnUrl", "price", "expireTime") // TODO @芋艿：win11 下，时间不太准
                        .extracting("subject", "body", "notifyUrl", "returnUrl", "price")
                        .containsExactly(order.getSubject(), order.getBody(), "http://127.0.0.1/10",
//                                reqVO.getReturnUrl(), order.getPrice(), order.getExpireTime());
                                reqVO.getReturnUrl(), order.getPrice());
                return true;
            }))).thenReturn(unifiedOrderResp);

            // 调用
            PayOrderSubmitRespVO result = orderService.submitOrder(reqVO, userIp);
            // 断言，数据记录（PayOrderExtensionDO）
            PayOrderExtensionDO orderExtension = orderExtensionMapper.selectOne(null);
            assertNotNull(orderExtension);
            assertThat(orderExtension).extracting("no", "orderId").isNotNull();
            assertThat(orderExtension)
                    .extracting("channelId", "channelCode","userIp" ,"status", "channelExtras",
                            "channelErrorCode", "channelErrorMsg", "channelNotifyData")
                    .containsExactly(10L, PayChannelEnum.ALIPAY_APP.getCode(), userIp,
                            PayOrderStatusEnum.WAITING.getStatus(), reqVO.getChannelExtras(),
                            null, null, null);
            // 断言，返回（PayOrderSubmitRespVO）
            assertThat(result)
                    .extracting("status", "displayMode", "displayContent")
                    .containsExactly(PayOrderStatusEnum.WAITING.getStatus(), PayOrderDisplayModeEnum.URL.getMode(), "tudou");
            // 断言，调用
            verify(payOrderServiceImpl).notifyOrder(same(channel), same(unifiedOrderResp));
        }
    }

    @Test
    public void testSubmitOrder_wechatReuseWaitingExtension() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO orderExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(eq(orderExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, orderExtension.getNo(), null));
            PayOrderRespDTO unifiedOrderResp = PayOrderRespDTO.waitingOf(
                    PayOrderDisplayModeEnum.APP.getMode(), "pay-content", orderExtension.getNo(), null);
            when(client.unifiedOrder(any())).thenAnswer(invocation -> {
                PayOrderUnifiedReqDTO request = invocation.getArgument(0);
                assertEquals(orderExtension.getNo(), request.getOutTradeNo());
                assertEquals(orderExtension.getUserIp(), request.getUserIp());
                return unifiedOrderResp;
            });

            // 调用
            PayOrderSubmitRespVO result = orderService.submitOrder(reqVO, randomString());
            // 断言：复用原拓展单，不新增记录
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(1);
            assertEquals("pay-content", result.getDisplayContent());
            verify(client).unifiedOrder(any());
            verify(payOrderServiceImpl).notifyOrder(any(PayChannelDO.class), same(unifiedOrderResp));
        }
    }

    @Test
    public void testSubmitOrder_wechatReuseClosed_thenCreateNewExtension() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）：原渠道订单已关闭，新渠道订单待支付
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(anyString())).thenAnswer(invocation ->
                    PayOrderRespDTO.waitingOf(null, null, invocation.getArgument(0), null));
            when(client.getOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.closedOf(null, null, oldExtension.getNo(), null));
            PayOrderRespDTO closedResp = PayOrderRespDTO.closedOf("ORDERCLOSED", "订单已关闭", oldExtension.getNo(), null);
            PayOrderRespDTO waitingResp = PayOrderRespDTO.waitingOf(
                    PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null);
            List<String> outTradeNos = new ArrayList<>();
            when(client.unifiedOrder(any())).thenAnswer(invocation -> {
                PayOrderUnifiedReqDTO request = invocation.getArgument(0);
                outTradeNos.add(request.getOutTradeNo());
                return oldExtension.getNo().equals(request.getOutTradeNo()) ? closedResp : waitingResp;
            });

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：原拓展单保持待支付，并记录下单失败结果；新建拓展单发起支付
            List<PayOrderExtensionDO> extensions = orderExtensionMapper.selectListByOrderId(order.getId());
            assertThat(extensions).hasSize(2);
            PayOrderExtensionDO dbOldExtension = orderExtensionMapper.selectById(oldExtension.getId());
            assertEquals(PayOrderStatusEnum.WAITING.getStatus(), dbOldExtension.getStatus());
            assertEquals("ORDERCLOSED", dbOldExtension.getChannelErrorCode());
            assertEquals(toJsonString(closedResp), dbOldExtension.getChannelNotifyData());
            PayOrderExtensionDO newExtension = extensions.stream()
                    .filter(extension -> !extension.getId().equals(oldExtension.getId())).findFirst().orElseThrow();
            assertThat(outTradeNos).containsExactly(oldExtension.getNo(), newExtension.getNo());
            // 断言：只处理新拓展单的下单结果，不能用原拓展单的失败结果更新状态
            verify(payOrderServiceImpl).notifyOrder(any(PayChannelDO.class), same(waitingResp));
            verify(payOrderServiceImpl, never()).notifyOrder(any(PayChannelDO.class), same(closedResp));
            verify(client, never()).closeOrder(anyString());

            // 再次调用：复用新拓展单，不再使用原拓展单下单
            orderService.submitOrder(reqVO, randomString());
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            assertThat(outTradeNos).containsExactly(oldExtension.getNo(), newExtension.getNo(), newExtension.getNo());
        }
    }

    @Test
    public void testSubmitOrder_wechatReuseFail_channelWaiting_closeThenCreateNewExtension() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）：原拓展单下单失败，渠道订单仍待支付，主动关闭成功
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(anyString())).thenAnswer(invocation ->
                    PayOrderRespDTO.waitingOf(null, null, invocation.getArgument(0), null));
            when(client.closeOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.closedOf(null, null, oldExtension.getNo(), null));
            List<String> outTradeNos = new ArrayList<>();
            when(client.unifiedOrder(any())).thenAnswer(invocation -> {
                PayOrderUnifiedReqDTO request = invocation.getArgument(0);
                outTradeNos.add(request.getOutTradeNo());
                return oldExtension.getNo().equals(request.getOutTradeNo())
                        ? PayOrderRespDTO.closedOf("SYSTEMERROR", "系统错误", request.getOutTradeNo(), null)
                        : PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null);
            });

            // 第一次调用：先关闭原渠道订单，再新建拓展单发起支付
            PayOrderSubmitRespVO result = orderService.submitOrder(reqVO, randomString());
            assertEquals("pay-content", result.getDisplayContent());
            verify(client).closeOrder(eq(oldExtension.getNo()));
            // 断言：原拓展单保持待支付，并记录下单失败结果；新建拓展单发起支付
            List<PayOrderExtensionDO> extensions = orderExtensionMapper.selectListByOrderId(order.getId());
            assertThat(extensions).hasSize(2);
            PayOrderExtensionDO dbOldExtension = orderExtensionMapper.selectById(oldExtension.getId());
            assertEquals(PayOrderStatusEnum.WAITING.getStatus(), dbOldExtension.getStatus());
            assertEquals("SYSTEMERROR", dbOldExtension.getChannelErrorCode());
            assertNotNull(dbOldExtension.getChannelNotifyData());
            PayOrderExtensionDO newExtension = extensions.stream()
                    .filter(extension -> !extension.getId().equals(oldExtension.getId())).findFirst().orElseThrow();
            // 第二次调用：复用新拓展单
            orderService.submitOrder(reqVO, randomString());
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            assertThat(outTradeNos).containsExactly(oldExtension.getNo(), newExtension.getNo(), newExtension.getNo());
            verify(client).closeOrder(anyString());
        }
    }

    @Test
    public void testSubmitOrder_wechatReuseFail_channelWaiting_closeFail() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）：原拓展单下单失败，渠道订单仍待支付，第一次关闭失败（例如说：用户支付中），第二次关闭成功
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(anyString())).thenAnswer(invocation ->
                    PayOrderRespDTO.waitingOf(null, null, invocation.getArgument(0), null));
            when(client.closeOrder(eq(oldExtension.getNo())))
                    .thenReturn(PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null))
                    .thenReturn(PayOrderRespDTO.closedOf(null, null, oldExtension.getNo(), null));
            List<String> outTradeNos = new ArrayList<>();
            when(client.unifiedOrder(any())).thenAnswer(invocation -> {
                PayOrderUnifiedReqDTO request = invocation.getArgument(0);
                outTradeNos.add(request.getOutTradeNo());
                return oldExtension.getNo().equals(request.getOutTradeNo())
                        ? PayOrderRespDTO.closedOf("SYSTEMERROR", "系统错误", request.getOutTradeNo(), null)
                        : PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null);
            });

            // 第一次调用，并断言异常
            assertServiceException(() -> orderService.submitOrder(reqVO, randomString()),
                    PAY_ORDER_SUBMIT_CHANNEL_ERROR, "SYSTEMERROR", "系统错误");
            // 断言：无法确认原渠道订单已关闭，不新建拓展单，也不记录下单失败结果，避免存在两个可支付的渠道订单
            List<PayOrderExtensionDO> extensions = orderExtensionMapper.selectListByOrderId(order.getId());
            assertThat(extensions).hasSize(1);
            assertEquals(PayOrderStatusEnum.WAITING.getStatus(), extensions.get(0).getStatus());
            assertNull(extensions.get(0).getChannelNotifyData());
            verify(payOrderServiceImpl, never()).notifyOrder(any(PayChannelDO.class), any(PayOrderRespDTO.class));

            // 第二次调用：仍复用原拓展单，关闭成功后，再新建拓展单发起支付
            PayOrderSubmitRespVO result = orderService.submitOrder(reqVO, randomString());
            assertEquals("pay-content", result.getDisplayContent());
            extensions = orderExtensionMapper.selectListByOrderId(order.getId());
            assertThat(extensions).hasSize(2);
            PayOrderExtensionDO newExtension = extensions.stream()
                    .filter(extension -> !extension.getId().equals(oldExtension.getId())).findFirst().orElseThrow();
            assertThat(outTradeNos).containsExactly(oldExtension.getNo(), oldExtension.getNo(), newExtension.getNo());
            verify(client, times(2)).closeOrder(eq(oldExtension.getNo()));
        }
    }

    @Test
    public void testSubmitOrder_wechatReuseFail_channelWaiting_closeFoundPaid() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）：原拓展单下单失败，查询时待支付，关闭时发现已支付
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null));
            PayOrderRespDTO successResp = PayOrderRespDTO.successOf(randomString(), randomString(),
                    LocalDateTime.now(), oldExtension.getNo(), null);
            when(client.closeOrder(eq(oldExtension.getNo()))).thenReturn(successResp);
            when(client.unifiedOrder(any())).thenReturn(
                    PayOrderRespDTO.closedOf("SYSTEMERROR", "系统错误", oldExtension.getNo(), null));

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：不新建拓展单，按渠道已支付结果处理
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(1);
            verify(client).unifiedOrder(any());
            verify(payOrderServiceImpl).notifyOrder(any(PayChannelDO.class), same(successResp));
        }
    }

    @Test
    public void testSubmitOrder_wechatReuseFail_channelSuccess() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO orderExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）：提交校验时待支付，下单时已支付（回调延迟）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            PayOrderRespDTO successResp = PayOrderRespDTO.successOf(randomString(), randomString(),
                    LocalDateTime.now(), orderExtension.getNo(), null);
            when(client.getOrder(eq(orderExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, orderExtension.getNo(), null), successResp);
            when(client.unifiedOrder(any())).thenReturn(
                    PayOrderRespDTO.closedOf("ORDERPAID", "订单已支付", orderExtension.getNo(), null));

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：不新建拓展单，按渠道已支付结果处理
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(1);
            verify(client).unifiedOrder(any());
            verify(payOrderServiceImpl).notifyOrder(any(PayChannelDO.class), same(successResp));
        }
    }

    @Test
    public void testSubmitOrder_wechatOrderUpdated_notReuse() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）：拓展单创建后，支付订单被改价
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            PayOrderDO updateObj = new PayOrderDO().setId(order.getId()).setPrice(order.getPrice() + 1);
            updateObj.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(updateObj);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null));
            when(client.unifiedOrder(argThat(request -> !oldExtension.getNo().equals(request.getOutTradeNo()))))
                    .thenReturn(PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null));

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：新建拓展单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            verify(client).unifiedOrder(any());
        }
    }

    @Test
    public void testSubmitOrder_wechatChannelExtrasChanged_notReuse() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB,
                    Map.of("openid", "old-openid"));
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(Map.of("openid", "new-openid")));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null));
            when(client.unifiedOrder(argThat(request -> !oldExtension.getNo().equals(request.getOutTradeNo()))))
                    .thenReturn(PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null));

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：新建拓展单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            verify(client).unifiedOrder(any());
        }
    }

    @Test
    public void testSubmitOrder_wechatOrderUpdatedSameSecond_notReuse() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）：拓展单创建后，同一秒内支付订单被改价，数据库时间相等
            Map<String, String> channelExtras = Map.of("openid", "openid");
            LocalDateTime time = LocalDateTime.now().withNano(0).minusMinutes(1);
            PayOrderDO order = insertSubmitOrder(time.minusMinutes(10), time);
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB,
                    channelExtras, time);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null));
            when(client.unifiedOrder(argThat(request -> !oldExtension.getNo().equals(request.getOutTradeNo()))))
                    .thenReturn(PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null));

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：无法区分先后，按已改价处理，新建拓展单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            verify(client).unifiedOrder(any());
        }
    }

    @Test
    public void testSubmitOrder_wechatNewOrderSameSecond_notReuse() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）：支付订单与拓展单在同一秒内创建，数据库时间相等（可能同一秒内被改价）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            LocalDateTime time = LocalDateTime.now().withNano(0).minusMinutes(1);
            PayOrderDO order = insertSubmitOrder(time, time);
            PayOrderExtensionDO orderExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB,
                    channelExtras, time);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(eq(orderExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, orderExtension.getNo(), null));
            when(client.unifiedOrder(argThat(request -> !orderExtension.getNo().equals(request.getOutTradeNo()))))
                    .thenReturn(PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null));

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：同一秒内无法排除改价，按已修改处理，新建拓展单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            verify(client).unifiedOrder(any());
        }
    }

    @Test
    public void testSubmitOrder_wechatReuseFail_queryException() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）：提交校验时待支付，下单失败后查询、关闭渠道订单都异常，之后恢复正常
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(anyString())).thenAnswer(invocation ->
                    PayOrderRespDTO.waitingOf(null, null, invocation.getArgument(0), null));
            when(client.getOrder(eq(oldExtension.getNo())))
                    .thenReturn(PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null))
                    .thenThrow(new PayClientException(new RuntimeException("网络超时")))
                    .thenReturn(PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null));
            when(client.closeOrder(eq(oldExtension.getNo())))
                    .thenThrow(new PayClientException(new RuntimeException("网络超时")))
                    .thenReturn(PayOrderRespDTO.closedOf(null, null, oldExtension.getNo(), null));
            List<String> outTradeNos = new ArrayList<>();
            when(client.unifiedOrder(any())).thenAnswer(invocation -> {
                PayOrderUnifiedReqDTO request = invocation.getArgument(0);
                outTradeNos.add(request.getOutTradeNo());
                return oldExtension.getNo().equals(request.getOutTradeNo())
                        ? PayOrderRespDTO.closedOf("SYSTEMERROR", "系统错误", request.getOutTradeNo(), null)
                        : PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null);
            });

            // 第一次调用，并断言异常：查询、关闭异常转换为业务异常
            assertServiceException(() -> orderService.submitOrder(reqVO, randomString()),
                    PAY_ORDER_SUBMIT_CHANNEL_ERROR, "SYSTEMERROR", "系统错误");
            // 断言：渠道状态未知，不新建拓展单，也不记录下单失败结果
            List<PayOrderExtensionDO> extensions = orderExtensionMapper.selectListByOrderId(order.getId());
            assertThat(extensions).hasSize(1);
            assertEquals(PayOrderStatusEnum.WAITING.getStatus(), extensions.get(0).getStatus());
            assertNull(extensions.get(0).getChannelNotifyData());
            verify(payOrderServiceImpl, never()).notifyOrder(any(PayChannelDO.class), any(PayOrderRespDTO.class));

            // 第二次调用：渠道恢复后，关闭原渠道订单，再新建拓展单发起支付
            orderService.submitOrder(reqVO, randomString());
            extensions = orderExtensionMapper.selectListByOrderId(order.getId());
            assertThat(extensions).hasSize(2);
            PayOrderExtensionDO newExtension = extensions.stream()
                    .filter(extension -> !extension.getId().equals(oldExtension.getId())).findFirst().orElseThrow();
            assertThat(outTradeNos).containsExactly(oldExtension.getNo(), oldExtension.getNo(), newExtension.getNo());
        }
    }

    @Test
    public void testSubmitOrder_wechatReuseFail_extensionPaidConcurrently() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("openid", "openid");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO orderExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_PUB, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_PUB.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）：下单失败后，渠道订单已关闭，但原拓展单被并发回调更新为已支付
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_PUB);
            when(client.getOrder(eq(orderExtension.getNo())))
                    .thenReturn(PayOrderRespDTO.waitingOf(null, null, orderExtension.getNo(), null))
                    .thenAnswer(invocation -> {
                        orderExtensionMapper.updateById(new PayOrderExtensionDO().setId(orderExtension.getId())
                                .setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
                        return PayOrderRespDTO.closedOf(null, null, orderExtension.getNo(), null);
                    });
            when(client.unifiedOrder(any())).thenReturn(
                    PayOrderRespDTO.closedOf("ORDERCLOSED", "订单已关闭", orderExtension.getNo(), null));

            // 调用，并断言异常
            assertServiceException(() -> orderService.submitOrder(reqVO, randomString()),
                    PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
            // 断言：不新建拓展单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(1);
            verify(client).unifiedOrder(any());
        }
    }

    @Test
    public void testSubmitOrder_wechatWapUserIpChanged_notReuse() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("key", "value");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_WAP, channelExtras);
            // 准备参数：用户 IP 变化（例如说：切换网络）
            String userIp = "10.0.0.1";
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_WAP.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_WAP);
            when(client.getOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null));
            when(client.unifiedOrder(argThat(request -> !oldExtension.getNo().equals(request.getOutTradeNo())
                    && userIp.equals(request.getUserIp()))))
                    .thenReturn(PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.URL.getMode(), "pay-url", null, null));

            // 调用
            orderService.submitOrder(reqVO, userIp);
            // 断言：新建拓展单，使用新的用户 IP 下单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            verify(client).unifiedOrder(any());
        }
    }

    @Test
    public void testSubmitOrder_wechatWapUserIpSame_reuse() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("key", "value");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO orderExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_WAP, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_WAP.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_WAP);
            when(client.getOrder(eq(orderExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, orderExtension.getNo(), null));
            when(client.unifiedOrder(argThat(request -> orderExtension.getNo().equals(request.getOutTradeNo()))))
                    .thenReturn(PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.URL.getMode(), "pay-url", null, null));

            // 调用
            orderService.submitOrder(reqVO, orderExtension.getUserIp());
            // 断言：用户 IP 一致，复用原拓展单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(1);
            verify(client).unifiedOrder(any());
        }
    }

    @Test
    public void testSubmitOrder_alipayNotReuse() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("key", "value");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.ALIPAY_APP, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.ALIPAY_APP.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.ALIPAY_APP);
            when(client.getOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null));
            when(client.unifiedOrder(argThat(request -> !oldExtension.getNo().equals(request.getOutTradeNo()))))
                    .thenReturn(PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.APP.getMode(), "pay-content", null, null));

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：支付宝保持每次新建拓展单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            verify(client).unifiedOrder(any());
        }
    }

    @Test
    public void testSubmitOrder_wechatBarNotReuse() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（order、orderExtension）
            Map<String, String> channelExtras = Map.of("authCode", "134567890123456789");
            PayOrderDO order = insertSubmitOrder();
            PayOrderExtensionDO oldExtension = insertSubmitOrderExtension(order, PayChannelEnum.WX_BAR, channelExtras);
            // 准备参数
            PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId())
                    .setChannelCode(PayChannelEnum.WX_BAR.getCode()).setChannelExtras(channelExtras));
            // mock 方法（app、channel、client）
            PayClient<?> client = mockSubmitClient(PayChannelEnum.WX_BAR);
            when(client.getOrder(eq(oldExtension.getNo()))).thenReturn(
                    PayOrderRespDTO.waitingOf(null, null, oldExtension.getNo(), null));
            when(client.unifiedOrder(argThat(request -> !oldExtension.getNo().equals(request.getOutTradeNo()))))
                    .thenReturn(PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.BAR_CODE.getMode(), "", null, null));

            // 调用
            orderService.submitOrder(reqVO, randomString());
            // 断言：微信付款码的 authCode 只能使用一次，保持每次新建拓展单
            assertThat(orderExtensionMapper.selectListByOrderId(order.getId())).hasSize(2);
            verify(client).unifiedOrder(any());
        }
    }

    private PayOrderDO insertSubmitOrder() {
        LocalDateTime time = LocalDateTime.now().minusMinutes(10);
        return insertSubmitOrder(time, time);
    }

    private PayOrderDO insertSubmitOrder(LocalDateTime createTime, LocalDateTime updateTime) {
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setAppId(1L).setExpireTime(addTime(Duration.ofDays(1))));
        order.setCreateTime(createTime).setUpdateTime(updateTime);
        orderMapper.insert(order);
        return order;
    }

    private PayOrderExtensionDO insertSubmitOrderExtension(PayOrderDO order, PayChannelEnum channelEnum,
                                                           Map<String, String> channelExtras) {
        return insertSubmitOrderExtension(order, channelEnum, channelExtras, LocalDateTime.now().minusMinutes(5));
    }

    private PayOrderExtensionDO insertSubmitOrderExtension(PayOrderDO order, PayChannelEnum channelEnum,
                                                           Map<String, String> channelExtras, LocalDateTime createTime) {
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class, o -> o.setOrderId(order.getId())
                .setChannelId(10L).setChannelCode(channelEnum.getCode())
                .setStatus(PayOrderStatusEnum.WAITING.getStatus()).setChannelExtras(channelExtras)
                .setChannelErrorCode(null).setChannelErrorMsg(null).setChannelNotifyData(null)
                .setCreateTime(createTime));
        orderExtensionMapper.insert(orderExtension);
        return orderExtension;
    }

    private PayClient<?> mockSubmitClient(PayChannelEnum channelEnum) {
        when(appService.validPayApp(eq(1L))).thenReturn(randomPojo(PayAppDO.class, o -> o.setId(1L)));
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setCode(channelEnum.getCode()));
        when(channelService.validPayChannel(eq(1L), eq(channelEnum.getCode()))).thenReturn(channel);
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        return client;
    }

    @Test
    public void testValidateOrderActuallyPaid_dbPaid() {
        // 准备参数
        Long id = randomLongId();
        // mock 方法（OrderExtension 已支付）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setOrderId(id).setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
        orderExtensionMapper.insert(orderExtension);

        // 调用，并断言异常
        assertServiceException(() -> orderService.validateOrderActuallyPaid(id),
                PAY_ORDER_EXTENSION_IS_PAID);
    }

    @Test
    public void testValidateOrderActuallyPaid_remotePaid() {
        // 准备参数
        Long id = randomLongId();
        // mock 方法（OrderExtension 已支付）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setOrderId(id).setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        orderExtensionMapper.insert(orderExtension);
        // mock 方法（PayClient 已支付）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(orderExtension.getChannelId()))).thenReturn(client);
        when(client.getOrder(eq(orderExtension.getNo()))).thenReturn(randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())));

        // 调用，并断言异常
        assertServiceException(() -> orderService.validateOrderActuallyPaid(id),
                PAY_ORDER_EXTENSION_IS_PAID);
    }

    @Test
    public void testValidateOrderActuallyPaid_success() {
        // 准备参数
        Long id = randomLongId();
        // mock 方法（OrderExtension 已支付）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setOrderId(id).setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        orderExtensionMapper.insert(orderExtension);
        // mock 方法（PayClient 已支付）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(orderExtension.getChannelId()))).thenReturn(client);
        when(client.getOrder(eq(orderExtension.getNo()))).thenReturn(randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())));

        // 调用，并断言异常
        orderService.validateOrderActuallyPaid(id);
    }

    @Test
    public void testNotifyOrder_channelId() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);
            // 准备参数
            Long channelId = 10L;
            PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class);
            // mock 方法（channel）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
            when(channelService.validPayChannel(eq(10L))).thenReturn(channel);

            // 调用
            orderService.notifyOrder(channelId, notify);
            // 断言
            verify(payOrderServiceImpl).notifyOrder(same(channel), same(notify));
        }
    }

    @Test
    public void testNotifyOrderSuccess_orderExtension_notFound() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> orderService.notifyOrder(channel, notify),
                PAY_ORDER_EXTENSION_NOT_FOUND);
    }

    @Test
    public void testNotifyOrderSuccess_orderExtension_closed() {
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setNo("P110"));
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setOutTradeNo("P110"));

        // 调用，并断言异常
        assertServiceException(() -> orderService.notifyOrder(channel, notify),
                PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
    }

    @Test
    public void testNotifyOrderSuccess_order_notFound() {
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setNo("P110"));
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setOutTradeNo("P110"));

        // 调用，并断言异常
        assertServiceException(() -> orderService.notifyOrder(channel, notify),
                PAY_ORDER_NOT_FOUND);
        // 断言 PayOrderExtensionDO ：数据更新被回滚
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null));
    }

    @Test
    public void testNotifyOrderSuccess_order_closed() {
        testNotifyOrderSuccess_order_closedOrRefund(PayOrderStatusEnum.CLOSED.getStatus());
    }

    @Test
    public void testNotifyOrderSuccess_order_refund() {
        testNotifyOrderSuccess_order_closedOrRefund(PayOrderStatusEnum.REFUND.getStatus());
    }

    private void testNotifyOrderSuccess_order_closedOrRefund(Integer status) {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(status));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setNo("P110")
                        .setOrderId(order.getId()));
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setOutTradeNo("P110"));

        // 调用，并断言异常
        assertServiceException(() -> orderService.notifyOrder(channel, notify),
                PAY_ORDER_STATUS_IS_NOT_WAITING);
        // 断言 PayOrderExtensionDO ：数据未更新，因为它是 SUCCESS
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null));
    }

    @Test
    public void testNotifyOrderSuccess_order_paid() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setNo("P110")
                        .setOrderId(order.getId()));
        orderExtensionMapper.insert(orderExtension);
        // 重要：需要将 order 的 extensionId 更新下
        order.setExtensionId(orderExtension.getId());
        orderMapper.updateById(order);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setOutTradeNo("P110"));

        // 调用，并断言异常
        orderService.notifyOrder(channel, notify);
        // 断言 PayOrderExtensionDO ：数据未更新，因为它是 SUCCESS
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null));
        // 断言 PayOrderDO ：数据未更新，因为它是 SUCCESS
        assertPojoEquals(order, orderMapper.selectOne(null), "updateTime", "updater");
        // 断言，调用
        verify(notifyService, never()).createPayNotifyTask(anyInt(), anyLong());
    }

    @Test
    public void testNotifyOrderSuccess_order_waiting() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setPrice(10));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setNo("P110")
                        .setOrderId(order.getId()));
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L)
                .setFeeRate(10D));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setOutTradeNo("P110"));

        // 调用，并断言异常
        orderService.notifyOrder(channel, notify);
        // 断言 PayOrderExtensionDO ：数据未更新，因为它是 SUCCESS
        orderExtension.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setChannelNotifyData(toJsonString(notify));
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null),
                "updateTime", "updater");
        // 断言 PayOrderDO ：数据未更新，因为它是 SUCCESS
        order.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setChannelId(10L).setChannelCode(channel.getCode())
                .setSuccessTime(notify.getSuccessTime()).setExtensionId(orderExtension.getId()).setNo(orderExtension.getNo())
                .setChannelOrderNo(notify.getChannelOrderNo()).setChannelUserId(notify.getChannelUserId())
                .setChannelFeeRate(10D).setChannelFeePrice(1);
        assertPojoEquals(order, orderMapper.selectOne(null),
                "updateTime", "updater");
        // 断言，调用
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.ORDER.getType()),
            eq(orderExtension.getOrderId()));
    }

    @Test
    public void testNotifyOrderClosed_orderExtension_notFound() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> orderService.notifyOrder(channel, notify),
                PAY_ORDER_EXTENSION_NOT_FOUND);
    }

    @Test
    public void testNotifyOrderClosed_orderExtension_closed() {
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setNo("P110"));
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setOutTradeNo("P110"));

        // 调用，并断言
        orderService.notifyOrder(channel, notify);
        // 断言 PayOrderExtensionDO ：数据未更新，因为它是 CLOSED
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null));
    }

    @Test
    public void testNotifyOrderClosed_orderExtension_paid() {
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setNo("P110"));
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setOutTradeNo("P110"));

        // 调用，并断言
        orderService.notifyOrder(channel, notify);
        // 断言 PayOrderExtensionDO ：数据未更新，因为它是 SUCCESS
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null));
    }

    @Test
    public void testNotifyOrderClosed_orderExtension_refund() {
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.REFUND.getStatus())
                        .setNo("P110"));
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setOutTradeNo("P110"));

        // 调用，并断言异常
        assertServiceException(() -> orderService.notifyOrder(channel, notify),
                PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING);
    }

    @Test
    public void testNotifyOrderClosed_orderExtension_waiting() {
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setNo("P110"));
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
        PayOrderRespDTO notify = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                        .setOutTradeNo("P110"));

        // 调用
        orderService.notifyOrder(channel, notify);
        // 断言 PayOrderExtensionDO
        orderExtension.setStatus(PayOrderStatusEnum.CLOSED.getStatus()).setChannelNotifyData(toJsonString(notify))
                .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg());
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null),
                "updateTime", "updater");
    }

    @Test
    public void testUpdateOrderRefundPrice_notFound() {
        // 准备参数
        Long id = randomLongId();
        Integer incrRefundPrice = randomInteger();

        // 调用，并断言异常
        assertServiceException(() -> orderService.updateOrderRefundPrice(id, incrRefundPrice),
                PAY_ORDER_NOT_FOUND);
    }

    @Test
    public void testUpdateOrderRefundPrice_waiting() {
        testUpdateOrderRefundPrice_waitingOrClosed(PayOrderStatusEnum.WAITING.getStatus());
    }

    @Test
    public void testUpdateOrderRefundPrice_closed() {
        testUpdateOrderRefundPrice_waitingOrClosed(PayOrderStatusEnum.CLOSED.getStatus());
    }

    private void testUpdateOrderRefundPrice_waitingOrClosed(Integer status) {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(status));
        orderMapper.insert(order);
        // 准备参数
        Long id = order.getId();
        Integer incrRefundPrice = randomInteger();

        // 调用，并断言异常
        assertServiceException(() -> orderService.updateOrderRefundPrice(id, incrRefundPrice),
                PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
    }

    @Test
    public void testUpdateOrderRefundPrice_priceExceed() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setRefundPrice(1).setPrice(10));
        orderMapper.insert(order);
        // 准备参数
        Long id = order.getId();
        Integer incrRefundPrice = 10;

        // 调用，并断言异常
        assertServiceException(() -> orderService.updateOrderRefundPrice(id, incrRefundPrice),
                REFUND_PRICE_EXCEED);
    }

    @Test
    public void testUpdateOrderRefundPrice_refund() {
        testUpdateOrderRefundPrice_refundOrSuccess(PayOrderStatusEnum.REFUND.getStatus());
    }

    @Test
    public void testUpdateOrderRefundPrice_success() {
        testUpdateOrderRefundPrice_refundOrSuccess(PayOrderStatusEnum.SUCCESS.getStatus());
    }

    private void testUpdateOrderRefundPrice_refundOrSuccess(Integer status) {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(status).setRefundPrice(1).setPrice(10));
        orderMapper.insert(order);
        // 准备参数
        Long id = order.getId();
        Integer incrRefundPrice = 8;

        // 调用
        orderService.updateOrderRefundPrice(id, incrRefundPrice);
        // 断言
        order.setRefundPrice(9).setStatus(PayOrderStatusEnum.REFUND.getStatus());
        assertPojoEquals(order, orderMapper.selectOne(null),
                "updateTime", "updater");
    }

    @Test
    public void testGetOrderExtension() {
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class);
        orderExtensionMapper.insert(orderExtension);
        // 准备参数
        Long id = orderExtension.getId();

        // 调用
        PayOrderExtensionDO dbOrderExtension = orderService.getOrderExtension(id);
        // 断言
        assertPojoEquals(dbOrderExtension, orderExtension);
    }

    @Test
    public void testSyncOrder_payClientNotFound() {
        // 准备参数
        LocalDateTime minCreateTime = LocalDateTime.now().minus(Duration.ofMinutes(10));
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setCreateTime(LocalDateTime.now()));
        orderExtensionMapper.insert(orderExtension);

        // 调用
        int count = orderService.syncOrder(minCreateTime);
        // 断言
        assertEquals(count, 0);
    }

    @Test
    public void testSyncOrder_exception() {
        // 准备参数
        LocalDateTime minCreateTime = LocalDateTime.now().minus(Duration.ofMinutes(10));
        // mock 数据（PayOrderExtensionDO）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setChannelId(10L)
                        .setCreateTime(LocalDateTime.now()));
        orderExtensionMapper.insert(orderExtension);
        // mock 方法（PayClient）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（PayClient 异常）
        when(client.getOrder(any())).thenThrow(new RuntimeException());

        // 调用
        int count = orderService.syncOrder(minCreateTime);
        // 断言
        assertEquals(count, 0);
    }

    @Test
    public void testSyncOrder_orderSuccess() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // 准备参数
            LocalDateTime minCreateTime = LocalDateTime.now().minus(Duration.ofMinutes(10));
            // mock 数据（PayOrderExtensionDO）
            PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                    o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                            .setChannelId(10L).setNo("P110")
                            .setCreateTime(LocalDateTime.now()));
            orderExtensionMapper.insert(orderExtension);
            // mock 方法（PayClient）
            PayClient<?> client = mock(PayClient.class);
            when(channelService.getPayClient(eq(10L))).thenReturn(client);
            // mock 方法（PayClient 成功返回）
            PayOrderRespDTO respDTO = randomPojo(PayOrderRespDTO.class,
                    o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
            when(client.getOrder(eq("P110"))).thenReturn(respDTO);
            // mock 方法（PayChannelDO）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
            when(channelService.validPayChannel(eq(10L))).thenReturn(channel);

            // 调用
            int count = orderService.syncOrder(minCreateTime);
            // 断言
            assertEquals(count, 1);
            verify(payOrderServiceImpl).notifyOrder(same(channel), same(respDTO));
        }
    }

    @Test
    public void testSyncOrder_orderClosed() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // 准备参数
            LocalDateTime minCreateTime = LocalDateTime.now().minus(Duration.ofMinutes(10));
            // mock 数据（PayOrderExtensionDO）
            PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                    o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                            .setChannelId(10L).setNo("P110")
                            .setCreateTime(LocalDateTime.now()));
            orderExtensionMapper.insert(orderExtension);
            // mock 方法（PayClient）
            PayClient<?> client = mock(PayClient.class);
            when(channelService.getPayClient(eq(10L))).thenReturn(client);
            // mock 方法（PayClient 成功返回）
            PayOrderRespDTO respDTO = randomPojo(PayOrderRespDTO.class,
                    o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus()));
            when(client.getOrder(eq("P110"))).thenReturn(respDTO);
            // mock 方法（PayChannelDO）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
            when(channelService.validPayChannel(eq(10L))).thenReturn(channel);

            // 调用
            int count = orderService.syncOrder(minCreateTime);
            // 断言
            assertEquals(count, 0);
            verify(payOrderServiceImpl, never()).notifyOrder(same(channel), same(respDTO));
        }
    }

    @Test
    public void testExpireOrder_orderExtension_isSuccess() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setExpireTime(addTime(Duration.ofMinutes(-1))));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO 已支付）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setOrderId(order.getId()));
        orderExtensionMapper.insert(orderExtension);
        // mock 方法（PayClient）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);

        // 调用
        int count = orderService.expireOrder();
        // 断言
        assertEquals(count, 0);
        // 断言 order 没有变化，因为没更新
        assertPojoEquals(order, orderMapper.selectOne(null));
    }

    @Test
    public void testExpireOrder_payClient_notFound() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setExpireTime(addTime(Duration.ofMinutes(-1))));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO 等待中）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setOrderId(order.getId())
                        .setChannelId(10L));
        orderExtensionMapper.insert(orderExtension);

        // 调用
        int count = orderService.expireOrder();
        // 断言
        assertEquals(count, 0);
        // 断言 order 没有变化，因为没更新
        assertPojoEquals(order, orderMapper.selectOne(null));
    }

    @Test
    public void testExpireOrder_getOrder_isRefund() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setExpireTime(addTime(Duration.ofMinutes(-1))));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO 等待中）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setOrderId(order.getId()).setNo("P110")
                        .setChannelId(10L));
        orderExtensionMapper.insert(orderExtension);
        // mock 方法（PayClient）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（PayClient 退款返回）
        PayOrderRespDTO respDTO = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.REFUND.getStatus()));
        when(client.getOrder(eq("P110"))).thenReturn(respDTO);

        // 调用
        int count = orderService.expireOrder();
        // 断言
        assertEquals(count, 0);
        // 断言 order 没有变化，因为没更新
        assertPojoEquals(order, orderMapper.selectOne(null));
    }

    @Test
    public void testExpireOrder_getOrder_isSuccess() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（PayOrderDO）
            PayOrderDO order = randomPojo(PayOrderDO.class,
                    o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                            .setExpireTime(addTime(Duration.ofMinutes(-1))));
            orderMapper.insert(order);
            // mock 数据（PayOrderExtensionDO 等待中）
            PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                    o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                            .setOrderId(order.getId()).setNo("P110")
                            .setChannelId(10L));
            orderExtensionMapper.insert(orderExtension);
            // mock 方法（PayClient）
            PayClient<?> client = mock(PayClient.class);
            when(channelService.getPayClient(eq(10L))).thenReturn(client);
            // mock 方法（PayClient 成功返回）
            PayOrderRespDTO respDTO = randomPojo(PayOrderRespDTO.class,
                    o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
            when(client.getOrder(eq("P110"))).thenReturn(respDTO);
            // mock 方法（PayChannelDO）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
            when(channelService.validPayChannel(eq(10L))).thenReturn(channel);

            // 调用
            int count = orderService.expireOrder();
            // 断言
            assertEquals(count, 0);
            // 断言 order 没有变化，因为没更新
            assertPojoEquals(order, orderMapper.selectOne(null));
            verify(payOrderServiceImpl).notifyOrder(same(channel), same(respDTO));
        }
    }

    @Test
    public void testExpireOrder_success() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setExpireTime(addTime(Duration.ofMinutes(-1))));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO 等待中）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setOrderId(order.getId()).setNo("P110")
                        .setChannelId(10L));
        orderExtensionMapper.insert(orderExtension);
        // mock 方法（PayClient）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（PayClient 关闭返回）
        PayOrderRespDTO respDTO = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus()));
        when(client.getOrder(eq("P110"))).thenReturn(respDTO);

        // 调用
        int count = orderService.expireOrder();
        // 断言
        assertEquals(count, 1);
        // 断言 extension 变化
        orderExtension.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                .setChannelNotifyData(toJsonString(respDTO));
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null),
                "updateTime", "updater");
        // 断言 order 变化
        order.setStatus(PayOrderStatusEnum.CLOSED.getStatus());
        assertPojoEquals(order, orderMapper.selectOne(null),
                "updateTime", "updater");
    }

    @Test
    public void testCloseOrder_orderExtension_isSuccess() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO 已支付）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                        .setOrderId(order.getId()));
        orderExtensionMapper.insert(orderExtension);

        // 调用
        orderService.closeOrder(order.getId());
        // 断言 order 没有变化，因为没更新
        assertPojoEquals(order, orderMapper.selectOne(null));
    }

    @Test
    public void testCloseOrder_payClient_notFound() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO 等待中）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setOrderId(order.getId())
                        .setChannelId(10L));
        orderExtensionMapper.insert(orderExtension);

        // 调用
        orderService.closeOrder(order.getId());
        // 断言 order、extension 没有变化，因为没更新
        assertPojoEquals(order, orderMapper.selectOne(null));
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null));
    }

    @Test
    public void testCloseOrder_closeOrder_isWaiting() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO 等待中）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setOrderId(order.getId()).setNo("P110")
                        .setChannelId(10L));
        orderExtensionMapper.insert(orderExtension);
        // mock 方法（PayClient）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（PayClient 等待返回，例如说渠道系统异常）
        PayOrderRespDTO respDTO = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        when(client.closeOrder(eq("P110"))).thenReturn(respDTO);

        // 调用
        orderService.closeOrder(order.getId());
        // 断言 order、extension 没有变化，因为没更新
        assertPojoEquals(order, orderMapper.selectOne(null));
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null));
    }

    @Test
    public void testCloseOrder_closeOrder_isSuccess() {
        PayOrderServiceImpl payOrderServiceImpl = mock(PayOrderServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayOrderServiceImpl.class)))
                    .thenReturn(payOrderServiceImpl);

            // mock 数据（PayOrderDO）
            PayOrderDO order = randomPojo(PayOrderDO.class,
                    o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus()));
            orderMapper.insert(order);
            // mock 数据（PayOrderExtensionDO 等待中）
            PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                    o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                            .setOrderId(order.getId()).setNo("P110")
                            .setChannelId(10L));
            orderExtensionMapper.insert(orderExtension);
            // mock 方法（PayClient）
            PayClient<?> client = mock(PayClient.class);
            when(channelService.getPayClient(eq(10L))).thenReturn(client);
            // mock 方法（PayClient 成功返回）
            PayOrderRespDTO respDTO = randomPojo(PayOrderRespDTO.class,
                    o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
            when(client.closeOrder(eq("P110"))).thenReturn(respDTO);
            // mock 方法（PayChannelDO）
            PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L));
            when(channelService.validPayChannel(eq(10L))).thenReturn(channel);

            // 调用
            orderService.closeOrder(order.getId());
            // 断言 order、extension 没有变化，因为没更新
            assertPojoEquals(order, orderMapper.selectOne(null));
            assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null));
            verify(payOrderServiceImpl).notifyOrder(same(channel), same(respDTO));
        }
    }

    @Test
    public void testCloseOrder_success() {
        // mock 数据（PayOrderDO）
        PayOrderDO order = randomPojo(PayOrderDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        orderMapper.insert(order);
        // mock 数据（PayOrderExtensionDO 等待中）
        PayOrderExtensionDO orderExtension = randomPojo(PayOrderExtensionDO.class,
                o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                        .setOrderId(order.getId()).setNo("P110")
                        .setChannelId(10L));
        orderExtensionMapper.insert(orderExtension);
        // mock 方法（PayClient）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（PayClient 关闭返回）
        PayOrderRespDTO respDTO = randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus()));
        when(client.closeOrder(eq("P110"))).thenReturn(respDTO);

        // 调用
        orderService.closeOrder(order.getId());
        // 断言 extension 变化
        orderExtension.setStatus(PayOrderStatusEnum.CLOSED.getStatus())
                .setChannelErrorCode(respDTO.getChannelErrorCode())
                .setChannelErrorMsg(respDTO.getChannelErrorMsg())
                .setChannelNotifyData(toJsonString(respDTO));
        assertPojoEquals(orderExtension, orderExtensionMapper.selectOne(null),
                "updateTime", "updater");
        // 断言 order 变化
        order.setStatus(PayOrderStatusEnum.CLOSED.getStatus());
        assertPojoEquals(order, orderMapper.selectOne(null),
                "updateTime", "updater");
    }

}
