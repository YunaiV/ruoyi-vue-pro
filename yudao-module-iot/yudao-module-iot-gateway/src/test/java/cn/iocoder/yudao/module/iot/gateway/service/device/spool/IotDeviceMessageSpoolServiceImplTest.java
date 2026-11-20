package cn.iocoder.yudao.module.iot.gateway.service.device.spool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageRejectedException;
import cn.iocoder.yudao.module.iot.core.messagebus.core.local.IotLocalMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_MESSAGE_REJECTED;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_MESSAGE_SPOOL_REJECTED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceMessageSpoolServiceImpl} 的单元测试：使用临时目录的真实 SQLite 文件，消息总线为 mock
 *
 * @author 芋道源码
 */
public class IotDeviceMessageSpoolServiceImplTest extends BaseMockitoUnitTest {

    /**
     * 网关接收时刻：取当前时间，避免被当作超过保留时间的消息删除
     */
    private static final long T1 = System.currentTimeMillis();
    private static final long T2 = T1 + 1;

    @TempDir
    File tempDir;

    @InjectMocks
    private IotDeviceMessageSpoolServiceImpl spoolService;

    @Mock
    private IotDeviceMessageProducer deviceMessageProducer;

    private final IotGatewayProperties gatewayProperties = new IotGatewayProperties();
    private String file;

    @BeforeEach
    public void setUp() {
        // 不调用 init：不启动补传线程，由测试直接调用 drain
        gatewayProperties.getSpool().setEnabled(true);
        gatewayProperties.getSpool().setDrainRate(100_000);
        ReflectUtil.setFieldValue(spoolService, "gatewayProperties", gatewayProperties);
        ReflectUtil.setFieldValue(spoolService, "enabled", true);
        file = new File(tempDir, "spool/spool.db").getPath();
        spoolService.open(file);
    }

    @AfterEach
    public void tearDown() {
        spoolService.close();
    }

    @Test
    public void testIsSpoolableMessage() {
        IotDeviceMessage property = buildMessage("m1");
        IotDeviceMessage event = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.EVENT_POST.getMethod(), null);
        IotDeviceMessage pack = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod(), null);
        IotDeviceMessage reply = IotDeviceMessage.replyOf("r1", IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                null, 0, null);
        IotDeviceMessage state = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod(), null);
        IotDeviceMessage raw = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.RAW_POST.getMethod(), null);

        // 断言：各协议入口的属性、事件、批量上报
        for (IotProtocolTypeEnum protocol : IotProtocolTypeEnum.values()) {
            assertTrue(spoolService.isSpoolableMessage(property, protocol));
            assertTrue(spoolService.isSpoolableMessage(event, protocol));
            assertTrue(spoolService.isSpoolableMessage(pack, protocol));
        }
        // 断言：回执、状态、纯透传、未指定协议，不暂存
        assertFalse(spoolService.isSpoolableMessage(reply, IotProtocolTypeEnum.MQTT));
        assertFalse(spoolService.isSpoolableMessage(state, IotProtocolTypeEnum.TCP));
        assertFalse(spoolService.isSpoolableMessage(raw, IotProtocolTypeEnum.UDP));
        assertFalse(spoolService.isSpoolableMessage(property, null));
    }

    @Test
    public void testInit_localMessageBus() {
        // 准备参数：Local 消息总线（进程内）
        IotDeviceMessageSpoolServiceImpl localService = new IotDeviceMessageSpoolServiceImpl();
        ReflectUtil.setFieldValue(localService, "gatewayProperties", gatewayProperties);
        ReflectUtil.setFieldValue(localService, "messageBus", mock(IotLocalMessageBus.class));

        // 调用
        localService.init();

        // 断言：不启用离线暂存，保持直发
        assertFalse(localService.isSpoolableMessage(buildMessage("m1"), IotProtocolTypeEnum.MQTT));
        assertNull(ReflectUtil.getFieldValue(localService, "jdbcTemplate"));
    }

    @Test
    public void testSendSpoolableMessage_direct() {
        // 调用
        IotDeviceMessage message = buildMessage("m1");
        spoolService.sendSpoolableMessage(message, T1);

        // 断言：直发，不暂存
        verify(deviceMessageProducer).sendDeviceMessageConfirmed(same(message));
        assertNull(message.getSpoolTime());
        assertEquals(0L, getCount());
    }

    @Test
    public void testSendSpoolableMessage_spoolThenDrain() {
        // mock 方法：第一次直发失败，之后成功
        doThrow(new IllegalStateException("redis down")).doNothing()
                .when(deviceMessageProducer).sendDeviceMessageConfirmed(any());

        // 调用：第一条直发失败，写入暂存；第二条已有积压，直接写入暂存，不再直发
        spoolService.sendSpoolableMessage(buildMessage("m1"), T1);
        spoolService.sendSpoolableMessage(buildMessage("m2"), T2);
        // 断言
        verify(deviceMessageProducer, times(1)).sendDeviceMessageConfirmed(any());
        assertEquals(2L, getCount());

        // 调用：补传
        spoolService.drain();

        // 断言：按顺序补传，带上 spoolTime；补传后删除，恢复直发
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageProducer, times(3)).sendDeviceMessageConfirmed(captor.capture());
        List<IotDeviceMessage> drained = captor.getAllValues().subList(1, 3);
        assertEquals("m1", drained.get(0).getId());
        assertEquals(T1, drained.get(0).getSpoolTime());
        assertEquals("m2", drained.get(1).getId());
        assertEquals(T2, drained.get(1).getSpoolTime());
        assertEquals(0L, getCount());
        assertEquals(0L, (long) ReflectUtil.getFieldValue(spoolService, "bytes"));
        assertEquals(false, ReflectUtil.getFieldValue(spoolService, "spooling"));
    }

    @Test
    public void testDrain_sendFailureKeep() {
        // 准备参数：已有 2 条积压
        ReflectUtil.setFieldValue(spoolService, "spooling", true);
        spoolService.sendSpoolableMessage(buildMessage("m1"), T1);
        spoolService.sendSpoolableMessage(buildMessage("m2"), T2);
        // mock 方法：第一条成功，第二条失败
        doNothing().doThrow(new IllegalStateException("redis down"))
                .when(deviceMessageProducer).sendDeviceMessageConfirmed(any());

        // 调用
        spoolService.drain();

        // 断言：已发送的删除，失败的保留，仍在暂存中
        verify(deviceMessageProducer, times(2)).sendDeviceMessageConfirmed(any());
        assertEquals(1L, getCount());
        assertEquals(true, ReflectUtil.getFieldValue(spoolService, "spooling"));
    }

    @Test
    public void testDrain_badRecordDiscarded() {
        // 准备参数：第一条无法解析
        ReflectUtil.setFieldValue(spoolService, "spooling", true);
        ReflectUtil.invoke(spoolService, "insert", T1, "{bad json", 9);
        spoolService.sendSpoolableMessage(buildMessage("m2"), T2);

        // 调用
        spoolService.drain();

        // 断言：坏记录删除，不卡住后面的消息
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageProducer, times(1)).sendDeviceMessageConfirmed(captor.capture());
        assertEquals("m2", captor.getValue().getId());
        assertEquals(0L, getCount());
    }

    @Test
    public void testDrain_expired() {
        // 准备参数：第一条超过保留时间
        ReflectUtil.setFieldValue(spoolService, "spooling", true);
        spoolService.sendSpoolableMessage(buildMessage("m1"), T1 - Duration.ofDays(8).toMillis());
        spoolService.sendSpoolableMessage(buildMessage("m2"), T2);

        // 调用
        spoolService.drain();

        // 断言：过期的删除、不补传
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageProducer, times(1)).sendDeviceMessageConfirmed(captor.capture());
        assertEquals("m2", captor.getValue().getId());
        assertEquals(0L, getCount());
    }

    @Test
    public void testDrain_expiredBeforeSend() {
        // 准备参数：第一条超过保留时间；定期的过期删除尚未到间隔，不会先删掉它
        ReflectUtil.setFieldValue(spoolService, "spooling", true);
        spoolService.sendSpoolableMessage(buildMessage("m1"), T1 - Duration.ofDays(8).toMillis());
        spoolService.sendSpoolableMessage(buildMessage("m2"), T2);
        ReflectUtil.setFieldValue(spoolService, "lastExpireTime", System.currentTimeMillis());

        // 调用
        spoolService.drain();

        // 断言：发出前逐条检查，过期的跳过（删除）、不补传
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageProducer, times(1)).sendDeviceMessageConfirmed(captor.capture());
        assertEquals("m2", captor.getValue().getId());
        assertEquals(0L, getCount());
    }

    @Test
    public void testDrain_rejectedSkipped() {
        // 准备参数：已有 3 条积压，第一条超过消息总线的大小限制
        ReflectUtil.setFieldValue(spoolService, "spooling", true);
        spoolService.sendSpoolableMessage(buildMessage("m1"), T1);
        spoolService.sendSpoolableMessage(buildMessage("m2"), T2);
        spoolService.sendSpoolableMessage(buildMessage("m3"), T2);
        // mock 方法：第一条被永久拒绝，之后成功
        doThrow(new IotMessageRejectedException("too large", null)).doNothing()
                .when(deviceMessageProducer).sendDeviceMessageConfirmed(any());

        // 调用
        spoolService.drain();

        // 断言：被拒绝的跳过（删除），不阻塞后面的消息；补传完成后恢复直发
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageProducer, times(3)).sendDeviceMessageConfirmed(captor.capture());
        assertEquals("m1", captor.getAllValues().get(0).getId());
        assertEquals("m2", captor.getAllValues().get(1).getId());
        assertEquals("m3", captor.getAllValues().get(2).getId());
        assertEquals(0L, getCount());
        assertEquals(false, ReflectUtil.getFieldValue(spoolService, "spooling"));
    }

    @Test
    public void testDrain_closedConcurrently() {
        // 准备参数：已有 1 条积压
        ReflectUtil.setFieldValue(spoolService, "spooling", true);
        spoolService.sendSpoolableMessage(buildMessage("m1"), T1);
        // mock 方法：发送期间关闭暂存（与关闭竞争）
        doAnswer(invocation -> {
            spoolService.close();
            return null;
        }).when(deviceMessageProducer).sendDeviceMessageConfirmed(any());

        // 调用：不抛出异常
        spoolService.drain();

        // 断言：没能删除的消息保留在文件中，重新打开后仍在
        verify(deviceMessageProducer, times(1)).sendDeviceMessageConfirmed(any());
        assertEquals(true, ReflectUtil.getFieldValue(spoolService, "spooling"));
        spoolService.open(file);
        assertEquals(1L, getCount());
    }

    @Test
    public void testSendSpoolableMessage_rejected() {
        // mock 方法：直发被永久拒绝（例如说超过大小限制）
        doThrow(new IotMessageRejectedException("too large", null))
                .when(deviceMessageProducer).sendDeviceMessageConfirmed(any());

        // 调用，并断言：返回错误，不写入暂存，也不进入暂存中
        assertServiceException(() -> spoolService.sendSpoolableMessage(buildMessage("m1"), T1),
                DEVICE_MESSAGE_REJECTED);
        assertEquals(0L, getCount());
        assertEquals(false, ReflectUtil.getFieldValue(spoolService, "spooling"));
    }

    @Test
    public void testInit_invalidQuota() {
        // 准备参数
        IotDeviceMessageSpoolServiceImpl service = new IotDeviceMessageSpoolServiceImpl();
        ReflectUtil.setFieldValue(service, "gatewayProperties", gatewayProperties);
        ReflectUtil.setFieldValue(service, "messageBus", mock(IotMessageBus.class));

        // 调用，并断言：max-size 不大于 0
        gatewayProperties.getSpool().setMaxSize(DataSize.ofBytes(0));
        assertThrows(IllegalStateException.class, service::init);
        // 调用，并断言：max-age 不大于 0
        gatewayProperties.getSpool().setMaxSize(DataSize.ofMegabytes(1));
        gatewayProperties.getSpool().setMaxAge(Duration.ZERO);
        assertThrows(IllegalStateException.class, service::init);
        assertFalse(service.isSpoolableMessage(buildMessage("m1"), IotProtocolTypeEnum.MQTT));
    }

    @Test
    public void testInit_notConfirmable() {
        // 准备参数：消息总线不能确认发送（例如说 Kafka acks=0）
        IotMessageBus messageBus = mock(IotMessageBus.class);
        doThrow(new IllegalStateException("acks=0")).when(messageBus).validateConfirmable();
        IotDeviceMessageSpoolServiceImpl service = new IotDeviceMessageSpoolServiceImpl();
        ReflectUtil.setFieldValue(service, "gatewayProperties", gatewayProperties);
        ReflectUtil.setFieldValue(service, "messageBus", messageBus);

        // 调用，并断言：启动失败，不打开暂存
        assertThrows(IllegalStateException.class, service::init);
        assertFalse(service.isSpoolableMessage(buildMessage("m1"), IotProtocolTypeEnum.MQTT));
        assertNull(ReflectUtil.getFieldValue(service, "jdbcTemplate"));
    }

    @Test
    public void testSendSpoolableMessage_full() {
        // 准备参数：上限 1 字节，且已在暂存中
        gatewayProperties.getSpool().setMaxSize(DataSize.ofBytes(1));
        ReflectUtil.setFieldValue(spoolService, "spooling", true);

        // 调用，并断言：拒绝，不直发
        assertServiceException(() -> spoolService.sendSpoolableMessage(buildMessage("m1"), T1), DEVICE_MESSAGE_SPOOL_REJECTED, "暂存已满");
        verifyNoInteractions(deviceMessageProducer);
        assertEquals(0L, getCount());
    }

    @Test
    public void testSendSpoolableMessage_fileUnavailable() {
        // 准备参数：暂存文件打开失败
        spoolService.close();
        doThrow(new IllegalStateException("redis down")).when(deviceMessageProducer).sendDeviceMessageConfirmed(any());

        // 调用，并断言：直发失败后拒绝，不能静默成功
        assertServiceException(() -> spoolService.sendSpoolableMessage(buildMessage("m1"), T1),
                DEVICE_MESSAGE_SPOOL_REJECTED, "暂存文件不可用");
    }

    @Test
    public void testOpen_reopenKeepsRecords() {
        // 准备参数：写入后关闭
        ReflectUtil.setFieldValue(spoolService, "spooling", true);
        spoolService.sendSpoolableMessage(buildMessage("m1"), T1);
        spoolService.close();
        ReflectUtil.setFieldValue(spoolService, "spooling", false);

        // 调用：重新打开
        spoolService.open(file);

        // 断言：从文件统计已暂存的消息，处于暂存中；补传原来的消息
        assertEquals(1L, getCount());
        assertEquals(true, ReflectUtil.getFieldValue(spoolService, "spooling"));
        spoolService.drain();
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageProducer).sendDeviceMessageConfirmed(captor.capture());
        assertEquals("m1", captor.getValue().getId());
        assertEquals(T1, captor.getValue().getSpoolTime());
    }

    @Test
    public void testOpen_corruptedFile() {
        // 准备参数：不是 SQLite 的文件
        spoolService.close();
        File corrupted = new File(tempDir, "corrupted.db");
        FileUtil.writeString("not a sqlite database file, just some garbage bytes", corrupted, StandardCharsets.UTF_8);

        // 调用，并断言：打开失败，文件保留，不可写入
        assertThrows(RuntimeException.class, () -> spoolService.open(corrupted.getPath()));
        assertTrue(corrupted.exists());
        assertNull(ReflectUtil.getFieldValue(spoolService, "jdbcTemplate"));
    }

    private long getCount() {
        return (long) ReflectUtil.getFieldValue(spoolService, "count");
    }

    private static IotDeviceMessage buildMessage(String id) {
        return IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                Collections.singletonMap("t", 1)).setId(id);
    }

}
