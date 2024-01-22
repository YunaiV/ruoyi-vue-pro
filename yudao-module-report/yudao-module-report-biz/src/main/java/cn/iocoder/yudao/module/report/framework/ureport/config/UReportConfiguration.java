package cn.iocoder.yudao.module.report.framework.ureport.config;

import com.bstek.ureport.console.UReportServlet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import javax.servlet.Servlet;

/**
 * UReport2 配置类
 *
 * @author 赤焰
 */
@Configuration
@ImportResource({"classpath:ureport-console-context.xml"})
@PropertySource(value = {"classpath:ureport.properties"}) // TODO @赤焰：这个可以搞到 application.yaml 里么？
@EnableConfigurationProperties({UReportProperties.class})
public class UReportConfiguration {

    @Bean
    public ServletRegistrationBean<Servlet> uReportRegistrationBean() {
        return new ServletRegistrationBean<>(new UReportServlet(), "/ureport/*");
    }

}
