package cn.iocoder.yudao.module.report.framework.ureport.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * UReport2 配置类
 *
 * @author 赤焰
 */
// @Configuration TODO 芋艿：JDK21 暂时不支持 UReport2，原因是 Spring Boot 3 的 javax 替换成 jakarta 了
@ImportResource({"classpath:ureport-console-context.xml"})
@PropertySource(value = {"classpath:ureport.properties"}) // TODO @赤焰：这个可以搞到 application.yaml 里么？
@EnableConfigurationProperties({UReportProperties.class})
public class UReportConfiguration {

//    TODO 芋艿：JDK21 暂时不支持 UReport2，原因是 Spring Boot 3 的 javax 替换成 jakarta 了
//    @Bean
//    public ServletRegistrationBean<Servlet> uReportRegistrationBean() {
//        return new ServletRegistrationBean<>(new UReportServlet(), "/ureport/*");
//    }

}
