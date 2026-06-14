package cn.iocoder.yudao.module.iot.core.enums.shadow;

/**
 * IoT 设备影子协议常量
 * <p>
 * 内层 method、state 键名等与设备侧透传 JSON 对齐
 *
 * @author 芋道源码
 */
public final class IotDeviceShadowProtocolConstants {

    private IotDeviceShadowProtocolConstants() {
    }

    // ========== 内层 method ==========

    public static final String INNER_METHOD_GET = "get";
    public static final String INNER_METHOD_UPDATE = "update";
    public static final String INNER_METHOD_GET_REPLY = "get_reply";
    public static final String INNER_METHOD_UPDATE_REPLY = "update_reply";

    // ========== state / metadata 键 ==========

    public static final String STATE_DESIRED = "desired";
    public static final String STATE_REPORTED = "reported";
    public static final String METADATA_TIMESTAMP = "timestamp";

    // ========== payload 键 ==========

    public static final String PAYLOAD_STATUS = "status";
    public static final String PAYLOAD_STATE = "state";
    public static final String PAYLOAD_VERSION = "version";
    public static final String PAYLOAD_CONTENT = "content";
    public static final String PAYLOAD_ERROR_CODE = "errorcode";
    public static final String PAYLOAD_ERROR_MESSAGE = "errormessage";

    // ========== payload status 值 ==========

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

}
