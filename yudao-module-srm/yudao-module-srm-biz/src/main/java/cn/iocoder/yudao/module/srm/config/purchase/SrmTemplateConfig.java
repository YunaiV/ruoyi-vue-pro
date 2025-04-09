package cn.iocoder.yudao.module.srm.config.purchase;

import cn.iocoder.yudao.framework.template.config.TemplateRegistar;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Objects;

@Configuration
public class SrmTemplateConfig {

    @Value("classpath:purchase/order/*.docx")
    public static List<Resource> resources;

    public static Resource getResource(String filename) {
        return resources.stream()
            .filter(r-> Objects.equals(r.getFilename(), filename))
            .toList().get(0);
    }

    @Bean
    @ConfigurationProperties(prefix = "srm")
    public TemplateRegistar srmTemplate() {
        var policy = new TemplateRegistar();
        policy.setResources(resources);
        policy.setPolicy(new LoopRowTableRenderPolicy());
        return policy;
    }
}
