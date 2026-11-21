package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotOpcUaDeviceStateTracker} 的单元测试：关闭后已排队的状态消息不再发送
 *
 * @author 芋道源码
 */
public class IotOpcUaDeviceStateTrackerTest {

    private Vertx vertx;
    private Context context;
    private IotDeviceMessageService messageService;
    private IotOpcUaDeviceStateTracker stateTracker;

    @BeforeEach
    public void setUp() {
        vertx = Vertx.vertx();
        context = vertx.getOrCreateContext();
        messageService = mock(IotDeviceMessageService.class);
        stateTracker = new IotOpcUaDeviceStateTracker(vertx, context, messageService, "server-1", 1000);
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    @Test
    public void testClose_queuedMessagesNotSent() throws Exception {
        // mock 方法：第一台设备的上线消息发送阻塞，第二台设备的排在它之后
        CountDownLatch sending = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            sending.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return null;
        }).when(messageService).sendDeviceMessage(any(), eq("pk"), eq("dev1"), eq("server-1"));
        runOnContext(() -> {
            stateTracker.online(newDevice(1L));
            stateTracker.online(newDevice(2L));
        });
        assertTrue(sending.await(5, TimeUnit.SECONDS));

        // 调用：协议停止
        runOnContext(stateTracker::close);
        release.countDown();

        // 断言：执行中的消息照常完成，排队中的第二台设备不再发送
        verify(messageService, timeout(5000)).sendDeviceMessage(any(), eq("pk"), eq("dev1"), eq("server-1"));
        Thread.sleep(300);
        verify(messageService, never()).sendDeviceMessage(any(), eq("pk"), eq("dev2"), any());
    }

    private static IotOpcUaDeviceConfigRespDTO newDevice(Long deviceId) {
        IotOpcUaDeviceConfigRespDTO device = new IotOpcUaDeviceConfigRespDTO();
        device.setDeviceId(deviceId);
        device.setTenantId(1L);
        device.setProductKey("pk");
        device.setDeviceName("dev" + deviceId);
        return device;
    }

    private void runOnContext(Runnable runnable) throws Exception {
        CompletableFuture<Void> future = new CompletableFuture<>();
        context.runOnContext(v -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        future.get(5, TimeUnit.SECONDS);
    }

}
