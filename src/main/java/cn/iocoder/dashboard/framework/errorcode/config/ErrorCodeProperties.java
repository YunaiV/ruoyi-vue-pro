package cn.iocoder.dashboard.framework.errorcode.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@ConfigurationProperties("yudao.error-code")
@Validated
@Getter
@Setter
public class ErrorCodeProperties {

    /**
     * 应用分组
     */
    @NotNull(message = "应用分组不能为空，请设置 yudao.error-code.group 配置项，推荐直接使用 spring.application.name 配置项")
    private String group;
    /**
     * 错误码枚举类
     */
    private String constantsClass;
}