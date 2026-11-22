package cn.iocoder.yudao.module.infra.controller.admin.sse;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.sse.config.SseProperties;
import cn.iocoder.yudao.framework.sse.core.sender.SseMessageSender;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.websocket.config.WebSocketProperties;
import cn.iocoder.yudao.module.infra.controller.admin.sse.vo.SseConfigRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.sse.vo.SseDemoSendReqVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * {@link SseController} 的单元测试
 *
 * @author 芋道源码
 */
public class SseControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SseController sseController;

    @Mock
    private SseMessageSender sseMessageSender;

    @Test
    public void testGetSseConfig_disabled() {
        // mock 方法：WebSocket、SSE 都关闭
        ReflectUtil.setFieldValue(sseController, "sseMessageSender", null);

        // 调用
        SseConfigRespVO config = sseController.getSseConfig().getData();
        // 断言
        assertFalse(config.getWebsocketEnabled());
        assertFalse(config.getSseEnabled());
        assertNull(config.getSsePath());
        assertNull(config.getSseHeartbeatInterval());
    }

    @Test
    public void testGetSseConfig_enabled() {
        // mock 方法：WebSocket、SSE 都开启
        SseProperties sseProperties = new SseProperties();
        sseProperties.setPath("/infra/sse2");
        ReflectUtil.setFieldValue(sseController, "sseProperties", sseProperties);
        ReflectUtil.setFieldValue(sseController, "webSocketProperties", new WebSocketProperties());

        // 调用
        SseConfigRespVO config = sseController.getSseConfig().getData();
        // 断言
        assertTrue(config.getWebsocketEnabled());
        assertTrue(config.getSseEnabled());
        assertEquals("/infra/sse2", config.getSsePath());
        assertEquals(25000L, config.getSseHeartbeatInterval());
        assertEquals(5000L, config.getSseRetryInterval());
    }

    @Test
    public void testSendDemoMessage_toUser() {
        // 准备参数
        SseDemoSendReqVO reqVO = new SseDemoSendReqVO();
        reqVO.setToUserId(1L);
        reqVO.setText("hello");
        // mock 方法
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        when(sseMessageSender.send(isNull(), eq(UserTypeEnum.ADMIN.getValue()), eq(1L),
                eq(SseController.DEMO_MESSAGE_TYPE), contentCaptor.capture())).thenReturn("event-1");

        // 调用
        CommonResult<String> result = sseController.sendDemoMessage(reqVO);
        // 断言
        assertEquals("event-1", result.getData());
        assertTrue(contentCaptor.getValue().contains("\"text\":\"hello\""));
        assertTrue(contentCaptor.getValue().contains("\"single\":true"));
    }

    @Test
    public void testSendDemoMessage_allAdmins() {
        // 准备参数
        SseDemoSendReqVO reqVO = new SseDemoSendReqVO();
        reqVO.setText("hello");
        // mock 方法
        when(sseMessageSender.send(isNull(), eq(UserTypeEnum.ADMIN.getValue()), isNull(),
                eq(SseController.DEMO_MESSAGE_TYPE), contains("\"single\":false"))).thenReturn("event-2");

        // 调用
        CommonResult<String> result = sseController.sendDemoMessage(reqVO);
        // 断言
        assertEquals("event-2", result.getData());
    }

    @Test
    public void testSendDemoMessage_sseDisabled() {
        // 准备参数
        SseDemoSendReqVO reqVO = new SseDemoSendReqVO();
        reqVO.setText("hello");
        // mock 方法：SSE 关闭
        ReflectUtil.setFieldValue(sseController, "sseMessageSender", null);

        // 调用，并断言异常
        ServiceException ex = assertThrows(ServiceException.class, () -> sseController.sendDemoMessage(reqVO));
        assertEquals(NOT_IMPLEMENTED.getCode(), ex.getCode());
    }

}
