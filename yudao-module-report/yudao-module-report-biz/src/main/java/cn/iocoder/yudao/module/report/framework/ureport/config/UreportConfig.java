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
 */
@PropertySource(value = {"classpath:ureport.properties"})
@ImportResource("classpath:ureport-console-context.xml")
@Configuration
public class UreportConfig{

    /**
     * ureport2报表Servlet配置
     */
    @Bean
    public ServletRegistrationBean<Servlet> ureport2Servlet(){
        return new ServletRegistrationBean<>(new UReportServlet(), "/ureport/*");
    }

}
