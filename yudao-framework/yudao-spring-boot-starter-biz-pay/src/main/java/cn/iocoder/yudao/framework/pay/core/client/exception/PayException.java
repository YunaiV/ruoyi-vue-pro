package cn.iocoder.yudao.framework.pay.core.client.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 业务逻辑异常 Exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PayException extends RuntimeException {

    /**
     * 第三方平台的错误码
     */
    private String code;
    /**
     * 第三方平台的错误提示
     */
    private String message;

}
