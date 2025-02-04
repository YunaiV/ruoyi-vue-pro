package cn.iocoder.yudao.module.iot.framework.plugin.core;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;

/**
 * IoT 插件状态监听器，用于 log 插件的状态变化
 *
 * @author haohao
 */
@Slf4j
public class IotPluginStateListener implements PluginStateListener {

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        log.info("[pluginStateChanged][插件({}) 状态变化，从 {} 变为 {}]", event.getPlugin().getPluginId(),
                event.getOldState().toString(), event.getPluginState().toString());
    }

}