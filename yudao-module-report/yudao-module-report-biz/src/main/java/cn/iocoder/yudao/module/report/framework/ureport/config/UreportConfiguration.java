package cn.iocoder.yudao.module.report.framework.ureport.config;

import com.bstek.ureport.console.UReportServlet;
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
@ImportResource({"classpath:ureport-console-context.xml"}) // Bean 配置
@PropertySource(value = {"classpath:ureport.properties"}) // 配置文件
public class UreportConfiguration {

    // TODO @赤焰：bean 是不是取个和 ureport 相关的名字好点哈？
    @Bean
    public ServletRegistrationBean<Servlet> registrationBean() {
        return new ServletRegistrationBean<>(new UReportServlet(), "/ureport/*");
    }

}
