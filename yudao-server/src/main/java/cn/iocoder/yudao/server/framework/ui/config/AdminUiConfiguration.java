package cn.iocoder.yudao.server.framework.ui.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * yudao-admin-ui 的配置类
 *
 * @author 芋道源码
 */
@Configuration
public class AdminUiConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/admin-ui/**", "/admin-ui/", "/admin-ui")
                .addResourceLocations("classpath:/admin-ui/")
                // 自定义 ClassPathResource 实现类，在前端请求的地址匹配不到对应的路径时，强制使用 /admin-ui/index.html 资源
                // 本质上，等价于 nginx 在处理不到 Vue 的请求地址时，try_files 到 index.html 地址
                // 想要彻底理解，可以调试 ResourceHttpRequestHandler 的 resolveResourceLocations 方法，前端请求 /admin-ui/system/tenant 地址
                .addResourceLocations(new ClassPathResource("/admin-ui/index.html") {

                    @Override
                    public Resource createRelative(String relativePath) {
                        return this;
                    }

                })
        ;
    }

}
