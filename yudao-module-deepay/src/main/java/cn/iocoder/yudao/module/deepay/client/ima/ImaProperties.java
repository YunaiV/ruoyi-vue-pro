package cn.iocoder.yudao.module.deepay.client.ima;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ima 知识库 API 配置。
 *
 * <p>对应 {@code application.yaml} 中的 {@code deepay.ima} 前缀。
 * {@code enabled=false} 时，{@link cn.iocoder.yudao.module.deepay.agent.ImaAgent} 跳过所有 ima 调用。</p>
 */
@Data
@ConfigurationProperties(prefix = "deepay.ima")
public class ImaProperties {

    /**
     * 是否启用 ima 同步。默认关闭，避免未配置 API Key 时报错。
     */
    private boolean enabled = false;

    /**
     * ima API 基础 URL。
     */
    private String baseUrl = "https://ima.qq.com";

    /**
     * ima API 密钥（Bearer Token）。
     */
    private String apiKey = "";

}
