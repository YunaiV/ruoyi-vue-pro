package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TCP编解码器管理器（简化版）
 *
 * 核心功能：
 * - 自动协议检测（二进制 vs JSON）
 * - 统一编解码接口
 * - 默认使用 JSON 协议
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotTcpCodecManager implements IotDeviceMessageCodec {

    public static final String TYPE = "TCP";

    // TODO @haohao：@Resource

    @Autowired
    private IotTcpBinaryDeviceMessageCodec binaryCodec;

    @Autowired
    private IotTcpJsonDeviceMessageCodec jsonCodec;

    /**
     * 当前默认协议（JSON）
     */
    private boolean useJsonByDefault = true;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public byte[] encode(IotDeviceMessage message) {
        // 默认使用 JSON 协议编码
        return jsonCodec.encode(message);
    }

    // TODO @haohao：要不还是不自动检测，用户手动配置哈。简化一些。。。
    @Override
    public IotDeviceMessage decode(byte[] bytes) {
        // 自动检测协议类型并解码
        if (isJsonFormat(bytes)) {
            if (log.isDebugEnabled()) {
                log.debug("[decode][检测到 JSON 协议，数据长度: {} 字节]", bytes.length);
            }
            return jsonCodec.decode(bytes);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("[decode][检测到二进制协议，数据长度: {} 字节]", bytes.length);
            }
            return binaryCodec.decode(bytes);
        }
    }

    // ==================== 便捷方法 ====================

    /**
     * 使用 JSON 协议编码
     */
    public byte[] encodeJson(IotDeviceMessage message) {
        return jsonCodec.encode(message);
    }

    /**
     * 使用二进制协议编码
     */
    public byte[] encodeBinary(IotDeviceMessage message) {
        return binaryCodec.encode(message);
    }

    /**
     * 获取当前默认协议
     */
    public String getDefaultProtocol() {
        return useJsonByDefault ? "JSON" : "BINARY";
    }

    /**
     * 设置默认协议
     */
    public void setDefaultProtocol(boolean useJson) {
        this.useJsonByDefault = useJson;
        log.info("[setDefaultProtocol][设置默认协议] 使用JSON: {}", useJson);
    }

    // ==================== 内部方法 ====================

    /**
     * 检测是否为JSON格式
     *
     * 检测规则：
     * 1. 数据以 '{' 开头
     * 2. 包含 "method" 或 "id" 字段
     */
    private boolean isJsonFormat(byte[] bytes) {
        // TODO @haohao：ArrayUtil.isEmpty(bytes) 可以简化下
        if (bytes == null || bytes.length == 0) {
            return useJsonByDefault;
        }

        try {
            // 检测 JSON 格式：以 '{' 开头
            if (bytes[0] == '{') {
                // TODO @haohao：不一定按照顺序写，这个可能要看下。
                // 进一步验证是否为有效 JSON
                String jsonStr = new String(bytes, 0, Math.min(bytes.length, 100));
                return jsonStr.contains("\"method\"") || jsonStr.contains("\"id\"");
            }

            // 检测二进制格式：长度 >= 8 且符合二进制协议结构
            if (bytes.length >= 8) {
                // 读取包头（前 4 字节表示后续数据长度）
                int expectedLength = ((bytes[0] & 0xFF) << 24) |
                                   ((bytes[1] & 0xFF) << 16) |
                                   ((bytes[2] & 0xFF) << 8) |
                                   (bytes[3] & 0xFF);

                // 验证长度是否合理
                // TODO @haohao：expectedLength > 0 多余的貌似；
                if (expectedLength == bytes.length - 4 && expectedLength > 0 && expectedLength < 1024 * 1024) {
                    return false; // 二进制格式
                }
            }
        } catch (Exception e) {
            log.warn("[isJsonFormat][协议检测异常，使用默认协议: {}]", getDefaultProtocol(), e);
        }

        // 默认使用当前设置的协议类型
        return useJsonByDefault;
    }

}
