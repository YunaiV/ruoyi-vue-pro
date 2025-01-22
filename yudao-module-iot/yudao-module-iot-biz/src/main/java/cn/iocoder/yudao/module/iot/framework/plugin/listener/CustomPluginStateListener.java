package cn.iocoder.yudao.module.iot.framework.plugin.listener;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.springframework.stereotype.Component;

// TODO @芋艿：需要 review 下
@Component
@Slf4j
public class CustomPluginStateListener implements PluginStateListener {

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        // 1. 获取插件ID
        String pluginId = event.getPlugin().getPluginId();
        // 2. 获取插件旧状态
        String oldState = event.getOldState().toString();
        // 3. 获取插件新状态
        String newState = event.getPluginState().toString();
        // 4. 打印日志信息
        log.info("插件的状态 '{}' 已更改为 '{}' 至 '{}'", pluginId, oldState, newState);
    }

}