package cn.iocoder.dashboard.framework.tracer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("yudao.tracer")
@Data
public class BizTracerProperties {
}
