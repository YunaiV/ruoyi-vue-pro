package cn.iocoder.yudao.module.iot.component.core.heartbeat;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 组件注册表
 * <p>
 * 用于管理多个组件的注册信息，解决多组件心跳问题
 */
@Component
@Slf4j
public class IotComponentRegistry {

    /**
     * 组件信息
     */
    @Data
    @ToString
    public static class IotComponentInfo {
        /**
         * 组件Key
         */
        private final String pluginKey;
        /**
         * 主机IP
         */
        private final String hostIp;
        /**
         * 下游端口
         */
        private final Integer downstreamPort;
        /**
         * 进程ID
         */
        private final String processId;
    }

    /**
     * 组件映射表，key为组件Key
     */
    private final Map<String, IotComponentInfo> components = new ConcurrentHashMap<>();

    /**
     * 注册组件
     *
     * @param pluginKey      组件Key
     * @param hostIp         主机IP
     * @param downstreamPort 下游端口
     * @param processId      进程ID
     */
    public void registerComponent(String pluginKey, String hostIp, Integer downstreamPort, String processId) {
        log.info("[registerComponent][注册组件, pluginKey={}, hostIp={}, downstreamPort={}, processId={}]",
                pluginKey, hostIp, downstreamPort, processId);
        components.put(pluginKey, new IotComponentInfo(pluginKey, hostIp, downstreamPort, processId));
    }

    /**
     * 注销组件
     *
     * @param pluginKey 组件Key
     */
    public void unregisterComponent(String pluginKey) {
        log.info("[unregisterComponent][注销组件, pluginKey={}]", pluginKey);
        components.remove(pluginKey);
    }

    /**
     * 获取所有组件
     *
     * @return 所有组件集合
     */
    public Collection<IotComponentInfo> getAllComponents() {
        return components.values();
    }

    /**
     * 获取指定组件
     *
     * @param pluginKey 组件Key
     * @return 组件信息
     */
    public IotComponentInfo getComponent(String pluginKey) {
        return components.get(pluginKey);
    }
}