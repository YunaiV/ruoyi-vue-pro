package cn.iocoder.yudao.module.iot.plugin;

import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.api.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.pf4j.Plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MqttPlugin extends Plugin {

    private ExecutorService executorService;
    private DeviceDataApi deviceDataApi;

    public MqttPlugin(PluginWrapper wrapper) {
        super(wrapper);
        // 初始化线程池
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() {
        log.info("MqttPlugin.start()");

        // 重新初始化线程池，确保它是活跃的
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();
        }

        // 从 ServiceRegistry 中获取主程序暴露的 DeviceDataApi 接口实例
        deviceDataApi = ServiceRegistry.getService(DeviceDataApi.class);
        if (deviceDataApi == null) {
            log.error("未能从 ServiceRegistry 获取 DeviceDataApi 实例，请确保主程序已正确注册！");
            return;
        }
    }

    @Override
    public void stop() {
        log.info("MqttPlugin.stop()");
        // 停止线程池
        executorService.shutdownNow();
    }

}