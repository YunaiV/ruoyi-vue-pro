package cn.iocoder.yudao.module.iot.core.topic.shadow;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.module.iot.core.enums.shadow.IotDeviceShadowProtocolConstants.*;

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
     * 回复载荷
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
        Map<String, Object> payload = new HashMap<>();
        payload.put(PAYLOAD_STATUS, STATUS_SUCCESS);
        payload.put(PAYLOAD_STATE, state);
        return new IotDeviceShadowReplyDTO()
                .setMethod(INNER_METHOD_GET_REPLY)
                .setPayload(payload)
                .setVersion(version)
                .setTimestamp(timestamp);
    }

    public static IotDeviceShadowReplyDTO updateReplyOf(Long version, Long timestamp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(PAYLOAD_STATUS, STATUS_SUCCESS);
        payload.put(PAYLOAD_VERSION, version);
        return new IotDeviceShadowReplyDTO()
                .setMethod(INNER_METHOD_UPDATE_REPLY)
                .setPayload(payload)
                .setVersion(version)
                .setTimestamp(timestamp);
    }

    public static IotDeviceShadowReplyDTO errorOf(Long version, Integer errorcode, String errormessage, Long timestamp) {
        Map<String, Object> content = new HashMap<>();
        content.put(PAYLOAD_ERROR_CODE, errorcode);
        content.put(PAYLOAD_ERROR_MESSAGE, errormessage);
        Map<String, Object> payload = new HashMap<>();
        payload.put(PAYLOAD_STATUS, STATUS_ERROR);
        payload.put(PAYLOAD_VERSION, version);
        payload.put(PAYLOAD_CONTENT, content);
        return new IotDeviceShadowReplyDTO()
                .setMethod(INNER_METHOD_UPDATE_REPLY)
                .setPayload(payload)
                .setVersion(version)
                .setTimestamp(timestamp);
    }

}
