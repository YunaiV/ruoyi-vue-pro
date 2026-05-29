package cn.iocoder.yudao.module.yaya.controller.app.pay;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderRespVO;
import cn.iocoder.yudao.module.yaya.service.pay.YayaMemberOrderService;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaAppPayControllerTest {

    @Test
    void shouldExposeExpectedMappings() throws Exception {
        RequestMapping root = YayaAppPayController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/yaya/pay"}, root.value());

        PostMapping create = YayaAppPayController.class
                .getMethod("createMemberOrder", YayaAppMemberOrderCreateReqVO.class)
                .getAnnotation(PostMapping.class);
        assertArrayEquals(new String[]{"/member-orders"}, create.value());

        Method notifyOrder = YayaAppPayController.class
                .getMethod("notifyOrderPaid", PayOrderNotifyReqDTO.class);
        PostMapping notify = notifyOrder.getAnnotation(PostMapping.class);
        assertArrayEquals(new String[]{"/notify/order"}, notify.value());
        assertNotNull(notifyOrder.getParameters()[0].getAnnotation(RequestBody.class));
        assertNotNull(notifyOrder.getAnnotation(PermitAll.class));
    }

    @Test
    void createMemberOrderShouldDelegateToService() {
        YayaMemberOrderService service = mock(YayaMemberOrderService.class);
        YayaAppPayController controller = new YayaAppPayController();
        ReflectionTestUtils.setField(controller, "memberOrderService", service);
        YayaAppMemberOrderCreateReqVO reqVO = new YayaAppMemberOrderCreateReqVO()
                .setPlanKey("monthly-pro")
                .setChannelCode("mock");
        when(service.createMemberOrder(isNull(), same(reqVO), nullable(String.class)))
                .thenReturn(new YayaAppMemberOrderRespVO()
                        .setOrderId(1L)
                        .setPayOrderId(9001L)
                        .setStatus("WAITING_PAYMENT"));

        YayaAppMemberOrderRespVO response = controller.createMemberOrder(reqVO).getData();

        assertEquals(1L, response.getOrderId());
        assertEquals(9001L, response.getPayOrderId());
        assertEquals("WAITING_PAYMENT", response.getStatus());
        verify(service).createMemberOrder(isNull(), same(reqVO), nullable(String.class));
    }

    @Test
    void notifyOrderPaidShouldActivateEntitlement() {
        YayaMemberOrderService service = mock(YayaMemberOrderService.class);
        YayaAppPayController controller = new YayaAppPayController();
        ReflectionTestUtils.setField(controller, "memberOrderService", service);
        PayOrderNotifyReqDTO reqDTO = PayOrderNotifyReqDTO.builder()
                .merchantOrderId("2")
                .payOrderId(1L)
                .build();

        CommonResult<Boolean> result = controller.notifyOrderPaid(reqDTO);

        assertTrue(result.getData());
        verify(service).activateEntitlementByPayOrder(2L, 1L);
    }

}
