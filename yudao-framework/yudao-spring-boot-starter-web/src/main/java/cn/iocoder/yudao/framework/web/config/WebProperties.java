package cn.iocoder.yudao.framework.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "yudao.web")
@Validated
@Data
public class WebProperties {

    @NotNull(message = "APP API 不能为空")
    private Api appApi;
    @NotNull(message = "Admin API 不能为空")
    private Api adminApi;

    @Data
    @Valid
    public static class Api {

        /**
         * API 前缀，实现所有 Controller 提供的 RESTFul API 的统一前缀
         *
         *
         * 意义：通过该前缀，避免 Swagger、Actuator 意外通过 Nginx 暴露出来给外部，带来安全性问题
         *      这样，Nginx 只需要配置转发到 /api/* 的所有接口即可。
         *
         * @see YudaoWebAutoConfiguration#configurePathMatch(PathMatchConfigurer)
         */
        @NotEmpty(message = "API 前缀不能为空")
        private String prefix;

        /**
         * Controller 所在包
         *
         * 主要目的是，给该 Controller 设置指定的 {@link #prefix}
         *
         * 因为我们有多个 modules 包里会包含 Controller，所以只需要写到 cn.iocoder.yudao 这样的层级
         */
        @NotEmpty(message = "Controller 所在包不能为空")
        private String controller;

    }

}
