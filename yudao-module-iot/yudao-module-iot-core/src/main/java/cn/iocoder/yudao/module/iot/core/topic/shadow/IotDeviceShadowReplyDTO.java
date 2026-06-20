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
 * 扁平对齐 CommonResult + method，用于 {@link IotDeviceMessageMethodEnum#SHADOW_GET}
 * 透传线 body。
 * 错误回复的 {@link #data} 仍携带 version 供设备 resync，有意偏离纯 CommonResult（error 时 data
 * 通常为 null）。
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
     * 结果码，0 表示成功
     */
    private Integer code;

    /**
     * 结果描述
     */
    private String msg;

    /**
     * 业务数据：get_reply 为 {state, version, timestamp}；update_reply 为 {version,
     * timestamp}
     */
    private Object data;

    public static IotDeviceShadowReplyDTO getReplyOf(Map<String, Object> desired, Map<String, Object> reported,
            Long version, Long timestamp) {
        Map<String, Object> state = new HashMap<>();
        state.put(STATE_DESIRED, desired != null ? desired : new HashMap<>());
        state.put(STATE_REPORTED, reported != null ? reported : new HashMap<>());
        Map<String, Object> data = new HashMap<>();
        data.put(PAYLOAD_STATE, state);
        data.put(PAYLOAD_VERSION, version);
        data.put(METADATA_TIMESTAMP, timestamp);
        return of(INNER_METHOD_GET_REPLY, SUCCESS.getCode(), SUCCESS.getMsg(), data);
    }

    public static IotDeviceShadowReplyDTO updateReplyOf(Long version, Long timestamp) {
        return of(INNER_METHOD_UPDATE_REPLY, SUCCESS.getCode(), SUCCESS.getMsg(),
                dataWithVersionTimestamp(version, timestamp));
    }

    public static IotDeviceShadowReplyDTO errorOf(String innerMethod, Long version, Integer errorCode,
            String errorMsg, Long timestamp) {
        return of(toReplyMethod(innerMethod), errorCode, errorMsg, dataWithVersionTimestamp(version, timestamp));
    }

    private static IotDeviceShadowReplyDTO of(String method, Integer code, String msg, Map<String, Object> data) {
        return new IotDeviceShadowReplyDTO().setMethod(method).setCode(code).setMsg(msg).setData(data);
    }

    private static Map<String, Object> dataWithVersionTimestamp(Long version, Long timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put(PAYLOAD_VERSION, version);
        data.put(METADATA_TIMESTAMP, timestamp);
        return data;
    }

}
