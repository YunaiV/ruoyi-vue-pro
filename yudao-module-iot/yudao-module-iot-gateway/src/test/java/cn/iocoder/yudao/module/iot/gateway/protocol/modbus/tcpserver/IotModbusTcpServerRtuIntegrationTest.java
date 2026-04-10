package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver;

import cn.hutool.core.util.HexUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameDecoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * IoT Modbus TCP Server 协议集成测试 — MODBUS_RTU 帧格式（手动测试）
 *
 * <p>测试场景：设备（TCP Client）连接到网关（TCP Server），使用 MODBUS_RTU（CRC16）帧格式通信
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（需开启 modbus-tcp-server 协议，默认端口 503）</li>
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
public class IotModbusTcpServerRtuIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 503;
    private static final int TIMEOUT_MS = 5000;

    private static final int CUSTOM_FC = 65;
    private static final int SLAVE_ID = 1;

    private static Vertx vertx;
    private static NetClient netClient;

    // ===================== 编解码器 =====================

    private static final IotModbusFrameDecoder FRAME_DECODER = new IotModbusFrameDecoder(CUSTOM_FC);
    private static final IotModbusFrameEncoder FRAME_ENCODER = new IotModbusFrameEncoder(CUSTOM_FC);

    // ===================== 设备信息（根据实际情况修改，从 iot_device 表查询） =====================

    private static final String PRODUCT_KEY = "modbus_tcp_server_product_demo";
    private static final String DEVICE_NAME = "modbus_tcp_server_device_demo_rtu";
    private static final String DEVICE_SECRET = "af01c55eb8e3424bb23fc6c783936b2e";

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
            assertEquals(0, json.getInt("code"));
            log.info("[testAuth][认证结果: code={}, message={}]", json.getInt("code"), json.getStr("message"));
        } finally {
            socket.close();
        }
    }

    // ===================== 轮询响应测试 =====================

    /**
     * 轮询响应测试：认证后持续监听网关下发的读请求（RTU 格式），每次收到都自动构造读响应帧发回
     */
    @Test
    public void testPollingResponse() throws Exception {
        NetSocket socket = connect().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try {
            // 1. 先认证
            IotModbusFrame authResponse = authenticate(socket);
            log.info("[testPollingResponse][认证响应: {}]", authResponse.getCustomData());
            JSONObject authJson = JSONUtil.parseObj(authResponse.getCustomData());
            assertEquals(0, authJson.getInt("code"));

            // 2. 设置持续监听：每收到一个读请求，自动回复
            log.info("[testPollingResponse][开始持续监听网关下发的读请求...]");
            CompletableFuture<Void> done = new CompletableFuture<>();
            // 注意：使用 requestMode=true，因为设备端收到的是网关下发的读请求（非响应）
            RecordParser parser = FRAME_DECODER.createRecordParser((frame, frameFormat) -> {
                log.info("[testPollingResponse][收到请求: slaveId={}, FC={}]",
                        frame.getSlaveId(), frame.getFunctionCode());
                // 解析读请求中的起始地址和数量
                byte[] pdu = frame.getPdu();
                int startAddress = ((pdu[0] & 0xFF) << 8) | (pdu[1] & 0xFF);
                int quantity = ((pdu[2] & 0xFF) << 8) | (pdu[3] & 0xFF);
                log.info("[testPollingResponse][读请求参数: startAddress={}, quantity={}]", startAddress, quantity);

                // 构造读响应帧（模拟寄存器数据，RTU 格式）
                int[] registerValues = new int[quantity];
                for (int i = 0; i < quantity; i++) {
                    registerValues[i] = 100 + i * 100; // 模拟值: 100, 200, 300, ...
                }
                byte[] responseData = buildReadResponse(frame.getSlaveId(),
                        frame.getFunctionCode(), registerValues);
                socket.write(Buffer.buffer(responseData));
                log.info("[testPollingResponse][已发送读响应, registerValues={}]", registerValues);
            }, true);
            socket.handler(parser);

            // 3. 持续等待（200 秒），期间会自动回复所有收到的读请求
            Thread.sleep(200000);
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
                    HexUtil.encodeHexStr(writeRequest.getPdu()));
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
        authInfo.setClientId("");
        byte[] authFrame = buildAuthFrame(authInfo.getClientId(), authInfo.getUsername(), authInfo.getPassword());
        return sendAndReceive(socket, authFrame);
    }

    /**
     * 发送帧并等待响应（使用 IotModbusFrameDecoder 自动检测帧格式并解码）
     */
    private IotModbusFrame sendAndReceive(NetSocket socket, byte[] frameData) throws Exception {
        CompletableFuture<IotModbusFrame> responseFuture = new CompletableFuture<>();
        // 使用 FrameDecoder 创建拆包器（自动检测帧格式 + 解码，直接回调 IotModbusFrame）
        RecordParser parser = FRAME_DECODER.createRecordParser(
                (frame, frameFormat) -> {
                    try {
                        log.info("[sendAndReceive][检测到帧格式: {}]", frameFormat);
                        responseFuture.complete(frame);
                    } catch (Exception e) {
                        responseFuture.completeExceptionally(e);
                    }
                });
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
        // 使用 FrameDecoder 创建拆包器（直接回调 IotModbusFrame）
        RecordParser parser = FRAME_DECODER.createRecordParser(
                (frame, frameFormat) -> {
                    try {
                        log.info("[waitForRequest][检测到帧格式: {}]", frameFormat);
                        requestFuture.complete(frame);
                    } catch (Exception e) {
                        requestFuture.completeExceptionally(e);
                    }
                });
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
        return FRAME_ENCODER.encodeCustomFrame(SLAVE_ID, json.toString(),
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
        int crc = IotModbusCommonUtils.calculateCrc16(frame, totalLength - 2);
        frame[totalLength - 2] = (byte) (crc & 0xFF);        // CRC Low
        frame[totalLength - 1] = (byte) ((crc >> 8) & 0xFF); // CRC High
        return frame;
    }

}
