package cn.iocoder.yudao.module.iot.net.component.core.heartbeat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 网络组件注册表
 * <p>
 * 用于管理多个网络组件的注册信息，解决多组件心跳问题
 *
 * @author haohao
 */
@Component
@Slf4j
public class IotNetComponentRegistry {

    /**
     * 网络组件信息
     */
    @Data
    public static class IotNetComponentInfo {

        /**
         * 组件 Key
         */
        private final String pluginKey;

        /**
         * 主机 IP
         */
        private final String hostIp;

        /**
         * 下游端口
         */
        private final Integer downstreamPort;

        /**
         * 进程 ID
         */
        private final String processId;
    }

    /**
     * 组件映射表：key 为组件 Key
     */
    private final Map<String, IotNetComponentInfo> components = new ConcurrentHashMap<>();

    /**
     * 注册网络组件
     *
     * @param pluginKey      组件 Key
     * @param hostIp         主机 IP
     * @param downstreamPort 下游端口
     * @param processId      进程 ID
     */
    public void registerComponent(String pluginKey, String hostIp, Integer downstreamPort, String processId) {
        log.info("[registerComponent][注册网络组件, pluginKey={}, hostIp={}, downstreamPort={}, processId={}]",
                pluginKey, hostIp, downstreamPort, processId);
        components.put(pluginKey, new IotNetComponentInfo(pluginKey, hostIp, downstreamPort, processId));
    }

    /**
     * 注销网络组件
     *
     * @param pluginKey 组件 Key
     */
    public void unregisterComponent(String pluginKey) {
        log.info("[unregisterComponent][注销网络组件, pluginKey={}]", pluginKey);
        components.remove(pluginKey);
    }

    /**
     * 获取所有网络组件
     *
     * @return 所有组件集合
     */
    public Collection<IotNetComponentInfo> getAllComponents() {
        return CollUtil.isEmpty(components) ? CollUtil.newArrayList() : components.values();
    }

    /**
     * 获取指定网络组件
     *
     * @param pluginKey 组件 Key
     * @return 组件信息
     */
    public IotNetComponentInfo getComponent(String pluginKey) {
        return MapUtil.isEmpty(components) ? null : components.get(pluginKey);
    }
}