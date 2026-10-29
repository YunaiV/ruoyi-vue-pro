package cn.iocoder.yudao.module.iot.gateway.service.device.message.dto;

import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 下行指令按数据解析脚本 encode 的结果：走标准路径，或者透传写出 encode 的字节；由协议的下行订阅者执行
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IotDownstreamEncodeRespDTO {

    private static final IotDownstreamEncodeRespDTO STANDARD = new IotDownstreamEncodeRespDTO(true, null, null);

    /**
     * 是否走标准路径：设备不存在、产品脚本停用 / 未配置，或 encode 返回 standard
     */
    private final boolean standard;
    /**
     * 透传写出的字节；走标准路径时为 null
     */
    private final byte[] bytes;
    /**
     * encode 时使用的脚本（固定 hash）：协议回复设备的实际响应时用它 decode；走标准路径时为 null
     */
    private final IotScriptSource source;

    public static IotDownstreamEncodeRespDTO standard() {
        return STANDARD;
    }

    public static IotDownstreamEncodeRespDTO raw(byte[] bytes, IotScriptSource source) {
        return new IotDownstreamEncodeRespDTO(false, bytes, source);
    }

}
