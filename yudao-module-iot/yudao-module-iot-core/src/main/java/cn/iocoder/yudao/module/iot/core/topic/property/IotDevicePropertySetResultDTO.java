package cn.iocoder.yudao.module.iot.core.topic.property;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备属性设置的单个属性结果
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#PROPERTY_SET} 回复消息的 data：key 为属性标识符，value 为本 DTO（逐项回复）
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDevicePropertySetResultDTO {

    /**
     * 结果码：0 成功，其它为失败的错误码
     */
    private Integer code;
    /**
     * 结果描述
     */
    private String msg;
    /**
     * 协议原始结果码（可为空）：OPC UA 为服务端 StatusCode（十六进制，例如 0x80730000），Modbus 为异常响应的异常码
     */
    private String statusCode;

    /**
     * 是否成功（不是 JavaBean 属性，不参与序列化）
     *
     * @return 是否成功
     */
    public boolean succeeded() {
        return GlobalErrorCodeConstants.SUCCESS.getCode().equals(code);
    }

    /**
     * 成功（没有协议结果码，例如 Modbus 设备正常回显）
     */
    public static IotDevicePropertySetResultDTO success() {
        return success(null);
    }

    /**
     * 成功，带协议结果码（例如 OPC UA 的 Good 0x00000000）
     */
    public static IotDevicePropertySetResultDTO success(String statusCode) {
        return new IotDevicePropertySetResultDTO(GlobalErrorCodeConstants.SUCCESS.getCode(),
                GlobalErrorCodeConstants.SUCCESS.getMsg(), statusCode);
    }

    /**
     * 失败：网关判定的错误码，params 为错误码文案的参数
     */
    public static IotDevicePropertySetResultDTO failure(ErrorCode errorCode, Object... params) {
        return failureWithStatus(errorCode, null, params);
    }

    /**
     * 失败，带协议结果码（OPC UA 的 Bad StatusCode、Modbus 的异常码）
     */
    public static IotDevicePropertySetResultDTO failureWithStatus(ErrorCode errorCode, String statusCode, Object... params) {
        String msg = ServiceExceptionUtil.doFormat(errorCode.getCode(), errorCode.getMsg(), params);
        return new IotDevicePropertySetResultDTO(errorCode.getCode(), msg, statusCode);
    }

}
