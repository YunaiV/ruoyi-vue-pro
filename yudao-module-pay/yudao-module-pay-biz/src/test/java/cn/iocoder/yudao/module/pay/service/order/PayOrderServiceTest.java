package cn.iocoder.yudao.module.pay.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.config.PayProperties;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderExtensionMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link PayOrderServiceImpl} 的单元测试类
 *
 * @author 芋艿
 */
@Import({PayOrderServiceImpl.class})
public class PayOrderServiceTest extends BaseDbUnitTest {

    @Resource
    private PayOrderServiceImpl orderService;

    @Resource
    private PayOrderMapper orderMapper;
    @Resource
    private PayOrderExtensionMapper orderExtensionMapper;

    @MockBean
    private PayClientFactory payClientFactory;
    @MockBean
    private PayProperties properties;
    @MockBean
    private PayAppService appService;
    @MockBean
    private PayChannelService channelService;
    @MockBean
    private PayNotifyService notifyService;

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
                o -> o.setAppId(1L).setMerchantOrderId("10")
                        .setSubject(randomString()).setBody(randomString()));
        // mock 方法
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L).setOrderNotifyUrl("http://127.0.0.1"));
        when(appService.validPayApp(eq(reqDTO.getAppId()))).thenReturn(app);

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
                o -> o.setAppId(1L).setMerchantOrderId("10"));
        // mock 数据
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class,  o -> o.setAppId(1L).setMerchantOrderId("10"));
        orderMapper.insert(dbOrder);

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
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), ORDER_NOT_FOUND);
    }

    @Test
    public void testSubmitOrder_notWaiting() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
        orderMapper.insert(order);
        // 准备参数
        PayOrderSubmitReqVO reqVO = randomPojo(PayOrderSubmitReqVO.class, o -> o.setId(order.getId()));
        String userIp = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), ORDER_STATUS_IS_NOT_WAITING);
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
        assertServiceException(() -> orderService.submitOrder(reqVO, userIp), ORDER_IS_EXPIRED);
    }

    @Test
    public void testSubmitOrder_channelNotFound() {
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setAppId(1L));
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
        // mock 数据（order）
        PayOrderDO order = randomPojo(PayOrderDO.class, o -> o.setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setAppId(1L));
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
        PayClient client = mock(PayClient.class);
        when(payClientFactory.getPayClient(eq(10L))).thenReturn(client);
        when(client.unifiedOrder(any(PayOrderUnifiedReqDTO.class))).thenThrow(new NullPointerException());

        // 调用，并断言异常
        assertThrows(NullPointerException.class, () -> orderService.submitOrder(reqVO, userIp));
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

    @Test // 【成功】支付结果为等待中
    public void testSubmitOrder_waitingResult() {

    }

    @Test // 【成功】支付结果为已完成
    public void testSubmitOrder_successResult() {

    }

    @Test // 【成功】支付结果为已关闭
    public void testSubmitOrder_closedResult() {

    }

}
