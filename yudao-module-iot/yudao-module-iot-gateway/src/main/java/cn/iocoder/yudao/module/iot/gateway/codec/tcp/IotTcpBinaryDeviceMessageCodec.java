package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import io.vertx.core.buffer.Buffer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// TODO @haohao：设备地址(变长) 是不是非必要哈？因为认证后，不需要每次都带呀。
/**
 * TCP 二进制格式 {@link IotDeviceMessage} 编解码器
 *
 * 使用自定义二进制协议格式：
 * 包头(4 字节) | 地址长度(2 字节) | 设备地址(变长) | 功能码(2 字节) | 消息序号(2 字节) | 包体数据(变长)
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotTcpBinaryDeviceMessageCodec implements IotDeviceMessageCodec {

    /**
     * 编解码器类型
     */
    public static final String TYPE = "TCP_BINARY";

    // TODO @haohao：这个注释不太对。
    // ==================== 常量定义 ====================

    @Override
    public byte[] encode(IotDeviceMessage message) {
        if (message == null || StrUtil.isEmpty(message.getMethod())) {
            throw new IllegalArgumentException("消息或方法不能为空");
        }

        try {
            // 1. 确定功能码（只支持数据上报和心跳）
            short code = MessageMethod.STATE_ONLINE.equals(message.getMethod()) ?
                TcpDataPackage.CODE_HEARTBEAT : TcpDataPackage.CODE_MESSAGE_UP;

            // 2. 构建简化负载
            String payload = buildSimplePayload(message);

            // 3. 构建 TCP 数据包
            String deviceAddr = message.getDeviceId() != null ? String.valueOf(message.getDeviceId()) : "default";
            short mid = (short) (System.currentTimeMillis() % Short.MAX_VALUE);
            TcpDataPackage dataPackage = new TcpDataPackage(deviceAddr, code, mid, payload);

            // 4. 编码为字节流
            return encodeTcpDataPackage(dataPackage).getBytes();
        } catch (Exception e) {
            log.error("[encode][编码失败] 方法: {}", message.getMethod(), e);
            throw new TcpCodecException("TCP 消息编码失败", e);
        }
    }

    @Override
    public IotDeviceMessage decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("待解码数据不能为空");
        }

        try {
            // 1. 解码 TCP 数据包
            TcpDataPackage dataPackage = decodeTcpDataPackage(Buffer.buffer(bytes));

            // 2. 根据功能码确定方法
            // TODO @haohao：会不会有事件上报哈。
            String method = (dataPackage.getCode() == TcpDataPackage.CODE_HEARTBEAT) ?
                MessageMethod.STATE_ONLINE : MessageMethod.PROPERTY_POST;

            // 3. 解析负载数据和请求 ID
            PayloadInfo payloadInfo = parsePayloadInfo(dataPackage.getPayload());

            // 4. 构建 IoT 设备消息（设置完整的必要参数）
            IotDeviceMessage message = IotDeviceMessage.requestOf(
                payloadInfo.getRequestId(), method, payloadInfo.getParams());

            // 5. 设置设备相关信息
            // TODO @haohao：serverId 不是这里解析的哈。
            Long deviceId = parseDeviceId(dataPackage.getAddr());
            message.setDeviceId(deviceId);

            // 6. 设置 TCP 协议相关信息
            // TODO @haohao：serverId 不是这里解析的哈。
            message.setServerId(generateServerId(dataPackage));

            // 7. 设置租户 ID（TODO: 后续可以从设备信息中获取）
            // TODO @haohao：租户 id 不是这里解析的哈。
            // message.setTenantId(getTenantIdByDeviceId(deviceId));

            if (log.isDebugEnabled()) {
                log.debug("[decode][解码成功] 设备ID: {}, 方法: {}, 请求ID: {}, 消息ID: {}",
                        deviceId, method, message.getRequestId(), message.getId());
            }

            return message;
        } catch (Exception e) {
            log.error("[decode][解码失败] 数据长度: {}", bytes.length, e);
            throw new TcpCodecException("TCP 消息解码失败", e);
        }
    }

    @Override
    public String type() {
        return TYPE;
    }

    // TODO @haohao：这种简单解析，中间不用空格哈。
    /**
     * 构建完整负载
     */
    private String buildSimplePayload(IotDeviceMessage message) {
        JSONObject payload = new JSONObject();

        // 核心字段
        payload.set(PayloadField.METHOD, message.getMethod());
        if (message.getParams() != null) {
            payload.set(PayloadField.PARAMS, message.getParams());
        }

        // 标识字段
        if (StrUtil.isNotEmpty(message.getRequestId())) {
            payload.set(PayloadField.REQUEST_ID, message.getRequestId());
        }
        if (StrUtil.isNotEmpty(message.getId())) {
            payload.set(PayloadField.MESSAGE_ID, message.getId());
        }

        // 时间戳
        payload.set(PayloadField.TIMESTAMP, System.currentTimeMillis());

        return payload.toString();
    }

    // ==================== 编解码方法 ====================

    /**
     * 解析负载信息（包含 requestId 和 params）
     */
    private PayloadInfo parsePayloadInfo(String payload) {
        if (StrUtil.isEmpty(payload)) {
            return new PayloadInfo(null, null);
        }

        try {
            // TODO @haohao：使用 jsonUtils
            JSONObject jsonObject = JSONUtil.parseObj(payload);
            String requestId = jsonObject.getStr(PayloadField.REQUEST_ID);
            if (StrUtil.isEmpty(requestId)) {
                requestId = jsonObject.getStr(PayloadField.MESSAGE_ID);
            }
            Object params = jsonObject.get(PayloadField.PARAMS);
            return new PayloadInfo(requestId, params);
        } catch (Exception e) {
            log.warn("[parsePayloadInfo][解析失败，返回原始字符串] 负载: {}", payload);
            return new PayloadInfo(null, payload);
        }
    }

    /**
     * 从设备地址解析设备ID
     *
     * @param deviceAddr 设备地址字符串
     * @return 设备ID
     */
    private Long parseDeviceId(String deviceAddr) {
        if (StrUtil.isEmpty(deviceAddr)) {
            log.warn("[parseDeviceId][设备地址为空，返回默认ID]");
            return 0L;
        }

        try {
            // 尝试直接解析为Long
            return Long.parseLong(deviceAddr);
        } catch (NumberFormatException e) {
            // 如果不是纯数字，可以使用哈希值或其他策略
            log.warn("[parseDeviceId][设备地址不是数字格式: {}，使用哈希值]", deviceAddr);
            return (long) deviceAddr.hashCode();
        }
    }

    /**
     * 生成服务ID
     *
     * @param dataPackage TCP数据包
     * @return 服务ID
     */
    private String generateServerId(TcpDataPackage dataPackage) {
        // 使用协议类型 + 设备地址 + 消息序号生成唯一的服务 ID
        return String.format("tcp_%s_%d", dataPackage.getAddr(), dataPackage.getMid());
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 编码 TCP 数据包
     *
     * @param dataPackage 数据包对象
     * @return 编码后的字节流
     * @throws IllegalArgumentException 如果数据包对象不正确
     */
    private Buffer encodeTcpDataPackage(TcpDataPackage dataPackage) {
        if (dataPackage == null) {
            throw new IllegalArgumentException("数据包对象不能为空");
        }

        // 验证数据包
        if (dataPackage.getAddr() == null || dataPackage.getAddr().isEmpty()) {
            throw new IllegalArgumentException("设备地址不能为空");
        }
        if (dataPackage.getPayload() == null) {
            throw new IllegalArgumentException("负载不能为空");
        }

        try {
            Buffer buffer = Buffer.buffer();

            // 1. 计算包体长度（除了包头 4 字节）
            int payloadLength = dataPackage.getPayload().getBytes().length;
            int totalLength = 2 + dataPackage.getAddr().length() + 2 + 2 + payloadLength;

            // 2.1 写入包头：总长度（4 字节）
            buffer.appendInt(totalLength);
            // 2.2 写入设备地址长度（2 字节）
            buffer.appendShort((short) dataPackage.getAddr().length());
            // 2.3 写入设备地址（不定长）
            buffer.appendBytes(dataPackage.getAddr().getBytes());
            // 2.4 写入功能码（2 字节）
            buffer.appendShort(dataPackage.getCode());
            // 2.5 写入消息序号（2 字节）
            buffer.appendShort(dataPackage.getMid());
            // 2.6 写入包体数据（不定长）
            buffer.appendBytes(dataPackage.getPayload().getBytes());

            if (log.isDebugEnabled()) {
                log.debug("[encodeTcpDataPackage][编码成功] 设备地址: {}, 功能码: {}, 消息序号: {}, 总长度: {}",
                        dataPackage.getAddr(), dataPackage.getCode(), dataPackage.getMid(), buffer.length());
            }
            return buffer;
        } catch (Exception e) {
            log.error("[encodeTcpDataPackage][编码失败] 数据包: {}", dataPackage, e);
            throw new IllegalArgumentException("数据包编码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解码 TCP 数据包
     *
     * @param buffer 数据缓冲区
     * @return 解码后的数据包
     * @throws IllegalArgumentException 如果数据包格式不正确
     */
    private TcpDataPackage decodeTcpDataPackage(Buffer buffer) {
        if (buffer == null || buffer.length() < 8) {
            throw new IllegalArgumentException("数据包长度不足");
        }

        try {
            int index = 0;

            // 1.1 跳过包头（4字节）
            index += 4;

            // 1.2 获取设备地址长度（2字节）
            short addrLength = buffer.getShort(index);
            index += 2;

            // 1.3 获取设备地址
            String addr = buffer.getBuffer(index, index + addrLength).toString();
            index += addrLength;

            // 1.4 获取功能码（2字节）
            short code = buffer.getShort(index);
            index += 2;

            // 1.5 获取消息序号（2字节）
            short mid = buffer.getShort(index);
            index += 2;

            // 1.6 获取包体数据
            String payload = "";
            if (index < buffer.length()) {
                payload = buffer.getString(index, buffer.length());
            }

            // 2. 构建数据包对象
            TcpDataPackage dataPackage = new TcpDataPackage(addr, code, mid, payload);

            if (log.isDebugEnabled()) {
                log.debug("[decodeTcpDataPackage][解码成功] 设备地址: {}, 功能码: {}, 消息序号: {}, 包体长度: {}",
                        addr, code, mid, payload.length());
            }
            return dataPackage;
        } catch (Exception e) {
            log.error("[decodeTcpDataPackage][解码失败] 数据长度: {}", buffer.length(), e);
            throw new IllegalArgumentException("数据包解码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 消息方法常量
     */
    public static class MessageMethod {

        public static final String PROPERTY_POST = "thing.property.post";  // 数据上报
        public static final String STATE_ONLINE = "thing.state.online";    // 心跳

    }

    /**
     * 负载字段名
     */
    private static class PayloadField {

        public static final String METHOD = "method";
        public static final String PARAMS = "params";
        public static final String TIMESTAMP = "timestamp";
        public static final String REQUEST_ID = "requestId";
        public static final String MESSAGE_ID = "msgId";

    }

    // ==================== TCP 数据包编解码方法 ====================

    // TODO @haohao：lombok 简化
    /**
     * 负载信息类
     */
    private static class PayloadInfo {
        private String requestId;
        private Object params;

        public PayloadInfo(String requestId, Object params) {
            this.requestId = requestId;
            this.params = params;
        }

        public String getRequestId() { return requestId; }
        public Object getParams() { return params; }
    }

    /**
     * TCP 数据包内部类
     */
    @Data
    private static class TcpDataPackage {
        // 功能码定义
        public static final short CODE_REGISTER = 10;
        public static final short CODE_REGISTER_REPLY = 11;
        public static final short CODE_HEARTBEAT = 20;
        public static final short CODE_HEARTBEAT_REPLY = 21;
        public static final short CODE_MESSAGE_UP = 30;
        public static final short CODE_MESSAGE_DOWN = 40;

        private String addr;
        private short code;
        private short mid;
        private String payload;

        public TcpDataPackage(String addr, short code, short mid, String payload) {
            this.addr = addr;
            this.code = code;
            this.mid = mid;
            this.payload = payload;
        }
    }

    // ==================== 自定义异常 ====================

    // TODO @haohao：可以搞个全局的；
    /**
     * TCP 编解码异常
     */
    public static class TcpCodecException extends RuntimeException {

        // TODO @haohao：非必要构造方法，可以去掉哈。
        public TcpCodecException(String message) {
            super(message);
        }

        public TcpCodecException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}