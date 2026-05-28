package cn.iocoder.yudao.module.yaya.framework.ai;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yaya.ai")
@Data
@Accessors(chain = true)
public class YayaAiProperties {

    private String baseUrl = "http://127.0.0.1:18080";
    private String internalKey = "local-internal-key";
    private Integer timeoutSeconds = 60;

}
