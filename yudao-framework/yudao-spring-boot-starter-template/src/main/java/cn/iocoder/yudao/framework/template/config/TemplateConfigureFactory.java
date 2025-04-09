package cn.iocoder.yudao.framework.template.config;

import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 收集所有模块渲染word的策略，模块实现TemplatePolicyRegistrar接口
 */
@Configuration
@Getter
public class TemplateConfigureFactory {

    @Autowired
    private List<TemplatePolicyRegistrar> registrars;

    public Configure build() {
        ConfigureBuilder builder = Configure.builder();
        registrars.forEach(r -> r.register(builder));
        return builder.build();
    }
}
