package cn.iocoder.yudao.module.pay.framework.pay.core.client.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付系统异常 Exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PayClientException extends RuntimeException {

    public PayClientException(Throwable cause) {
        super(cause);
    }

}
