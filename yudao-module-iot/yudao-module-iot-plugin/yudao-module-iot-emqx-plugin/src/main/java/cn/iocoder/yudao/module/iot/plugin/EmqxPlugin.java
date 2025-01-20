package cn.iocoder.yudao.module.iot.plugin;

import cn.iocoder.yudao.module.iot.api.ServiceRegistry;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class EmqxPlugin extends Plugin {

    private ExecutorService executorService;
    @Resource
    private DeviceDataApi deviceDataApi;

    public EmqxPlugin(PluginWrapper wrapper) {
        super(wrapper);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() {
        log.info("EmqxPlugin.start()");

        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();
        }

        deviceDataApi = ServiceRegistry.getService(DeviceDataApi.class);
        if (deviceDataApi == null) {
            log.error("未能从 ServiceRegistry 获取 DeviceDataApi 实例，请确保主程序已正确注册！");
            return;
        }

    }

    @Override
    public void stop() {
        log.info("EmqxPlugin.stop()");
    }
}