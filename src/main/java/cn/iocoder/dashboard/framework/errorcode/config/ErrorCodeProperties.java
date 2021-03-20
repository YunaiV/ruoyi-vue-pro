package cn.iocoder.dashboard.framework.errorcode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@ConfigurationProperties("ruoyi.error-code")
@Validated
public class ErrorCodeProperties {

    /**
     * 应用分组
     */
    @NotNull(message = "应用分组不能为空，请设置 ruoyi.error-code.group 配置项，推荐直接使用 spring. application.name 配置项")
    private String group;
    /**
     * 错误码枚举类
     */
    private String constantsClass;

    public String getGroup() {
        return group;
    }

    public ErrorCodeProperties setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getConstantsClass() {
        return constantsClass;
    }

    public ErrorCodeProperties setConstantsClass(String constantsClass) {
        this.constantsClass = constantsClass;
        return this;
    }
}