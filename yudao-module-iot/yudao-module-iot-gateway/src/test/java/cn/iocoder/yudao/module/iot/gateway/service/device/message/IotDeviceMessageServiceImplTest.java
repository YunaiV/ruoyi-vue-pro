package cn.iocoder.yudao.module.iot.gateway.service.device.message;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.script.IotScriptExecutor;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.dto.IotDownstreamEncodeRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceMessageServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class IotDeviceMessageServiceImplTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 10L;
    private static final Long TENANT_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final String PRODUCT_KEY = "pk";
    private static final String DEVICE_NAME = "dn";
    private static final String SERVER_ID = "server-1";
    private static final byte[] PAYLOAD = new byte[]{0x01, 0x02, (byte) 0xFF};
    private static final String RAW = Base64.getEncoder().encodeToString(PAYLOAD);

    private static final String SCRIPT = "function decode(ctx) {}";

    private static final String DECODE_PROPERTY_POST = "{\"method\":\"thing.property.post\",\"params\":{\"temperature\":25.5}}";

    @InjectMocks
    private IotDeviceMessageServiceImpl deviceMessageService;

    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDeviceMessageProducer deviceMessageProducer;
    @Mock
    private IotMessageSerializerManager messageSerializerManager;
    @Mock
    private IotScriptExecutor scriptExecutor;

    private final IotGatewayProperties gatewayProperties = new IotGatewayProperties();
    private final IotDeviceRespDTO device = new IotDeviceRespDTO().setId(DEVICE_ID).setTenantId(TENANT_ID)
            .setProductId(PRODUCT_ID).setProductKey(PRODUCT_KEY).setDeviceName(DEVICE_NAME)
            .setProtocolType(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType())
            .setScriptEnabled(true).setScriptContent(SCRIPT);
    private final IotScriptSource source = IotScriptSource.of(PRODUCT_ID, SCRIPT);

    @BeforeEach
    public void setUp() {
        ReflectUtil.setFieldValue(deviceMessageService, "gatewayProperties", gatewayProperties);
        ReflectUtil.setFieldValue(deviceMessageService, "scriptExecutor", scriptExecutor);
    }

    // ========== sendRawMessage ==========

    @Test
    public void testSendRawMessage_decodeSuccess() {
        // 准备参数：截止 = 收到的时刻 + gateway.script.deadline
        gatewayProperties.getScript().setEnabled(true);
        long receivedNanos = System.nanoTime();
        long deadlineNanos = receivedNanos + gatewayProperties.getScript().getDeadline().toNanos();
        // mock 方法
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), eq(deadlineNanos)))
                .thenReturn(IotScriptResult.success(DECODE_PROPERTY_POST, 1));

        // 调用
        String messageId = deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, receivedNanos);

        // 断言：只投递 1 条解析出的标准消息，raw 带上原始帧
        IotDeviceMessage message = captureSentMessage();
        assertEquals(messageId, message.getId());
        assertNotNull(message.getId());
        assertEquals(message.getId(), message.getRequestId()); // 没有 requestId 时默认 = id
        assertNotNull(message.getReportTime());
        assertEquals("thing.property.post", message.getMethod());
        assertEquals(Collections.singletonMap("temperature", 25.5), message.getParams());
        assertEquals(RAW, message.getRaw());
        assertDeviceIdentity(message);
        // 断言：decode 的入参
        ArgumentCaptor<String> ctxCaptor = ArgumentCaptor.forClass(String.class);
        verify(scriptExecutor).execute(sameScript(), eq("decode"), ctxCaptor.capture(), eq(deadlineNanos));
        JsonNode ctx = JsonUtils.parseTree(ctxCaptor.getValue());
        assertEquals("decode", ctx.get("direction").asText());
        assertEquals(IotProtocolTypeEnum.TCP.getType(), ctx.get("protocol").asText());
        assertEquals(PRODUCT_KEY, ctx.get("productKey").asText());
        assertEquals(DEVICE_NAME, ctx.get("deviceName").asText());
        assertEquals("0102FF", ctx.get("hex").asText());
        assertEquals(255, ctx.get("bytes").get(2).asInt());
        assertNull(ctx.get("request"));
    }

    @Test
    public void testSendRawMessage_decodeKeepRequestId() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        // mock 方法
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.success("{\"method\":\"thing.event.post\",\"requestId\":\"dev-1\","
                        + "\"params\":{\"identifier\":\"alarm\",\"value\":{\"level\":2}}}", 1));

        // 调用
        String messageId = deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.MQTT, System.nanoTime());

        // 断言：脚本返回的 requestId 保留，id 由服务端生成
        IotDeviceMessage message = captureSentMessage();
        assertEquals(messageId, message.getId());
        assertEquals("dev-1", message.getRequestId());
        assertNotEquals("dev-1", message.getId());
        assertEquals("thing.event.post", message.getMethod());
        assertEquals(RAW, message.getRaw());
        assertDeviceIdentity(message);
    }

    @Test
    public void testSendRawMessage_decodeReturnNull() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        // mock 方法
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.success(null, 1));

        // 调用
        String messageId = deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime());

        // 断言：只投递 1 条 thing.raw.post
        IotDeviceMessage message = captureSentMessage();
        assertEquals(messageId, message.getId());
        assertRawPost(message);
    }

    @Test
    public void testSendRawMessage_scriptError() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        // mock 方法
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.error("boom", 1));

        // 调用，并断言异常
        assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime()), SCRIPT_EXECUTE_FAILED, "boom");
        // 断言：解析失败不投递任何消息（只记错误日志）
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawMessage_scriptTimeout() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        // mock 方法
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.timeout("超过执行预算"));

        // 调用，并断言异常
        assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime()), SCRIPT_TIMEOUT);
        // 断言：解析失败不投递任何消息（只记错误日志）
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawMessage_scriptRejected() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        // mock 方法
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.rejected("已熔断"));

        // 调用，并断言异常
        assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime()), SCRIPT_REJECTED);
        // 断言：解析失败不投递任何消息（只记错误日志）
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawMessage_scriptResultOutputInvalid() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        // mock 方法：执行器自己判定输出不合法（例如结果过大）
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.of(IotScriptResult.Status.OUTPUT_INVALID, "输出过大", -1, 1));

        // 调用，并断言异常
        assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime()), SCRIPT_OUTPUT_INVALID, "输出过大");
        // 断言：解析失败不投递任何消息（只记错误日志）
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawMessage_outputInvalid() {
        // 准备参数：脚本试图伪造可信字段（raw、deviceId、id、tenantId、serverId、reportTime）、返回 messages 包装、ack、
        //         非对象、不支持的方法、缺少 params
        gatewayProperties.getScript().setEnabled(true);
        List<String> outputs = Arrays.asList(
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":1},\"raw\":\"AQID\"}",
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":1},\"deviceId\":999}",
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":1},\"id\":\"forged\"}",
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":1},\"tenantId\":999}",
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":1},\"serverId\":\"other\"}",
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":1},\"reportTime\":1}",
                "{\"messages\":[{\"method\":\"thing.property.post\",\"params\":{\"t\":1}}]}",
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":1},\"ack\":[1]}",
                "[{\"method\":\"thing.property.post\",\"params\":{\"t\":1}}]",
                "{\"method\":\"thing.raw.post\",\"params\":{\"t\":1}}",
                "{\"method\":\"thing.property.post\"}");
        // mock 方法
        mockDevice();
        for (String output : outputs) {
            when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), anyLong()))
                    .thenReturn(IotScriptResult.success(output, 1));

            // 调用
            ServiceException exception = assertThrows(ServiceException.class, () -> deviceMessageService.sendRawMessage(
                    PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID, IotProtocolTypeEnum.TCP, System.nanoTime()), output);

            // 断言：抛出 SCRIPT_OUTPUT_INVALID
            assertEquals(SCRIPT_OUTPUT_INVALID.getCode(), exception.getCode(), output);
        }
        // 断言：解析失败不投递任何消息（伪造的可信字段也不会被投递出去）
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawMessage_switchOff() {
        // 准备参数：功能开关默认关闭
        // mock 方法
        mockDevice();

        // 调用
        String messageId = deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime());

        // 断言：只投递 1 条 thing.raw.post；不执行脚本
        IotDeviceMessage message = captureSentMessage();
        assertEquals(messageId, message.getId());
        assertRawPost(message);
        verifyNoInteractions(scriptExecutor);
    }

    @Test
    public void testSendRawMessage_scriptDisabled() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        device.setScriptEnabled(false);
        // mock 方法
        mockDevice();

        // 调用
        String messageId = deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime());

        // 断言：只投递 1 条 thing.raw.post；不执行脚本
        IotDeviceMessage message = captureSentMessage();
        assertEquals(messageId, message.getId());
        assertRawPost(message);
        verifyNoInteractions(scriptExecutor);
    }

    @Test
    public void testSendRawMessage_scriptNotConfigured() {
        // 准备参数：产品从未配置脚本
        gatewayProperties.getScript().setEnabled(true);
        device.setScriptEnabled(null).setScriptContent(null);
        // mock 方法
        mockDevice();

        // 调用
        deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime());

        // 断言：与停用一致，只投递 1 条 thing.raw.post；不执行脚本
        assertRawPost(captureSentMessage());
        verifyNoInteractions(scriptExecutor);
    }

    @Test
    public void testSendRawMessage_scriptContentEmpty() {
        // 准备参数：已启用，但脚本内容为空（配置不合法）
        gatewayProperties.getScript().setEnabled(true);
        // mock 方法
        mockDevice();
        for (String content : Arrays.asList(null, "", "  ")) {
            device.setScriptContent(content);

            // 调用，并断言异常：fail-closed，不当作停用
            assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                    IotProtocolTypeEnum.TCP, System.nanoTime()), SCRIPT_EXECUTE_FAILED, "已启用但脚本内容为空");
        }
        // 断言：不投递任何消息（只记错误日志），也不执行脚本
        verifyNoInteractions(deviceMessageProducer, scriptExecutor);
    }

    @Test
    public void testSendRawMessage_queuedPastDeadline() {
        // 准备参数：开启功能，收到的时刻已早于截止时长之前（在 worker 队列里排队期间到期）
        gatewayProperties.getScript().setEnabled(true);
        long receivedNanos = System.nanoTime() - gatewayProperties.getScript().getDeadline().toNanos() - 1;
        // mock 方法
        mockDevice();

        // 调用，并断言异常
        assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, receivedNanos), SCRIPT_TIMEOUT);
        // 断言：不投递任何消息（只记错误日志），也不执行脚本
        verifyNoInteractions(deviceMessageProducer, scriptExecutor);
    }

    @Test
    public void testSendRawMessage_scriptFinishedPastDeadline() {
        // 准备参数：截止 500ms，脚本执行到截止之后才返回
        gatewayProperties.getScript().setEnabled(true).setDeadline(Duration.ofMillis(500));
        long receivedNanos = System.nanoTime();
        long deadlineNanos = receivedNanos + TimeUnit.MILLISECONDS.toNanos(500);
        // mock 方法
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), eq(deadlineNanos))).thenAnswer(invocation -> {
            sleepUntil(deadlineNanos);
            return IotScriptResult.success(DECODE_PROPERTY_POST, 300);
        });

        // 调用，并断言异常
        assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, receivedNanos), SCRIPT_TIMEOUT);
        // 断言：迟到的业务消息不投递，也不投递原始消息（只记错误日志）
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawMessage_scriptReturnNullPastDeadline() {
        // 准备参数：截止 500ms，脚本执行到截止之后才返回 null
        gatewayProperties.getScript().setEnabled(true).setDeadline(Duration.ofMillis(500));
        long receivedNanos = System.nanoTime();
        long deadlineNanos = receivedNanos + TimeUnit.MILLISECONDS.toNanos(500);
        // mock 方法
        mockDevice();
        when(scriptExecutor.execute(sameScript(), eq("decode"), anyString(), eq(deadlineNanos))).thenAnswer(invocation -> {
            sleepUntil(deadlineNanos);
            return IotScriptResult.success(null, 300);
        });

        // 调用，并断言异常
        assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, receivedNanos), SCRIPT_TIMEOUT);
        // 断言：迟到的 null 结果不记为 SUCCESS，不投递任何消息（只记错误日志）
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawMessage_payloadInvalid() {
        // 调用，并断言：长度 0、4097、null 都直接拒绝，不查设备、不投递
        for (byte[] payload : Arrays.asList(new byte[0], new byte[4097], null)) {
            ServiceException exception = assertThrows(ServiceException.class, () -> deviceMessageService.sendRawMessage(
                    payload, PRODUCT_KEY, DEVICE_NAME, SERVER_ID, IotProtocolTypeEnum.TCP, System.nanoTime()));
            assertEquals(BAD_REQUEST.getCode(), exception.getCode());
        }
        verifyNoInteractions(deviceService, deviceMessageProducer, scriptExecutor);
    }

    @Test
    public void testSendRawMessage_maxPayload() {
        // 准备参数：正好 4096 字节
        byte[] payload = new byte[4096];
        payload[4095] = 0x55;
        // mock 方法
        mockDevice();

        // 调用
        deviceMessageService.sendRawMessage(payload, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime());

        // 断言：原始帧完整保留
        IotDeviceMessage message = captureSentMessage();
        assertArrayEquals(payload, Base64.getDecoder().decode(message.getRaw()));
    }

    @Test
    public void testSendRawMessage_deviceNotExists() {
        // 调用，并断言异常：不投递
        assertServiceException(() -> deviceMessageService.sendRawMessage(PAYLOAD, PRODUCT_KEY, DEVICE_NAME, SERVER_ID,
                IotProtocolTypeEnum.TCP, System.nanoTime()), DEVICE_NOT_EXISTS, PRODUCT_KEY, DEVICE_NAME);
        verifyNoInteractions(deviceMessageProducer, scriptExecutor);
    }

    // ========== sendDeviceMessage ==========

    @Test
    public void testSendDeviceMessage_clearForgedRaw() {
        // 准备参数：设备经标准路径上报，试图伪造 raw、编号、时间、身份
        LocalDateTime forgedTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", Collections.singletonMap("t", 1))
                .setId("forged-id").setReportTime(forgedTime).setRaw(RAW)
                .setDeviceId(999L).setTenantId(999L).setServerId("forged-server");
        // mock 方法
        mockDevice();

        // 调用
        deviceMessageService.sendDeviceMessage(message, PRODUCT_KEY, DEVICE_NAME, SERVER_ID);

        // 断言：raw 被清空；编号、时间、身份由服务端设置
        IotDeviceMessage sent = captureSentMessage();
        assertSame(message, sent);
        assertNull(sent.getRaw());
        assertNotEquals("forged-id", sent.getId());
        assertNotEquals(forgedTime, sent.getReportTime());
        assertEquals(sent.getId(), sent.getRequestId());
        assertDeviceIdentity(sent);
    }

    @Test
    public void testSendDeviceMessage_keepRequestId() {
        // 准备参数
        IotDeviceMessage message = IotDeviceMessage.requestOf("req-1", "thing.property.post",
                Collections.singletonMap("t", 1));
        // mock 方法
        mockDevice();

        // 调用
        deviceMessageService.sendDeviceMessage(message, PRODUCT_KEY, DEVICE_NAME, SERVER_ID);

        // 断言：设备的 requestId 保留
        IotDeviceMessage sent = captureSentMessage();
        assertEquals("req-1", sent.getRequestId());
        assertNull(sent.getRaw());
    }

    @Test
    public void testSendDeviceMessage_rawMethodReserved() {
        // 调用，并断言异常：标准路径不接受 thing.raw.post / thing.raw.push
        for (IotDeviceMessageMethodEnum method : Arrays.asList(IotDeviceMessageMethodEnum.RAW_POST,
                IotDeviceMessageMethodEnum.RAW_PUSH)) {
            IotDeviceMessage message = IotDeviceMessage.requestOf(method.getMethod(), null).setRaw(RAW);
            assertServiceException(() -> deviceMessageService.sendDeviceMessage(message, PRODUCT_KEY, DEVICE_NAME,
                    SERVER_ID), DEVICE_MESSAGE_RAW_METHOD_RESERVED);
        }
        // 断言：不查设备、不投递
        verifyNoInteractions(deviceService, deviceMessageProducer);
    }

    @Test
    public void testSendDeviceMessage_deviceNotExists() {
        // 准备参数
        IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", Collections.singletonMap("t", 1));

        // 调用，并断言异常
        assertServiceException(() -> deviceMessageService.sendDeviceMessage(message, PRODUCT_KEY, DEVICE_NAME, SERVER_ID),
                DEVICE_NOT_EXISTS, PRODUCT_KEY, DEVICE_NAME);
        verifyNoInteractions(deviceMessageProducer);
    }

    // ========== encodeDownstreamCommand ==========

    @Test
    public void testEncodeDownstreamCommand_deviceQueryFailed() {
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenThrow(new IllegalStateException("rpc 失败"));

        // 调用
        IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                IotProtocolTypeEnum.TCP, System.nanoTime());

        // 断言：按设备不存在处理，走标准路径，不丢弃指令
        assertTrue(result.isStandard());
        verifyNoInteractions(scriptExecutor, deviceMessageProducer);
    }

    @Test
    public void testEncodeDownstreamCommand_deviceNotExists() {
        // 调用
        IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                IotProtocolTypeEnum.TCP, System.nanoTime());

        // 断言：与未启用脚本一致，走标准路径
        assertTrue(result.isStandard());
        verifyNoInteractions(scriptExecutor, deviceMessageProducer);
    }

    @Test
    public void testEncodeDownstreamCommand_scriptDisabledOrNotConfigured() {
        for (Boolean scriptEnabled : Arrays.asList(false, null)) {
            // mock 方法
            device.setScriptEnabled(scriptEnabled);
            when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);

            // 调用
            IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                    IotProtocolTypeEnum.TCP, System.nanoTime());

            // 断言：走标准路径，不执行脚本
            assertTrue(result.isStandard(), String.valueOf(scriptEnabled));
        }
        verifyNoInteractions(scriptExecutor, deviceMessageProducer);
    }

    @Test
    public void testEncodeDownstreamCommand_scriptContentEmpty() {
        // mock 方法：已启用，但脚本内容为空（配置不合法）
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        for (String content : Arrays.asList(null, "", "  ")) {
            device.setScriptContent(content);

            // 调用，并断言：不下发（不写设备，也不改发 JSON）
            assertNull(deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                    IotProtocolTypeEnum.TCP, System.nanoTime()), String.valueOf(content));
        }
        verifyNoInteractions(scriptExecutor, deviceMessageProducer);
    }

    @Test
    public void testEncodeDownstreamCommand_encodeBytes() {
        // 准备参数：截止 = 收到的时刻 + gateway.script.deadline
        IotDeviceMessage message = buildPropertySet();
        long receivedNanos = System.nanoTime();
        long deadlineNanos = receivedNanos + gatewayProperties.getScript().getDeadline().toNanos();
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        when(scriptExecutor.execute(sameScript(), eq("encode"), anyString(), eq(deadlineNanos)))
                .thenReturn(IotScriptResult.success("{\"bytes\":[1,2,255]}", 1));

        // 调用
        IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(message,
                IotProtocolTypeEnum.TCP, receivedNanos);

        // 断言：透传 encode 的字节，带上固定的脚本
        assertFalse(result.isStandard());
        assertArrayEquals(new byte[]{1, 2, (byte) 0xFF}, result.getBytes());
        assertEquals(source.getKey(), result.getSource().getKey());
        verifyNoInteractions(deviceMessageProducer);
        // 断言：encode 的入参
        ArgumentCaptor<String> ctxCaptor = ArgumentCaptor.forClass(String.class);
        verify(scriptExecutor).execute(sameScript(), eq("encode"), ctxCaptor.capture(), eq(deadlineNanos));
        JsonNode ctx = JsonUtils.parseTree(ctxCaptor.getValue());
        assertEquals("encode", ctx.get("direction").asText());
        assertEquals(IotProtocolTypeEnum.TCP.getType(), ctx.get("protocol").asText());
        assertEquals("thing.property.set", ctx.get("message").get("method").asText());
        assertEquals("req-1", ctx.get("message").get("requestId").asText());
        assertEquals(1, ctx.get("message").get("params").get("power").asInt());
    }

    @Test
    public void testEncodeDownstreamCommand_encodeStandard() {
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        when(scriptExecutor.execute(sameScript(), eq("encode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.success("{\"standard\":true}", 1));

        // 调用
        IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                IotProtocolTypeEnum.TCP, System.nanoTime());

        // 断言：走标准路径
        assertTrue(result.isStandard());
        assertNull(result.getBytes());
    }

    @Test
    public void testEncodeDownstreamCommand_encodeNotWrite() {
        // 准备参数：encode 返回 null、执行失败、超时、繁忙、输出不合法（未知键、bytes 为空、超过 255、同时有两种输出）
        List<IotScriptResult> results = Arrays.asList(IotScriptResult.success(null, 1),
                IotScriptResult.error("boom", 1), IotScriptResult.timeout("slow"), IotScriptResult.rejected("full"),
                IotScriptResult.success("{\"other\":1}", 1), IotScriptResult.success("{\"bytes\":[]}", 1),
                IotScriptResult.success("{\"bytes\":[256]}", 1),
                IotScriptResult.success("{\"bytes\":[1],\"standard\":true}", 1));
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        for (IotScriptResult result : results) {
            when(scriptExecutor.execute(sameScript(), eq("encode"), anyString(), anyLong())).thenReturn(result);

            // 调用，并断言：不下发（不写设备，也不改走标准路径）
            assertNull(deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                    IotProtocolTypeEnum.TCP, System.nanoTime()), String.valueOf(result.getJson()));
        }
        // 断言：不给 biz 构造失败回复
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testEncodeDownstreamCommand_modbusPdu() {
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        when(scriptExecutor.execute(sameScript(), eq("encode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.success("{\"bytes\":[1,2]}", 1),
                        IotScriptResult.success("{\"bytes\":[6,0,16,0,1]}", 1));

        // 调用，并断言：Modbus 协议，encode 输出不是合法的请求 PDU → 不下发
        assertNull(deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                IotProtocolTypeEnum.MODBUS_TCP_CLIENT, System.nanoTime()));
        // 调用，并断言：合法的请求 PDU → 透传
        IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                IotProtocolTypeEnum.MODBUS_TCP_CLIENT, System.nanoTime());
        assertArrayEquals(new byte[]{6, 0, 16, 0, 1}, result.getBytes());
    }

    @Test
    public void testEncodeDownstreamCommand_encodeFinishedPastDeadline() {
        // 准备参数：截止 500ms，encode 执行到截止之后才返回
        gatewayProperties.getScript().setDeadline(Duration.ofMillis(500));
        long receivedNanos = System.nanoTime();
        long deadlineNanos = receivedNanos + TimeUnit.MILLISECONDS.toNanos(500);
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        when(scriptExecutor.execute(sameScript(), eq("encode"), anyString(), eq(deadlineNanos))).thenAnswer(invocation -> {
            sleepUntil(deadlineNanos);
            return IotScriptResult.success("{\"bytes\":[1,2]}", 300);
        });

        // 调用
        IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                IotProtocolTypeEnum.TCP, receivedNanos);

        // 断言：迟到的结果不下发（不写设备，也不改走标准路径）
        assertNull(result);
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testEncodeDownstreamCommand_queuedPastDeadline() {
        // 准备参数：收到指令的时刻已早于截止时长之前（在设备的指令队列里排队期间到期）
        long receivedNanos = System.nanoTime() - gatewayProperties.getScript().getDeadline().toNanos() - 1;
        long deadlineNanos = receivedNanos + gatewayProperties.getScript().getDeadline().toNanos();
        // mock 方法：脚本执行器拿到已过的截止时间（真实的执行器不执行脚本，直接返回超时）；即使这里返回了字节，也不下发
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        when(scriptExecutor.execute(sameScript(), eq("encode"), anyString(), eq(deadlineNanos)))
                .thenReturn(IotScriptResult.success("{\"bytes\":[1,2]}", 1));

        // 调用
        IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(buildPropertySet(),
                IotProtocolTypeEnum.TCP, receivedNanos);

        // 断言：不下发（不写设备，也不改走标准路径）
        assertNull(result);
        verifyNoInteractions(deviceMessageProducer);
    }

    // ========== sendRawReply ==========

    @Test
    public void testSendRawReply_rawPushResponse() {
        // 准备参数：raw.push 的设备正常响应
        IotDeviceMessage request = buildRawPush();
        byte[] responsePdu = new byte[]{0x03, 0x04, 0x00, 0x01, 0x00, 0x02};
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);

        // 调用
        deviceMessageService.sendRawReply(request, null, responsePdu, null, SERVER_ID);

        // 断言：回复 code = 0，raw = 响应 PDU；不执行脚本
        IotDeviceMessage reply = captureSentMessage();
        assertEquals("req-raw", reply.getRequestId());
        assertNotEquals("req-raw", reply.getId());
        assertEquals("thing.raw.push", reply.getMethod());
        assertEquals(SUCCESS.getCode(), reply.getCode());
        assertEquals(SUCCESS.getMsg(), reply.getMsg());
        assertNull(reply.getParams());
        assertNull(reply.getData());
        assertEquals(Base64.getEncoder().encodeToString(responsePdu), reply.getRaw());
        assertDeviceIdentity(reply);
        verifyNoInteractions(scriptExecutor);
    }

    @Test
    public void testSendRawReply_rawPushDeviceError() {
        // 准备参数：raw.push 的设备异常响应（83 02），结果码由协议判定
        IotDeviceMessage request = buildRawPush();
        byte[] responsePdu = new byte[]{(byte) 0x83, 0x02};
        ErrorCode deviceError = new ErrorCode(MODBUS_EXCEPTION.getCode(), StrUtil.format(MODBUS_EXCEPTION.getMsg(), 2));
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);

        // 调用
        deviceMessageService.sendRawReply(request, null, responsePdu, deviceError, SERVER_ID);

        // 断言：回复协议判定的结果码与文案；raw = 响应 PDU
        IotDeviceMessage reply = captureSentMessage();
        assertEquals("req-raw", reply.getRequestId());
        assertEquals(deviceError.getCode(), reply.getCode());
        assertEquals(deviceError.getMsg(), reply.getMsg());
        assertEquals(Base64.getEncoder().encodeToString(responsePdu), reply.getRaw());
        verifyNoInteractions(scriptExecutor);
    }

    @Test
    public void testSendRawReply_encodeResponse() {
        // 准备参数：encode 指令的设备响应；设备缓存里已是新版本脚本，必须用 encode 时固定的脚本
        gatewayProperties.getScript().setEnabled(true);
        IotDeviceMessage request = buildPropertySet();
        byte[] responsePdu = new byte[]{0x06, 0x00, 0x10, 0x00, 0x01};
        device.setScriptContent("function decode(ctx) { return null; }");
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        when(scriptExecutor.execute(same(source), eq("decode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.success("{\"method\":\"thing.property.set\",\"requestId\":\"req-1\","
                        + "\"code\":0,\"data\":{\"power\":1}}", 1));

        // 调用
        long beginNanos = System.nanoTime();
        deviceMessageService.sendRawReply(request, source, responsePdu, null, SERVER_ID);

        // 断言：用固定的脚本 decode，入参带原请求、截止时间 = 现在 + deadline
        ArgumentCaptor<String> ctxCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> deadlineCaptor = ArgumentCaptor.forClass(Long.class);
        verify(scriptExecutor).execute(same(source), eq("decode"), ctxCaptor.capture(), deadlineCaptor.capture());
        verify(scriptExecutor, never()).execute(argThat(script -> script != source), any(), any(), anyLong());
        JsonNode ctx = JsonUtils.parseTree(ctxCaptor.getValue());
        assertEquals(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType(), ctx.get("protocol").asText());
        assertEquals("0600100001", ctx.get("hex").asText());
        assertEquals("thing.property.set", ctx.get("request").get("method").asText());
        assertEquals("req-1", ctx.get("request").get("requestId").asText());
        assertTrue(deadlineCaptor.getValue() - beginNanos
                >= gatewayProperties.getScript().getDeadline().toNanos());
        // 断言：回复带上 raw = 响应 PDU
        IotDeviceMessage reply = captureSentMessage();
        assertEquals("req-1", reply.getRequestId());
        assertEquals("thing.property.set", reply.getMethod());
        assertEquals(SUCCESS.getCode(), reply.getCode());
        assertEquals(Collections.singletonMap("power", 1), reply.getData());
        assertEquals(Base64.getEncoder().encodeToString(responsePdu), reply.getRaw());
        assertDeviceIdentity(reply);
    }

    @Test
    public void testSendRawReply_scriptDisabledBeforeResponse() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        for (Boolean scriptEnabled : Arrays.asList(false, null)) {
            // mock 方法：等待响应期间，设备缓存里已看到脚本停用（或未配置）
            device.setScriptEnabled(scriptEnabled);
            when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);

            // 调用
            deviceMessageService.sendRawReply(buildPropertySet(), source, new byte[]{0x06, 0x00, 0x10, 0x00, 0x01},
                    null, SERVER_ID);
        }

        // 断言：不解析响应，不回复
        verifyNoInteractions(scriptExecutor, deviceMessageProducer);
    }

    @Test
    public void testSendRawReply_switchOffBeforeResponse() {
        // 准备参数：等待响应期间功能开关被关闭；产品脚本仍是启用状态
        gatewayProperties.getScript().setEnabled(false);
        assertTrue(device.getScriptEnabled());
        byte[] responsePdu = new byte[]{0x06, 0x00, 0x10, 0x00, 0x01};
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);

        // 调用：encode 指令的设备响应（固定的脚本）
        deviceMessageService.sendRawReply(buildPropertySet(), source, responsePdu, null, SERVER_ID);
        // 断言：不执行脚本，不回复
        verifyNoInteractions(scriptExecutor, deviceMessageProducer);

        // 调用：raw.push 的设备响应（没有脚本）
        deviceMessageService.sendRawReply(buildRawPush(), null, responsePdu, null, SERVER_ID);
        // 断言：照常直接回复设备的实际响应
        IotDeviceMessage reply = captureSentMessage();
        assertEquals("req-raw", reply.getRequestId());
        assertEquals(Base64.getEncoder().encodeToString(responsePdu), reply.getRaw());
        verifyNoInteractions(scriptExecutor);
    }

    @Test
    public void testSendRawReply_decodeRequestIdMismatch() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        IotDeviceMessage request = buildPropertySet();
        // mock 方法：decode 返回的 requestId 与原请求不一致
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        when(scriptExecutor.execute(same(source), eq("decode"), anyString(), anyLong()))
                .thenReturn(IotScriptResult.success("{\"method\":\"thing.property.set\",\"requestId\":\"other\","
                        + "\"code\":0}", 1));

        // 调用
        deviceMessageService.sendRawReply(request, source, new byte[]{0x06, 0x00, 0x10, 0x00, 0x01}, null, SERVER_ID);

        // 断言：不回复
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawReply_decodeFailure() {
        // 准备参数：decode 执行失败、返回 null（指令的响应必须有回执）、返回请求而不是回执
        gatewayProperties.getScript().setEnabled(true);
        List<IotScriptResult> results = Arrays.asList(IotScriptResult.error("boom", 1), IotScriptResult.success(null, 1),
                IotScriptResult.success("{\"method\":\"thing.property.post\",\"params\":{\"t\":1}}", 1));
        // mock 方法
        when(deviceService.getDeviceFromCache(DEVICE_ID)).thenReturn(device);
        for (IotScriptResult result : results) {
            when(scriptExecutor.execute(same(source), eq("decode"), anyString(), anyLong())).thenReturn(result);

            // 调用
            deviceMessageService.sendRawReply(buildPropertySet(), source, new byte[]{0x06, 0x00, 0x10, 0x00, 0x01},
                    null, SERVER_ID);
        }

        // 断言：只记日志，不回复
        verifyNoInteractions(deviceMessageProducer);
    }

    @Test
    public void testSendRawReply_deviceNotExists() {
        // 调用
        deviceMessageService.sendRawReply(buildRawPush(), null, new byte[]{0x03, 0x02, 0x00, 0x01}, null, SERVER_ID);

        // 断言：不回复
        verifyNoInteractions(deviceMessageProducer, scriptExecutor);
    }

    // ========== 辅助方法 ==========

    private void mockDevice() {
        when(deviceService.getDeviceFromCache(PRODUCT_KEY, DEVICE_NAME)).thenReturn(device);
    }

    /**
     * 匹配按设备信息构造的脚本：服务每次构造新的 {@link IotScriptSource}，按 key（productId:hash）匹配
     */
    private IotScriptSource sameScript() {
        return argThat(script -> script != null && source.getKey().equals(script.getKey()));
    }

    private static IotDeviceMessage buildPropertySet() {
        return IotDeviceMessage.requestOf("req-1", IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(),
                Collections.singletonMap("power", 1)).setDeviceId(DEVICE_ID);
    }

    private static IotDeviceMessage buildRawPush() {
        return IotDeviceMessage.requestOf("req-raw", IotDeviceMessageMethodEnum.RAW_PUSH.getMethod(), null)
                .setDeviceId(DEVICE_ID).setRaw(Base64.getEncoder().encodeToString(new byte[]{0x03, 0x00, 0x10, 0x00, 0x02}));
    }

    /**
     * 断言 producer 恰好发送了 1 条消息，并返回它
     */
    private IotDeviceMessage captureSentMessage() {
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageProducer, times(1)).sendDeviceMessage(captor.capture());
        return captor.getValue();
    }

    private static void assertDeviceIdentity(IotDeviceMessage message) {
        assertEquals(DEVICE_ID, message.getDeviceId());
        assertEquals(TENANT_ID, message.getTenantId());
        assertEquals(SERVER_ID, message.getServerId());
    }

    /**
     * 断言是一条 thing.raw.post：params、data 为空，raw 为原始帧，requestId = id
     */
    private static void assertRawPost(IotDeviceMessage message) {
        assertEquals(IotDeviceMessageMethodEnum.RAW_POST.getMethod(), message.getMethod());
        assertNull(message.getParams());
        assertNull(message.getCode());
        assertEquals(RAW, message.getRaw());
        assertNotNull(message.getId());
        assertEquals(message.getId(), message.getRequestId());
        assertNotNull(message.getReportTime());
        assertDeviceIdentity(message);
        assertNull(message.getData());
    }

    private static void sleepUntil(long deadlineNanos) throws InterruptedException {
        long remaining;
        while ((remaining = deadlineNanos - System.nanoTime()) >= 0) {
            TimeUnit.NANOSECONDS.sleep(remaining + TimeUnit.MILLISECONDS.toNanos(1));
        }
    }

}
