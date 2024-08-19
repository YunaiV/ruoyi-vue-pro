package cn.iocoder.yudao.module.pay.service.refund;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
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
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.List;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link PayRefundServiceImpl} 的单元测试类
 *
 * @author 芋艿
 */
@Import({PayRefundServiceImpl.class, PayNoRedisDAO.class})
public class PayRefundServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private PayRefundServiceImpl refundService;

    @Resource
    private PayRefundMapper refundMapper;

    @MockBean
    private PayProperties payProperties;
    @MockBean
    private PayOrderService orderService;
    @MockBean
    private PayAppService appService;
    @MockBean
    private PayChannelService channelService;
    @MockBean
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
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
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
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
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
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
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
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
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
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
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
        PayClient client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 数据（refund 已存在）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o ->
                o.setAppId(1L).setMerchantRefundId("200"));
        refundMapper.insert(refund);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
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
        PayClient client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（client 调用发生异常）
        when(client.unifiedRefund(any(PayRefundUnifiedReqDTO.class))).thenThrow(new RuntimeException());

        // 调用
        Long refundId = refundService.createPayRefund(reqDTO);
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
            PayClient client = mock(PayClient.class);
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
            Long refundId = refundService.createPayRefund(reqDTO);
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
                o -> o.setStatus(PayRefundStatusRespEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));

        // 调用，并断言异常
        assertServiceException(() -> refundService.notifyRefund(channel, refundRespDTO),
                REFUND_NOT_FOUND);
    }

    @Test
    public void testNotifyRefundSuccess_isSuccess() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusRespEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));
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
                o -> o.setStatus(PayRefundStatusRespEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));
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
                o -> o.setStatus(PayRefundStatusRespEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));
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
                o -> o.setStatus(PayRefundStatusRespEnum.SUCCESS.getStatus()).setOutRefundNo("R100"));
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
                o -> o.setStatus(PayRefundStatusRespEnum.FAILURE.getStatus()).setOutRefundNo("R100"));

        // 调用，并断言异常
        assertServiceException(() -> refundService.notifyRefund(channel, refundRespDTO),
                REFUND_NOT_FOUND);
    }

    @Test
    public void testNotifyRefundFailure_isFailure() {
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L));
        PayRefundRespDTO refundRespDTO = randomPojo(PayRefundRespDTO.class,
                o -> o.setStatus(PayRefundStatusRespEnum.FAILURE.getStatus()).setOutRefundNo("R100"));
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
                o -> o.setStatus(PayRefundStatusRespEnum.FAILURE.getStatus()).setOutRefundNo("R100"));
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
                o -> o.setStatus(PayRefundStatusRespEnum.FAILURE.getStatus()).setOutRefundNo("R100"));
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
        assertEquals(testSyncRefund_waitingOrSuccessOrFailure(PayRefundStatusRespEnum.WAITING.getStatus()), 0);
    }

    @Test
    public void testSyncRefund_success() {
        assertEquals(testSyncRefund_waitingOrSuccessOrFailure(PayRefundStatusRespEnum.SUCCESS.getStatus()), 1);
    }

    @Test
    public void testSyncRefund_failure() {
        assertEquals(testSyncRefund_waitingOrSuccessOrFailure(PayRefundStatusRespEnum.FAILURE.getStatus()), 1);
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
            PayClient client = mock(PayClient.class);
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
        PayClient client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        // mock 方法（client 抛出异常）
        when(client.getRefund(eq("P110"), eq("R220"))).thenThrow(new RuntimeException());

        // 调用
        int count = refundService.syncRefund();
        // 断言
        assertEquals(count, 0);
    }

}
