package cn.iocoder.yudao.module.yaya.service.pay;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderRespVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.member.YayaMemberEntitlementDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.pay.YayaMemberOrderDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.member.YayaMemberEntitlementMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.pay.YayaMemberOrderMapper;
import cn.iocoder.yudao.module.yaya.service.member.YayaEntitlementServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_MEMBER_ORDER_PAY_MISMATCH;

@Import({YayaMemberOrderServiceImpl.class, YayaEntitlementServiceImpl.class})
class YayaMemberOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private YayaMemberOrderServiceImpl memberOrderService;
    @Resource
    private YayaMemberOrderMapper memberOrderMapper;
    @Resource
    private YayaMemberEntitlementMapper entitlementMapper;
    @MockBean
    private PayOrderApi payOrderApi;

    @Test
    void createMemberOrderShouldCreateLocalOrderAndPayOrder() {
        when(payOrderApi.createOrder(any(PayOrderCreateReqDTO.class))).thenReturn(9001L);

        YayaAppMemberOrderRespVO response = memberOrderService.createMemberOrder(10001L,
                new YayaAppMemberOrderCreateReqVO().setPlanKey("monthly-pro").setChannelCode("mock"),
                "127.0.0.1");

        assertNotNull(response.getOrderId());
        assertEquals(9001L, response.getPayOrderId());
        assertEquals("WAITING_PAYMENT", response.getStatus());
        YayaMemberOrderDO order = memberOrderMapper.selectById(response.getOrderId());
        assertEquals(10001L, order.getMemberUserId());
        assertEquals("monthly-pro", order.getPlanKey());
        assertEquals(3900L, order.getPriceCent());
        assertEquals("mock", order.getChannelCode());
        assertEquals("WAITING_PAYMENT", order.getStatus());

        ArgumentCaptor<PayOrderCreateReqDTO> captor = ArgumentCaptor.forClass(PayOrderCreateReqDTO.class);
        verify(payOrderApi).createOrder(captor.capture());
        PayOrderCreateReqDTO reqDTO = captor.getValue();
        assertEquals("yaya", reqDTO.getAppKey());
        assertEquals("127.0.0.1", reqDTO.getUserIp());
        assertEquals(10001L, reqDTO.getUserId());
        assertEquals(UserTypeEnum.MEMBER.getValue(), reqDTO.getUserType());
        assertEquals(response.getOrderId().toString(), reqDTO.getMerchantOrderId());
        assertEquals("Monthly Pro", reqDTO.getSubject());
        assertEquals(3900, reqDTO.getPrice());
        assertTrue(reqDTO.getExpireTime().isAfter(LocalDateTime.now()));
    }

    @Test
    void activateEntitlementByPayOrderShouldGrantOnce() {
        when(payOrderApi.createOrder(any(PayOrderCreateReqDTO.class))).thenReturn(9002L);
        YayaAppMemberOrderRespVO response = memberOrderService.createMemberOrder(10002L,
                new YayaAppMemberOrderCreateReqVO().setPlanKey("monthly-pro").setChannelCode("mock"),
                "127.0.0.1");
        when(payOrderApi.getOrder(9002L)).thenReturn(payOrder(9002L, response.getOrderId(),
                3900, PayOrderStatusEnum.SUCCESS.getStatus(), "mock"));

        memberOrderService.activateEntitlementByPayOrder(response.getOrderId(), 9002L);
        memberOrderService.activateEntitlementByPayOrder(response.getOrderId(), 9002L);

        YayaMemberOrderDO order = memberOrderMapper.selectById(response.getOrderId());
        assertEquals("PAID", order.getStatus());
        assertEquals("mock", order.getPayChannelCode());
        assertNotNull(order.getPaidAt());
        List<YayaMemberEntitlementDO> entitlements = entitlementMapper.selectList();
        assertEquals(1, entitlements.size());
        YayaMemberEntitlementDO entitlement = entitlements.get(0);
        assertEquals(10002L, entitlement.getMemberUserId());
        assertEquals("pay_order:9002", entitlement.getIdempotencyKey());
        assertTrue(entitlement.getEndsAt().isAfter(LocalDateTime.now().plusDays(29)));
    }

    @Test
    void activateEntitlementByPayOrderShouldIgnoreUnpaidPayOrder() {
        when(payOrderApi.createOrder(any(PayOrderCreateReqDTO.class))).thenReturn(9003L);
        YayaAppMemberOrderRespVO response = memberOrderService.createMemberOrder(10003L,
                new YayaAppMemberOrderCreateReqVO().setPlanKey("monthly-pro").setChannelCode("mock"),
                "127.0.0.1");
        when(payOrderApi.getOrder(9003L)).thenReturn(payOrder(9003L, response.getOrderId(),
                3900, PayOrderStatusEnum.WAITING.getStatus(), "mock"));

        memberOrderService.activateEntitlementByPayOrder(response.getOrderId(), 9003L);

        YayaMemberOrderDO order = memberOrderMapper.selectById(response.getOrderId());
        assertEquals("WAITING_PAYMENT", order.getStatus());
        assertTrue(entitlementMapper.selectList().isEmpty());
    }

    @Test
    void activateEntitlementByPayOrderShouldRejectForgedMerchantOrderId() {
        when(payOrderApi.createOrder(any(PayOrderCreateReqDTO.class))).thenReturn(9004L, 9005L);
        YayaAppMemberOrderRespVO firstOrder = memberOrderService.createMemberOrder(10004L,
                new YayaAppMemberOrderCreateReqVO().setPlanKey("monthly-pro").setChannelCode("mock"),
                "127.0.0.1");
        YayaAppMemberOrderRespVO secondOrder = memberOrderService.createMemberOrder(10005L,
                new YayaAppMemberOrderCreateReqVO().setPlanKey("monthly-pro").setChannelCode("mock"),
                "127.0.0.1");
        when(payOrderApi.getOrder(9004L)).thenReturn(payOrder(9004L, firstOrder.getOrderId(),
                3900, PayOrderStatusEnum.SUCCESS.getStatus(), "mock"));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> memberOrderService.activateEntitlementByPayOrder(secondOrder.getOrderId(), 9004L));

        assertEquals(YAYA_MEMBER_ORDER_PAY_MISMATCH.getCode(), exception.getCode());
        assertEquals("WAITING_PAYMENT", memberOrderMapper.selectById(secondOrder.getOrderId()).getStatus());
        assertTrue(entitlementMapper.selectList().isEmpty());
        verify(payOrderApi, never()).getOrder(9004L);
    }

    @Test
    void memberOrderShouldDeclareTenantScope() {
        assertTrue(TenantBaseDO.class.isAssignableFrom(YayaMemberOrderDO.class));
    }

    private static PayOrderRespDTO payOrder(Long payOrderId, Long memberOrderId, Integer price, Integer status,
                                            String channelCode) {
        PayOrderRespDTO payOrder = new PayOrderRespDTO();
        payOrder.setId(payOrderId);
        payOrder.setMerchantOrderId(memberOrderId.toString());
        payOrder.setPrice(price);
        payOrder.setStatus(status);
        payOrder.setChannelCode(channelCode);
        payOrder.setSuccessTime(LocalDateTime.now());
        return payOrder;
    }

}
