package cn.iocoder.yudao.module.iot.core.topic.shadow;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;
import static cn.iocoder.yudao.module.iot.core.util.IotDeviceShadowProtocolUtils.*;

/**
 * IoT 设备影子下行 Reply DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#SHADOW_GET} 消息的 params 参数（透传原始线 body）
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceShadowReplyDTO {

    /**
     * 内层方法：get_reply / update_reply
     */
    private String method;

    /**
     * 回复载荷（对齐 CommonResult：code / msg / data）
     */
    private Object payload;

    /**
     * 影子版本号
     */
    private Long version;

    /**
     * 时间戳（epoch 秒）
     */
    private Long timestamp;

    public static IotDeviceShadowReplyDTO getReplyOf(Map<String, Object> desired, Map<String, Object> reported,
                                                     Long version, Long timestamp) {
        Map<String, Object> state = new HashMap<>();
        state.put(STATE_DESIRED, desired != null ? desired : new HashMap<>());
        state.put(STATE_REPORTED, reported != null ? reported : new HashMap<>());
        Map<String, Object> data = new HashMap<>();
        data.put(PAYLOAD_STATE, state);
        Map<String, Object> payload = buildSuccessPayload(data);
        return new IotDeviceShadowReplyDTO()
                .setMethod(INNER_METHOD_GET_REPLY)
                .setPayload(payload)
                .setVersion(version)
                .setTimestamp(timestamp);
    }

    public static IotDeviceShadowReplyDTO updateReplyOf(Long version, Long timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put(PAYLOAD_VERSION, version);
        Map<String, Object> payload = buildSuccessPayload(data);
        return new IotDeviceShadowReplyDTO()
                .setMethod(INNER_METHOD_UPDATE_REPLY)
                .setPayload(payload)
                .setVersion(version)
                .setTimestamp(timestamp);
    }

    public static IotDeviceShadowReplyDTO errorOf(Long version, Integer errorCode, String errorMsg, Long timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put(PAYLOAD_VERSION, version);
        Map<String, Object> payload = new HashMap<>();
        payload.put(PAYLOAD_CODE, errorCode);
        payload.put(PAYLOAD_MSG, errorMsg);
        payload.put(PAYLOAD_DATA, data);
        return new IotDeviceShadowReplyDTO()
                .setMethod(INNER_METHOD_UPDATE_REPLY)
                .setPayload(payload)
                .setVersion(version)
                .setTimestamp(timestamp);
    }

    private static Map<String, Object> buildSuccessPayload(Map<String, Object> data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(PAYLOAD_CODE, SUCCESS.getCode());
        payload.put(PAYLOAD_MSG, SUCCESS.getMsg());
        payload.put(PAYLOAD_DATA, data);
        return payload;
    }

}
