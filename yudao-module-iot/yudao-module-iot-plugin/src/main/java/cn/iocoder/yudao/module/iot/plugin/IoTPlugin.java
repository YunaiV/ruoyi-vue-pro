package cn.iocoder.yudao.module.iot.plugin;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IoTPlugin extends SpringPlugin {
    public IoTPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("IoTPlugin 启动");
    }

    @Override
    public void stop() {
        System.out.println("IoTPlugin 停止");
        super.stop(); // to close applicationContext
    }

    @Override
    protected ApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setClassLoader(getWrapper().getPluginClassLoader());
        applicationContext.register(IoTHttpPluginController.class); // 注册 IoTPluginConfig
        applicationContext.refresh();
        System.out.println("IoTPlugin 加载完成");
        return applicationContext;
    }
}
