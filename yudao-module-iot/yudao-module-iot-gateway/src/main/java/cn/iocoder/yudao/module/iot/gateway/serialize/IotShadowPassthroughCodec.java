package cn.iocoder.yudao.module.iot.gateway.serialize;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * IoT 设备影子透传编解码
 * <p>
 * 影子协议本质为 JSON 结构，透传编解码规则：
 * <ul>
 *     <li>{@code serializeType == null} 或 {@link IotSerializeTypeEnum#JSON}：按 JSON 编解码（未配置时默认 JSON，兼容历史设备）</li>
 *     <li>其它显式类型（如 {@link IotSerializeTypeEnum#BINARY}）：warn 并丢弃，返回 {@code null}，不静默喂给 JSON 解析器</li>
 * </ul>
 * 下行发布方收到 {@code null} 时应跳过发送，避免 NPE；影子功能目前仅支持 JSON 协议设备。
 *
 * @author 芋道源码
 */
@Slf4j
public final class IotShadowPassthroughCodec {

    private static final String UNSUPPORTED_MSG = "影子透传暂仅支持 JSON serializeType";

    private IotShadowPassthroughCodec() {
    }

    /**
     * 编码影子透传 params
     *
     * @param params        透传参数（Map 或 DTO）
     * @param serializeType 设备序列化类型
     * @return 线格式字节；非 JSON 类型返回 null
     */
    public static byte[] encode(Object params, IotSerializeTypeEnum serializeType) {
        if (isJsonSerializeType(serializeType)) {
            return JsonUtils.toJsonByte(params);
        }
        log.warn("[encode][{}，serializeType={}，已丢弃]", UNSUPPORTED_MSG, serializeType);
        return null;
    }

    /**
     * 解码影子透传 payload
     *
     * @param bytes         线格式字节
     * @param serializeType 设备序列化类型
     * @return 透传 params；非 JSON 类型或解析失败返回 null
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> decode(byte[] bytes, IotSerializeTypeEnum serializeType) {
        if (!isJsonSerializeType(serializeType)) {
            log.warn("[decode][{}，serializeType={}，已丢弃]", UNSUPPORTED_MSG, serializeType);
            return null;
        }
        return JsonUtils.parseObject(bytes, Map.class);
    }

    private static boolean isJsonSerializeType(IotSerializeTypeEnum serializeType) {
        // null 视为未配置，默认走 JSON（与产品/设备默认 serializeType 一致）
        return serializeType == null || IotSerializeTypeEnum.JSON.equals(serializeType);
    }

}
