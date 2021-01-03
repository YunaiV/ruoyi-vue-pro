package cn.iocoder.dashboard.common.exception;

import cn.iocoder.dashboard.common.exception.enums.GlobalErrorCodeConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局异常 Exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GlobalException extends RuntimeException {

    /**
     * 全局错误码
     *
     * @see GlobalErrorCodeConstants
     */
    private Integer code;
    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public GlobalException() {
    }

    public GlobalException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public GlobalException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
