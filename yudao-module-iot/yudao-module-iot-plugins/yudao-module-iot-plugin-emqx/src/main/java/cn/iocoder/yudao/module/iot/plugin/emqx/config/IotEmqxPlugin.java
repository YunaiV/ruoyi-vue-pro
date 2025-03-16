package cn.iocoder.yudao.module.iot.plugin.emqx.config;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * EMQX 插件实现类
 *
 * 基于 PF4J 插件框架，实现 EMQX 消息中间件的集成：负责插件的生命周期管理，包括启动、停止和应用上下文的创建
 *
 * @author haohao
 */
@Slf4j
public class IotEmqxPlugin extends SpringPlugin {

    public IotEmqxPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("[EmqxPlugin][EmqxPlugin 插件启动开始...]");
        try {
            log.info("[EmqxPlugin][EmqxPlugin 插件启动成功...]");
        } catch (Exception e) {
            log.error("[EmqxPlugin][EmqxPlugin 插件开启动异常...]", e);
        }
    }

    @Override
    public void stop() {
        log.info("[EmqxPlugin][EmqxPlugin 插件停止开始...]");
        try {
            log.info("[EmqxPlugin][EmqxPlugin 插件停止成功...]");
        } catch (Exception e) {
            log.error("[EmqxPlugin][EmqxPlugin 插件停止异常...]", e);
        }
    }

    @Override
    protected ApplicationContext createApplicationContext() {
        // 创建插件自己的 ApplicationContext
        AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext();
        // 设置父容器为主应用的 ApplicationContext （确保主应用中提供的类可用）
        pluginContext.setParent(SpringUtil.getApplicationContext());
        // 继续使用插件自己的 ClassLoader 以加载插件内部的类
        pluginContext.setClassLoader(getWrapper().getPluginClassLoader());
        // 扫描当前插件的自动配置包
        // TODO @芋艿：是不是要配置下包
        pluginContext.scan("cn.iocoder.yudao.module.iot.plugin.emqx.config");
        pluginContext.refresh();
        return pluginContext;
    }

}