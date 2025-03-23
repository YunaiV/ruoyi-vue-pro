package cn.iocoder.yudao.module.iot.plugin;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

// TODO @芋艿：暂未实现
@Slf4j
public class MqttPlugin extends Plugin {

    private MqttServerExtension mqttServerExtension;

    public MqttPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("MQTT Plugin started.");
        mqttServerExtension = new MqttServerExtension();
        mqttServerExtension.startMqttServer();
    }

    @Override
    public void stop() {
        log.info("MQTT Plugin stopped.");
        if (mqttServerExtension != null) {
            mqttServerExtension.stopMqttServer().onComplete(ar -> {
                if (ar.succeeded()) {
                    log.info("Stopped MQTT Server successfully");
                } else {
                    log.error("Failed to stop MQTT Server: {}", ar.cause().getMessage());
                }
            });
        }
    }
}
