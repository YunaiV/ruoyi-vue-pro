package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrameCodec;
import io.vertx.core.Handler;
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT Modbus TCP Slave 协议集成测试 — MODBUS_TCP 帧格式（手动测试）
 *
 * <p>测试场景：设备（TCP Client）连接到网关（TCP Server），使用 MODBUS_TCP（MBAP 头）帧格式通信
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（需开启 modbus-tcp-slave 协议，默认端口 503）</li>
 *     <li>确保数据库有对应的 Modbus 设备配置（mode=1, frameFormat=modbus_tcp）</li>
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
public class IotModbusTcpSlaveModbusTcpIntegrationTest {

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
     * 认证测试：发送自定义功能码 FC65 认证帧，验证认证成功响应
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
     * 轮询响应测试：认证后等待网关下发 FC03 读请求，构造读响应帧发回
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
            log.info("[testPollingResponse][收到读请求: slaveId={}, FC={}, transactionId={}]",
                    readRequest.getSlaveId(), readRequest.getFunctionCode(), readRequest.getTransactionId());

            // 3. 解析读请求中的起始地址和数量
            byte[] pdu = readRequest.getPdu();
            int startAddress = ((pdu[0] & 0xFF) << 8) | (pdu[1] & 0xFF);
            int quantity = ((pdu[2] & 0xFF) << 8) | (pdu[3] & 0xFF);
            log.info("[testPollingResponse][读请求参数: startAddress={}, quantity={}]", startAddress, quantity);

            // 4. 构造读响应帧（模拟寄存器数据）
            int[] registerValues = new int[quantity];
            for (int i = 0; i < quantity; i++) {
                registerValues[i] = 100 + i * 100; // 模拟值: 100, 200, 300, ...
            }
            byte[] responseData = buildReadResponse(readRequest.getTransactionId(),
                    readRequest.getSlaveId(), readRequest.getFunctionCode(), registerValues);
            socket.write(Buffer.buffer(responseData));
            log.info("[testPollingResponse][已发送读响应, registerValues={}]", registerValues);

            // 5. 等待一段时间让网关处理
            Thread.sleep(200000);
        } finally {
            socket.close();
        }
    }

    // ===================== 属性设置测试 =====================

    /**
     * 属性设置测试：认证后等待接收网关下发的 FC06/FC16 写请求
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
            log.info("[testPropertySetWrite][收到写请求: slaveId={}, FC={}, transactionId={}, pdu={}]",
                    writeRequest.getSlaveId(), writeRequest.getFunctionCode(),
                    writeRequest.getTransactionId(), bytesToHex(writeRequest.getPdu()));
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
     * 发送帧并等待响应（MODBUS_TCP 格式）
     * <p>
     * 使用两阶段 RecordParser 拆包：fixedSizeMode(6) 读 MBAP 头 → fixedSizeMode(length) 读 body
     */
    private IotModbusFrame sendAndReceive(NetSocket socket, byte[] frameData) throws Exception {
        CompletableFuture<IotModbusFrame> responseFuture = new CompletableFuture<>();
        // 创建 TCP 两阶段拆包 RecordParser
        RecordParser parser = RecordParser.newFixed(6);
        parser.handler(new TcpRecordParserHandler(parser, responseFuture));
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
        RecordParser parser = RecordParser.newFixed(6);
        parser.handler(new TcpRecordParserHandler(parser, requestFuture));
        socket.handler(parser);

        // 等待（超时 30 秒，因为轮询间隔可能比较长）
        return requestFuture.get(30000, TimeUnit.MILLISECONDS);
    }

    /**
     * MODBUS_TCP 两阶段拆包 Handler
     */
    private class TcpRecordParserHandler implements Handler<Buffer> {

        private final RecordParser parser;
        private final CompletableFuture<IotModbusFrame> future;
        private byte[] mbapHeader;
        private boolean waitingForBody = false;

        TcpRecordParserHandler(RecordParser parser, CompletableFuture<IotModbusFrame> future) {
            this.parser = parser;
            this.future = future;
        }

        @Override
        public void handle(Buffer buffer) {
            try {
                if (waitingForBody) {
                    // Phase 2: 收到 body（unitId + PDU）
                    byte[] body = buffer.getBytes();
                    byte[] fullFrame = new byte[mbapHeader.length + body.length];
                    System.arraycopy(mbapHeader, 0, fullFrame, 0, mbapHeader.length);
                    System.arraycopy(body, 0, fullFrame, mbapHeader.length, body.length);

                    IotModbusFrame frame = FRAME_CODEC.decodeResponse(fullFrame, IotModbusFrameFormatEnum.MODBUS_TCP);
                    future.complete(frame);
                } else {
                    // Phase 1: 收到 MBAP 头 6 字节
                    this.mbapHeader = buffer.getBytes();
                    int length = ((mbapHeader[4] & 0xFF) << 8) | (mbapHeader[5] & 0xFF);
                    this.waitingForBody = true;
                    parser.fixedSizeMode(length);
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }
    }

    /**
     * 构造认证帧（MODBUS_TCP 格式）
     * <p>
     * JSON: {"method":"auth","params":{"clientId":"...","username":"...","password":"..."}}
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
                IotModbusFrameFormatEnum.MODBUS_TCP, 1);
    }

    /**
     * 构造 FC03/FC01-04 读响应帧（MODBUS_TCP 格式）
     * <p>
     * 格式：[MBAP(6)] [UnitId(1)] [FC(1)] [ByteCount(1)] [RegisterData(N*2)]
     */
    private byte[] buildReadResponse(int transactionId, int slaveId, int functionCode, int[] registerValues) {
        int byteCount = registerValues.length * 2;
        // PDU: FC(1) + ByteCount(1) + Data(N*2)
        int pduLength = 1 + 1 + byteCount;
        // 完整帧：MBAP(6) + UnitId(1) + PDU
        int totalLength = 6 + 1 + pduLength;
        ByteBuffer buf = ByteBuffer.allocate(totalLength).order(ByteOrder.BIG_ENDIAN);
        // MBAP Header
        buf.putShort((short) transactionId);  // Transaction ID
        buf.putShort((short) 0);              // Protocol ID
        buf.putShort((short) (1 + pduLength)); // Length (UnitId + PDU)
        // UnitId
        buf.put((byte) slaveId);
        // PDU
        buf.put((byte) functionCode);
        buf.put((byte) byteCount);
        for (int value : registerValues) {
            buf.putShort((short) value);
        }
        return buf.array();
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
