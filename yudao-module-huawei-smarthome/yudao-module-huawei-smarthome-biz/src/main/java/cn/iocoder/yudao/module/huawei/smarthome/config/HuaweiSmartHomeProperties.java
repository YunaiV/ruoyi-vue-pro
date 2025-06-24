package cn.iocoder.yudao.module.huawei.smarthome.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Configuration
@ConfigurationProperties(prefix = "yudao.huawei.smarthome")
@Data
@Validated
public class HuaweiSmartHomeProperties {

    /**
     * 华为智能家居平台的 API Endpoint
     * 例如：https://domain:port
     */
    @NotEmpty(message = "华为智能家居 API Endpoint 不能为空")
    private String endpoint;

    /**
     * 访问密钥 ID (AK)
     */
    @NotEmpty(message = "华为智能家居 AK 不能为空")
    private String accessKey;

    /**
     * 秘密访问密钥 (SK)
     */
    @NotEmpty(message = "华为智能家居 SK 不能为空")
    private String secretKey;

    /**
     * 项目 ID
     */
    @NotEmpty(message = "华为智能家居项目 ID 不能为空")
    private String projectId;

    /**
     * 连接超时时间，单位毫秒
     */
    private int connectTimeout = 10000; // 默认10秒

    /**
     * 读取超时时间，单位毫秒
     */
    private int readTimeout = 30000; // 默认30秒

}
