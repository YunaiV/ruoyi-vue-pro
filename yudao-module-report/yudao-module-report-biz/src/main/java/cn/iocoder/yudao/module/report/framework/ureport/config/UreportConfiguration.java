package cn.iocoder.yudao.module.report.framework.ureport.config;

import com.bstek.ureport.console.UReportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import javax.servlet.Servlet;

/**
 * Ureport 配置类
 * 加载ureport对应的xml配置文件
 * @author  赤焰
 */
@Configuration
@ImportResource({"classpath:ureport-console-context.xml"})
@PropertySource(value = {"classpath:ureport.properties"})
public class UreportConfiguration {

    @Bean
    public ServletRegistrationBean<Servlet> registrationBean() {
        return new ServletRegistrationBean<>(new UReportServlet(), "/ureport/*");
    }

}
