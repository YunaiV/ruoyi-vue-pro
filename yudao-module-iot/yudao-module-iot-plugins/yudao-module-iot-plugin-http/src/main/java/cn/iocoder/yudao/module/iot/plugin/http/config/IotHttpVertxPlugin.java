package cn.iocoder.yudao.module.iot.plugin.http.config;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// TODO @芋艿：完善注释
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
            ApplicationContext pluginContext = getApplicationContext();
            Assert.notNull(pluginContext, "pluginContext 不能为空");
            log.info("[HttpVertxPlugin][HttpVertxPlugin 插件启动成功...]");
        } catch (Exception e) {
            log.error("[HttpVertxPlugin][HttpVertxPlugin 插件开启动异常...]", e);
        }
    }

    @Override
    public void stop() {
        log.info("[HttpVertxPlugin][HttpVertxPlugin 插件停止开始...]");
        try {
            log.info("[HttpVertxPlugin][HttpVertxPlugin 插件停止成功...]");
        } catch (Exception e) {
            log.error("[HttpVertxPlugin][HttpVertxPlugin 插件停止异常...]", e);
        }
    }

    // TODO @芋艿：思考下，未来要不要。。。
    @Override
    protected ApplicationContext createApplicationContext() {
        // 创建插件自己的 ApplicationContext
        AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext();
        // 设置父容器为主应用的 ApplicationContext （确保主应用中提供的类可用）
        pluginContext.setParent(SpringUtil.getApplicationContext());
        // 继续使用插件自己的 ClassLoader 以加载插件内部的类
        pluginContext.setClassLoader(getWrapper().getPluginClassLoader());
        // 扫描当前插件的自动配置包
        // TODO @芋艿：后续看看，怎么配置类包
        pluginContext.scan("cn.iocoder.yudao.module.iot.plugin.http.config");
        pluginContext.refresh();
        return pluginContext;
    }

}