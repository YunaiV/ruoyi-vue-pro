package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

// TODO @haohao：【重要】是不是二进制更彻底哈？
// 包头(4 字节)
// 消息 ID string；nvarchar（length + string）
// version（可选，不要干脆）
// method string；nvarchar；为什么不要 opcode？因为 IotTcpJsonDeviceMessageCodec 里面，实际已经没 opcode 了
// reply bit；0 请求，1 响应
// 请求时：
//      params；nvarchar；json 处理
// 响应时：
//      code
//      msg nvarchar
//      data；nvarchar；json 处理
/**
 * TCP 二进制格式 {@link IotDeviceMessage} 编解码器
 *
 * 使用自定义二进制协议格式：包头(4 字节) | 功能码(2 字节) | 消息序号(2 字节) | 包体数据(变长)
 *
 * @author 芋道源码
 */
@Component
public class IotTcpBinaryDeviceMessageCodec implements IotDeviceMessageCodec {

    // TODO @haohao：是不是叫 TCP_Binary 好点哈？
    public static final String TYPE = "TCP_BINARY";

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TcpBinaryMessage {

        /**
         * 功能码
         */
        private Short code;

        // TODO @haohao：这个和 AlinkMessage 里面，是一个东西哇？
        /**
         * 消息序号
         */
        private Short mid;

        // TODO @haohao：这个字段，是不是没用到呀？感觉应该也不在消息列哈？
        /**
         * 设备 ID
         */
        private Long deviceId;

        /**
         * 请求方法
         */
        private String method;

        /**
         * 请求参数
         */
        private Object params;

        /**
         * 响应结果
         */
        private Object data;

        // TODO @haohao：这个可以改成 code 哇？更好理解一点；
        /**
         * 响应错误码
         */
        private Integer responseCode;

        /**
         * 响应提示
         */
        private String msg;

        // TODO @haohao：TcpBinaryMessage 和 TcpJsonMessage 保持一致哈？

    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public byte[] encode(IotDeviceMessage message) {
        Assert.notNull(message, "消息不能为空");
        Assert.notBlank(message.getMethod(), "消息方法不能为空");

        try {
            // 1. 确定功能码
            short code = MessageMethod.STATE_ONLINE.equals(message.getMethod())
                    ? TcpDataPackage.CODE_HEARTBEAT : TcpDataPackage.CODE_MESSAGE_UP;

            // 2. 构建负载数据
            String payload = buildPayload(message);

            // 3. 构建 TCP 数据包
            // TODO @haohao：这个和 AlinkMessage.id 是不是一致的哈？
            short mid = (short) (System.currentTimeMillis() % Short.MAX_VALUE);
            TcpDataPackage dataPackage = new TcpDataPackage(code, mid, payload);

            // 4. 编码为字节流
            return encodeTcpDataPackage(dataPackage).getBytes();
        } catch (Exception e) {
            throw new TcpCodecException("TCP 消息编码失败", e);
        }
    }

    @Override
    public IotDeviceMessage decode(byte[] bytes) {
        Assert.notNull(bytes, "待解码数据不能为空");
        Assert.isTrue(bytes.length > 0, "待解码数据不能为空");

        try {
            // 1. 解码 TCP 数据包
            TcpDataPackage dataPackage = decodeTcpDataPackage(Buffer.buffer(bytes));

            // 2. 根据功能码确定方法
            String method = (dataPackage.getCode() == TcpDataPackage.CODE_HEARTBEAT) ? MessageMethod.STATE_ONLINE
                    : MessageMethod.PROPERTY_POST;

            // 3. 解析负载数据
            PayloadInfo payloadInfo = parsePayloadInfo(dataPackage.getPayload());

            // 4. 构建 IoT 设备消息
            return IotDeviceMessage.of(
                    payloadInfo.getRequestId(),
                    method,
                    payloadInfo.getParams(),
                    null,
                    null,
                    null);
        } catch (Exception e) {
            throw new TcpCodecException("TCP 消息解码失败", e);
        }
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 构建负载数据
     *
     * @param message 设备消息
     * @return 负载字符串
     */
    private String buildPayload(IotDeviceMessage message) {
        TcpBinaryMessage tcpBinaryMessage = new TcpBinaryMessage(
                null, // code 在数据包中单独处理
                null, // mid 在数据包中单独处理
                message.getDeviceId(),
                message.getMethod(),
                message.getParams(),
                message.getData(),
                message.getCode(),
                message.getMsg());
        return JsonUtils.toJsonString(tcpBinaryMessage);
    }

    /**
     * 解析负载信息
     *
     * @param payload 负载字符串
     * @return 负载信息
     */
    private PayloadInfo parsePayloadInfo(String payload) {
        if (StrUtil.isBlank(payload)) {
            return new PayloadInfo(null, null);
        }

        try {
            TcpBinaryMessage tcpBinaryMessage = JsonUtils.parseObject(payload, TcpBinaryMessage.class);
            if (tcpBinaryMessage != null) {
                return new PayloadInfo(
                        StrUtil.isNotEmpty(tcpBinaryMessage.getMethod())
                                ? tcpBinaryMessage.getMethod() + "_" + System.currentTimeMillis()
                                : null,
                        tcpBinaryMessage.getParams());
            }
        } catch (Exception e) {
            // 如果解析失败，返回默认值
            return new PayloadInfo("unknown_" + System.currentTimeMillis(), null);
        }
        return null;
    }

    /**
     * 编码 TCP 数据包
     *
     * @param dataPackage 数据包对象
     * @return 编码后的字节流
     */
    private Buffer encodeTcpDataPackage(TcpDataPackage dataPackage) {
        Assert.notNull(dataPackage, "数据包对象不能为空");
        Assert.notNull(dataPackage.getPayload(), "负载不能为空");

        // 1. 计算包体长度（除了包头 4 字节）
        int payloadLength = dataPackage.getPayload().getBytes().length;
        int totalLength = 2 + 2 + payloadLength;

        // 2. 写入数据
        Buffer buffer = Buffer.buffer();
        // 2.1 写入包头：总长度（4 字节）
        buffer.appendInt(totalLength);
        // 2.2 写入功能码（2 字节）
        buffer.appendShort(dataPackage.getCode());
        // 2.3 写入消息序号（2 字节）
        buffer.appendShort(dataPackage.getMid());
        // 2.4 写入包体数据（不定长）
        buffer.appendBytes(dataPackage.getPayload().getBytes());
        return buffer;
    }

    /**
     * 解码 TCP 数据包
     *
     * @param buffer 数据缓冲区
     * @return 解码后的数据包
     */
    private TcpDataPackage decodeTcpDataPackage(Buffer buffer) {
        Assert.isTrue(buffer.length() >= 8, "数据包长度不足");

        int index = 0;
        // 1. 跳过包头（4 字节）
        index += 4;
        // 2. 获取功能码（2 字节）
        short code = buffer.getShort(index);
        index += 2;
        // 3. 获取消息序号（2 字节）
        short mid = buffer.getShort(index);
        index += 2;
        // 4. 获取包体数据
        String payload = "";
        if (index < buffer.length()) {
            payload = buffer.getString(index, buffer.length());
        }

        return new TcpDataPackage(code, mid, payload);
    }

    // ==================== 内部类 ====================

    // TODO @haohao：会不会存在 reply 的时候，有 data、msg、code 参数哈。
    /**
     * 负载信息类
     */
    @Data
    @AllArgsConstructor
    private static class PayloadInfo {

        private String requestId;
        private Object params;

    }

    /**
     * TCP 数据包内部类
     */
    @Data
    @AllArgsConstructor
    private static class TcpDataPackage {

        // 功能码定义
        public static final short CODE_REGISTER = 10;
        public static final short CODE_REGISTER_REPLY = 11;
        public static final short CODE_HEARTBEAT = 20;
        public static final short CODE_HEARTBEAT_REPLY = 21;
        public static final short CODE_MESSAGE_UP = 30;
        public static final short CODE_MESSAGE_DOWN = 40;

        // TODO @haohao：要不改成 opCode
        private short code;
        private short mid;
        private String payload;

    }

    // ==================== 常量定义 ====================

    /**
     * 消息方法常量
     */
    public static class MessageMethod {

        public static final String PROPERTY_POST = "thing.property.post"; // 数据上报
        public static final String STATE_ONLINE = "thing.state.online"; // 心跳

    }

    // ==================== 自定义异常 ====================

    // TODO @haohao：全局异常搞个。看着可以服用哈。
    /**
     * TCP 编解码异常
     */
    public static class TcpCodecException extends RuntimeException {
        public TcpCodecException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}