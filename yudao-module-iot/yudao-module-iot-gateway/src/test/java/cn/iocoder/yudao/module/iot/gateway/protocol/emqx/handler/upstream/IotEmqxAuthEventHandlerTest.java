package cn.iocoder.yudao.module.iot.gateway.protocol.emqx.handler.upstream;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotEmqxAuthEventHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotEmqxAuthEventHandlerTest extends BaseMockitoUnitTest {

    private static final String SERVER_ID = "emqx-server";

    @Mock
    private IotDeviceMessageService deviceMessageService;
    @Mock
    private IotDeviceCommonApi deviceApi;

    private MockedStatic<SpringUtil> springUtilMockedStatic;
    private IotEmqxAuthEventHandler handler;

    @BeforeEach
    public void setUp() {
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotDeviceMessageService.class)).thenReturn(deviceMessageService);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotDeviceCommonApi.class)).thenReturn(deviceApi);
        handler = new IotEmqxAuthEventHandler(SERVER_ID, null);
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
    }

    @Test
    public void testHandleEvent_disconnectedTakenover() {
        // 调用：同一 ClientID 的新连接接管、丢弃旧会话
        handler.handleEvent(mockDisconnectedEvent("takenover"));
        handler.handleEvent(mockDisconnectedEvent("discarded"));
        // 断言：旧会话的断开被忽略，不发离线
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any(), any());
    }

    @Test
    public void testHandleEvent_disconnectedNormal() {
        // 调用：正常断开、心跳超时
        handler.handleEvent(mockDisconnectedEvent("normal"));
        handler.handleEvent(mockDisconnectedEvent("keepalive_timeout"));
        // 断言：发送离线
        verify(deviceMessageService, times(2)).sendDeviceMessage(argThat((IotDeviceMessage m) ->
                        IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod().equals(m.getMethod())
                                && String.valueOf(IotDeviceStateEnum.OFFLINE.getState()).equals(IotDeviceMessageUtils.getIdentifier(m))),
                eq("pk"), eq("dn"), eq(SERVER_ID));
    }

    private static RoutingContext mockDisconnectedEvent(String reason) {
        RoutingContext context = mock(RoutingContext.class, RETURNS_DEEP_STUBS);
        JsonObject body = new JsonObject().put("event", "client.disconnected")
                .put("username", "dn&pk").put("reason", reason);
        when(context.body().asJsonObject()).thenReturn(body);
        return context;
    }

}
