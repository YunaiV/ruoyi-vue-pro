package cn.iocoder.yudao.module.pay.service.refund;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    private PayClientFactory payClientFactory;
    @MockBean
    private PayOrderService orderService;
    @MockBean
    private PayAppService appService;
    @MockBean
    private PayChannelService channelService;
    @MockBean
    private PayNotifyService notifyService;

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
                o -> o.setAppId(1L));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq(1L))).thenReturn(app);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
                ORDER_NOT_FOUND);
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
                o -> o.setAppId(1L).setMerchantOrderId("100"));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq(1L))).thenReturn(app);
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(status));
        when(orderService.getOrder(eq(1L), eq("100"))).thenReturn(order);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
                ORDER_REFUND_FAIL_STATUS_ERROR);
    }

    @Test
    public void testCreateRefund_refundPriceExceed() {
        // 准备参数
        PayRefundCreateReqDTO reqDTO = randomPojo(PayRefundCreateReqDTO.class,
                o -> o.setAppId(1L).setMerchantOrderId("100").setPrice(10));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq(1L))).thenReturn(app);
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
                o -> o.setAppId(1L).setMerchantOrderId("100").setPrice(10));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq(1L))).thenReturn(app);
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
                o -> o.setAppId(1L).setMerchantOrderId("100").setPrice(9));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq(1L))).thenReturn(app);
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
                o -> o.setAppId(1L).setMerchantOrderId("100").setPrice(9)
                        .setReason("测试退款"));
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L));
        when(appService.validPayApp(eq(1L))).thenReturn(app);
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
        when(payClientFactory.getPayClient(eq(10L))).thenReturn(client);
        // mock 数据（refund 已存在）
        PayRefundDO refund = randomPojo(PayRefundDO.class, o ->
                o.setOrderId(order.getId()).setStatus(PayOrderStatusEnum.WAITING.getStatus()));
        refundMapper.insert(refund);

        // 调用，并断言异常
        assertServiceException(() -> refundService.createPayRefund(reqDTO),
                REFUND_EXISTS);
    }

    @Test
    public void testCreateRefund_invokeException() {

    }

    @Test
    public void testCreateRefund_invokeSuccess() {

    }

}
