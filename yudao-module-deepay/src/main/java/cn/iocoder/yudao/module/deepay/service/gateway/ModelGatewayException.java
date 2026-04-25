package cn.iocoder.yudao.module.deepay.service.gateway;

/**
 * 模型网关调用异常。
 */
public class ModelGatewayException extends RuntimeException {

    public ModelGatewayException(String message) {
        super(message);
    }

    public ModelGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

}
