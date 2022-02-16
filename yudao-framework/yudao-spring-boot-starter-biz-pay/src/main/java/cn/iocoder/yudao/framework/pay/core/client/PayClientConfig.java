package cn.iocoder.yudao.framework.pay.core.client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

/**
 * 支付客户端的配置，本质是支付渠道的配置
 * 每个不同的渠道，需要不同的配置，通过子类来定义
 *
 * @author 芋道源码
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
// @JsonTypeInfo 注解的作用，Jackson 多态
// 1. 序列化到时数据库时，增加 @class 属性。
// 2. 反序列化到内存对象时，通过 @class 属性，可以创建出正确的类型
public interface PayClientConfig {

    /**
     * 配置验证参数是
     *
     * @param validator 校验对象
     * @return 配置好的验证参数
     */
    Set<ConstraintViolation<PayClientConfig>> verifyParam(Validator validator);

    // TODO @aquan：貌似抽象一个 validation group 就好了！
    /**
     * 参数校验
     *
     * @param validator 校验对象
     */
    default void validate(Validator validator) {
        Set<ConstraintViolation<PayClientConfig>> violations = verifyParam(validator);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
