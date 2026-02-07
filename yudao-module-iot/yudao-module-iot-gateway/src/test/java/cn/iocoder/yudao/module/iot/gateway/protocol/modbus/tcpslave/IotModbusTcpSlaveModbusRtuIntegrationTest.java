package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusRecordParserFactory;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT Modbus TCP Slave 协议集成测试 — MODBUS_RTU 帧格式（手动测试）
 *
 * <p>测试场景：设备（TCP Client）连接到网关（TCP Server），使用 MODBUS_RTU（CRC16）帧格式通信
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（需开启 modbus-tcp-slave 协议，默认端口 503）</li>
 *     <li>确保数据库有对应的 Modbus 设备配置（mode=1, frameFormat=modbus_rtu）</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testAuth()} - 自定义功能码认证</li>
 *             <li>{@link #testPollingResponse()} - 轮询响应</li>
 *             <li>{@link #testPropertySetWrite()} - 属性设置（接收写指令）</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
public class IotModbusTcpSlaveModbusRtuIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 503;
    private static final int TIMEOUT_MS = 5000;

    private static final int CUSTOM_FC = 65;
    private static final int SLAVE_ID = 1;

    private static Vertx vertx;
    private static NetClient netClient;

    // ===================== 编解码器 =====================

    private static final IotModbusFrameCodec FRAME_CODEC = new IotModbusFrameCodec(CUSTOM_FC);

    // ===================== 设备信息（根据实际情况修改，从 iot_device 表查询） =====================

    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

    @BeforeAll
    static void setUp() {
        vertx = Vertx.vertx();
        NetClientOptions options = new NetClientOptions()
                .setConnectTimeout(TIMEOUT_MS)
                .setIdleTimeout(TIMEOUT_MS);
        netClient = vertx.createNetClient(options);
    }

    @AfterAll
    static void tearDown() {
        if (netClient != null) {
            netClient.close();
        }
        if (vertx != null) {
            vertx.close();
        }
    }

    // ===================== 认证测试 =====================

    /**
     * 认证测试：发送自定义功能码 FC65 认证帧（RTU 格式），验证认证成功响应
     */
    @Test
    public void testAuth() throws Exception {
        NetSocket socket = connect().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try {
            // 1. 构造并发送认证帧
            IotModbusFrame response = authenticate(socket);

            // 2. 验证响应
            log.info("[testAuth][认证响应帧: slaveId={}, FC={}, customData={}]",
                    response.getSlaveId(), response.getFunctionCode(), response.getCustomData());
            JSONObject json = JSONUtil.parseObj(response.getCustomData());
            log.info("[testAuth][认证结果: code={}, message={}]", json.getInt("code"), json.getStr("message"));
        } finally {
            socket.close();
        }
    }

    // ===================== 轮询响应测试 =====================

    /**
     * 轮询响应测试：认证后等待网关下发 FC03 读请求（RTU 格式），构造读响应帧发回
     */
    @Test
    public void testPollingResponse() throws Exception {
        NetSocket socket = connect().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try {
            // 1. 先认证
            IotModbusFrame authResponse = authenticate(socket);
            log.info("[testPollingResponse][认证响应: {}]", authResponse.getCustomData());

            // 2. 等待网关下发读请求
            log.info("[testPollingResponse][等待网关下发读请求...]");
            IotModbusFrame readRequest = waitForRequest(socket);
            log.info("[testPollingResponse][收到读请求: slaveId={}, FC={}]",
                    readRequest.getSlaveId(), readRequest.getFunctionCode());

            // 3. 解析读请求中的起始地址和数量
            byte[] pdu = readRequest.getPdu();
            int startAddress = ((pdu[0] & 0xFF) << 8) | (pdu[1] & 0xFF);
            int quantity = ((pdu[2] & 0xFF) << 8) | (pdu[3] & 0xFF);
            log.info("[testPollingResponse][读请求参数: startAddress={}, quantity={}]", startAddress, quantity);

            // 4. 构造读响应帧（模拟寄存器数据，RTU 格式）
            int[] registerValues = new int[quantity];
            for (int i = 0; i < quantity; i++) {
                registerValues[i] = 100 + i * 100; // 模拟值: 100, 200, 300, ...
            }
            byte[] responseData = buildReadResponse(readRequest.getSlaveId(),
                    readRequest.getFunctionCode(), registerValues);
            socket.write(Buffer.buffer(responseData));
            log.info("[testPollingResponse][已发送读响应, registerValues={}]", registerValues);

            // 5. 等待一段时间让网关处理
            Thread.sleep(20000);
        } finally {
            socket.close();
        }
    }

    // ===================== 属性设置测试 =====================

    /**
     * 属性设置测试：认证后等待接收网关下发的 FC06/FC16 写请求（RTU 格式）
     * <p>
     * 注意：需手动在平台触发 property.set
     */
    @Test
    public void testPropertySetWrite() throws Exception {
        NetSocket socket = connect().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try {
            // 1. 先认证
            IotModbusFrame authResponse = authenticate(socket);
            log.info("[testPropertySetWrite][认证响应: {}]", authResponse.getCustomData());

            // 2. 等待网关下发写请求（需手动在平台触发 property.set）
            log.info("[testPropertySetWrite][等待网关下发写请求（请在平台触发 property.set）...]");
            IotModbusFrame writeRequest = waitForRequest(socket);
            log.info("[testPropertySetWrite][收到写请求: slaveId={}, FC={}, pdu={}]",
                    writeRequest.getSlaveId(), writeRequest.getFunctionCode(),
                    bytesToHex(writeRequest.getPdu()));
        } finally {
            socket.close();
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 建立 TCP 连接
     */
    private CompletableFuture<NetSocket> connect() {
        CompletableFuture<NetSocket> future = new CompletableFuture<>();
        netClient.connect(SERVER_PORT, SERVER_HOST)
                .onSuccess(future::complete)
                .onFailure(future::completeExceptionally);
        return future;
    }

    /**
     * 执行认证并返回响应帧
     */
    private IotModbusFrame authenticate(NetSocket socket) throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        byte[] authFrame = buildAuthFrame(authInfo.getClientId(), authInfo.getUsername(), authInfo.getPassword());
        return sendAndReceive(socket, authFrame);
    }

    /**
     * 发送帧并等待响应（使用 IotModbusRecordParserFactory 自动检测帧格式）
     */
    private IotModbusFrame sendAndReceive(NetSocket socket, byte[] frameData) throws Exception {
        CompletableFuture<IotModbusFrame> responseFuture = new CompletableFuture<>();
        // 使用 RecordParserFactory 创建拆包器（自动检测帧格式）
        RecordParser parser = IotModbusRecordParserFactory.create(CUSTOM_FC,
                buffer -> {
                    try {
                        // 检测到的帧格式应该是 RTU，使用 RTU 格式解码
                        IotModbusFrame frame = FRAME_CODEC.decodeResponse(
                                buffer.getBytes(), IotModbusFrameFormatEnum.MODBUS_RTU);
                        responseFuture.complete(frame);
                    } catch (Exception e) {
                        responseFuture.completeExceptionally(e);
                    }
                },
                format -> log.info("[sendAndReceive][检测到帧格式: {}]", format));
        socket.handler(parser);

        // 发送请求
        log.info("[sendAndReceive][发送帧, 长度={}]", frameData.length);
        socket.write(Buffer.buffer(frameData));

        // 等待响应
        return responseFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 等待接收网关下发的请求帧（不发送，只等待接收）
     */
    private IotModbusFrame waitForRequest(NetSocket socket) throws Exception {
        CompletableFuture<IotModbusFrame> requestFuture = new CompletableFuture<>();
        // 使用 RecordParserFactory 创建拆包器
        RecordParser parser = IotModbusRecordParserFactory.create(CUSTOM_FC,
                buffer -> {
                    try {
                        IotModbusFrame frame = FRAME_CODEC.decodeResponse(
                                buffer.getBytes(), IotModbusFrameFormatEnum.MODBUS_RTU);
                        requestFuture.complete(frame);
                    } catch (Exception e) {
                        requestFuture.completeExceptionally(e);
                    }
                },
                format -> log.info("[waitForRequest][检测到帧格式: {}]", format));
        socket.handler(parser);

        // 等待（超时 30 秒，因为轮询间隔可能比较长）
        return requestFuture.get(30000, TimeUnit.MILLISECONDS);
    }

    /**
     * 构造认证帧（MODBUS_RTU 格式）
     * <p>
     * JSON: {"method":"auth","params":{"clientId":"...","username":"...","password":"..."}}
     * <p>
     * RTU 帧格式：[SlaveId(1)] [FC=0x41(1)] [ByteCount(1)] [JSON(N)] [CRC16(2)]
     */
    private byte[] buildAuthFrame(String clientId, String username, String password) {
        JSONObject params = new JSONObject();
        params.set("clientId", clientId);
        params.set("username", username);
        params.set("password", password);
        JSONObject json = new JSONObject();
        json.set("method", "auth");
        json.set("params", params);
        return FRAME_CODEC.encodeCustomFrame(SLAVE_ID, json.toString(),
                IotModbusFrameFormatEnum.MODBUS_RTU, 0);
    }

    /**
     * 构造 FC03/FC01-04 读响应帧（MODBUS_RTU 格式）
     * <p>
     * RTU 帧格式：[SlaveId(1)] [FC(1)] [ByteCount(1)] [RegisterData(N*2)] [CRC16(2)]
     */
    private byte[] buildReadResponse(int slaveId, int functionCode, int[] registerValues) {
        int byteCount = registerValues.length * 2;
        // 帧长度：SlaveId(1) + FC(1) + ByteCount(1) + Data(N*2) + CRC(2)
        int totalLength = 1 + 1 + 1 + byteCount + 2;
        byte[] frame = new byte[totalLength];
        frame[0] = (byte) slaveId;
        frame[1] = (byte) functionCode;
        frame[2] = (byte) byteCount;
        for (int i = 0; i < registerValues.length; i++) {
            frame[3 + i * 2] = (byte) ((registerValues[i] >> 8) & 0xFF);
            frame[3 + i * 2 + 1] = (byte) (registerValues[i] & 0xFF);
        }
        // 计算 CRC16
        int crc = IotModbusFrameCodec.calculateCrc16(frame, totalLength - 2);
        frame[totalLength - 2] = (byte) (crc & 0xFF);        // CRC Low
        frame[totalLength - 1] = (byte) ((crc >> 8) & 0xFF); // CRC High
        return frame;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

}
