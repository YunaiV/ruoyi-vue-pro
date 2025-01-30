package cn.iocoder.yudao.module.iot.plugin.http.config;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.http.upstream.IotDeviceUpstreamServer;
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
public class IotHttpVertxPlugin extends SpringPlugin {

    public IotHttpVertxPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("[HttpVertxPlugin][HttpVertxPlugin 插件启动开始...]");
        try {
            // 1. 获取插件上下文
            ApplicationContext pluginContext = getApplicationContext();
            Assert.notNull(pluginContext, "pluginContext 不能为空");

            // 2. 启动 Vert.x
            IotDeviceUpstreamServer vertxService = pluginContext.getBean(IotDeviceUpstreamServer.class);
            vertxService.start();

            log.info("[HttpVertxPlugin][HttpVertxPlugin 插件启动成功...]");
        } catch (Exception e) {
            log.error("[HttpVertxPlugin][HttpVertxPlugin 插件开启动异常...]", e);
        }
    }

    @Override
    public void stop() {
        log.info("[HttpVertxPlugin][HttpVertxPlugin 插件停止开始...]");
        try {
            // 停止服务器
            ApplicationContext pluginContext = getApplicationContext();
            if (pluginContext != null) {
                IotDeviceUpstreamServer vertxService = pluginContext.getBean(IotDeviceUpstreamServer.class);
                vertxService.stopServer();
            }

            log.info("[HttpVertxPlugin][HttpVertxPlugin 插件停止成功...]");
        } catch (Exception e) {
            log.error("[HttpVertxPlugin][HttpVertxPlugin 插件停止异常...]", e);
        }
    }

    @Override
    protected ApplicationContext createApplicationContext() {
        // TODO @haohao：这个加 deviceDataApi 的目的是啥呀？
        AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext() {

            @Override
            protected void prepareRefresh() {
                // 在刷新容器前注册主程序中的 Bean
                ConfigurableListableBeanFactory beanFactory = this.getBeanFactory();
                IotDeviceUpstreamApi deviceDataApi = SpringUtil.getBean(IotDeviceUpstreamApi.class);
                beanFactory.registerSingleton("deviceDataApi", deviceDataApi);
                super.prepareRefresh();
            }

        };

        pluginContext.setClassLoader(getWrapper().getPluginClassLoader());
        // TODO @芋艿：枚举
        pluginContext.scan("cn.iocoder.yudao.module.iot.plugin.http.config");
        pluginContext.refresh();
        return pluginContext;
    }

}