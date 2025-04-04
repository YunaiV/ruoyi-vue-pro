package cn.iocoder.yudao.module.iot.net.component.core.heartbeat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotPluginInstanceHeartbeatReqDTO;
import cn.iocoder.yudao.module.iot.net.component.core.config.IotNetComponentCommonProperties;
import cn.iocoder.yudao.module.iot.net.component.core.heartbeat.IotNetComponentRegistry.IotNetComponentInfo;
import cn.iocoder.yudao.module.iot.net.component.core.util.IotNetComponentCommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网络组件实例心跳定时任务
 * <p>
 * 将组件的状态，定时上报给 server 服务器
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Slf4j
public class IotNetComponentInstanceHeartbeatJob {

    private final IotDeviceUpstreamApi deviceUpstreamApi;
    private final IotNetComponentCommonProperties commonProperties;
    private final IotNetComponentRegistry componentRegistry;

    /**
     * 初始化方法，由 Spring 调用：注册当前组件并发送上线心跳
     */
    public void init() {
        // 发送所有组件的上线心跳
        Collection<IotNetComponentInfo> components = componentRegistry.getAllComponents();
        if (CollUtil.isEmpty(components)) {
            return;
        }
        for (IotNetComponentInfo component : components) {
            try {
                CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(
                        buildPluginInstanceHeartbeatReqDTO(component, true));
                log.info("[init][组件({})上线结果：{}]", component.getPluginKey(), result);
            } catch (Exception e) {
                log.error("[init][组件({})上线发送异常]", component.getPluginKey(), e);
            }
        }
    }

    /**
     * 停止方法，由 Spring 调用：发送下线心跳并注销组件
     */
    public void stop() {
        // 发送所有组件的下线心跳
        Collection<IotNetComponentInfo> components = componentRegistry.getAllComponents();
        if (CollUtil.isEmpty(components)) {
            return;
        }
        for (IotNetComponentInfo component : components) {
            try {
                CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(
                        buildPluginInstanceHeartbeatReqDTO(component, false));
                log.info("[stop][组件({})下线结果：{}]", component.getPluginKey(), result);
            } catch (Exception e) {
                log.error("[stop][组件({})下线发送异常]", component.getPluginKey(), e);
            }
        }

        // 注销当前组件
        componentRegistry.unregisterComponent(commonProperties.getPluginKey());
    }

    /**
     * 定时发送心跳
     */
    @Scheduled(initialDelay = 1, fixedRate = 1, timeUnit = TimeUnit.MINUTES) // 1 分钟执行一次
    public void execute() {
        // 发送所有组件的心跳
        Collection<IotNetComponentInfo> components = componentRegistry.getAllComponents();
        if (CollUtil.isEmpty(components)) {
            return;
        }
        for (IotNetComponentInfo component : components) {
            try {
                CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(
                        buildPluginInstanceHeartbeatReqDTO(component, true));
                log.info("[execute][组件({})心跳结果：{}]", component.getPluginKey(), result);
            } catch (Exception e) {
                log.error("[execute][组件({})心跳发送异常]", component.getPluginKey(), e);
            }
        }
    }

    /**
     * 构建心跳 DTO
     *
     * @param component 组件信息
     * @param online    是否在线
     * @return 心跳 DTO
     */
    private IotPluginInstanceHeartbeatReqDTO buildPluginInstanceHeartbeatReqDTO(IotNetComponentInfo component,
                                                                                Boolean online) {
        return new IotPluginInstanceHeartbeatReqDTO()
                .setPluginKey(component.getPluginKey()).setProcessId(component.getProcessId())
                .setHostIp(component.getHostIp()).setDownstreamPort(component.getDownstreamPort())
                .setOnline(online);
    }
}