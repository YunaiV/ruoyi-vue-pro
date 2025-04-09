package cn.iocoder.yudao.module.srm.config.purchase;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "srm")
public class PurchaseProperties {

    /**
     * 是否启用模板预热
     */
    private boolean enablePreload = false;

    /**
     * 模板扫描路径（支持多个 classpath 目录）
     */
    private List<String> scanPath;
}
