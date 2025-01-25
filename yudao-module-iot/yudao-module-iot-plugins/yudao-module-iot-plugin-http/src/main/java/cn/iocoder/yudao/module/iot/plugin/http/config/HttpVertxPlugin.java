package cn.iocoder.yudao.module.iot.plugin.http.config;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 负责插件的启动和停止，与 Vert.x 的生命周期管理
 */
@Slf4j
public class HttpVertxPlugin extends SpringPlugin {

    public HttpVertxPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // TODO @haohao：这种最好启动中，启动完成，成对打印日志，方便定位问题
        log.info("[HttpVertxPlugin][start ...]");

        // 1. 获取插件上下文
        ApplicationContext pluginContext = getApplicationContext();
        if (pluginContext == null) {
            log.error("[HttpVertxPlugin] pluginContext is null, start failed.");
            return;
        }

        // 2. 启动 Vertx
        VertxService vertxService = pluginContext.getBean(VertxService.class);
        vertxService.startServer();
    }


    @Override
    public void stop() {
        log.info("[HttpVertxPlugin][stop ...]");
        ApplicationContext pluginContext = getApplicationContext();
        if (pluginContext != null) {
            // 停止服务器
            VertxService vertxService = pluginContext.getBean(VertxService.class);
            vertxService.stopServer();
        }
    }

    @Override
    protected ApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext() {
            @Override
            protected void prepareRefresh() {
                // 在刷新容器前注册主程序中的 Bean
                ConfigurableListableBeanFactory beanFactory = this.getBeanFactory();
                DeviceDataApi deviceDataApi = SpringUtil.getBean(DeviceDataApi.class);
                beanFactory.registerSingleton("deviceDataApi", deviceDataApi);

                super.prepareRefresh();
            }
        };

        pluginContext.setClassLoader(getWrapper().getPluginClassLoader());
        pluginContext.scan("cn.iocoder.yudao.module.iot.plugin.http.config");
        pluginContext.refresh();

        return pluginContext;
    }

}
