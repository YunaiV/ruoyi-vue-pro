package cn.iocoder.dashboard.framework.errorcode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * 错误码的配置属性类
 *
 * @author dlyan
 */
@ConfigurationProperties("yudao.error-code")
@Data
@Validated
public class ErrorCodeProperties {

    /**
     * 错误码枚举类
     */
    @NotEmpty(message = "错误码枚举类不能为空")
    private String constantsClass;

}
