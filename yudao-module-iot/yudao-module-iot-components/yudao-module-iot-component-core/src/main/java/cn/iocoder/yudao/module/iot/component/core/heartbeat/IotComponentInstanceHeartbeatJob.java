package cn.iocoder.yudao.module.iot.component.core.heartbeat;

import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotPluginInstanceHeartbeatReqDTO;
import cn.iocoder.yudao.module.iot.component.core.config.IotComponentCommonProperties;
import cn.iocoder.yudao.module.iot.component.core.downstream.IotDeviceDownstreamServer;
import cn.iocoder.yudao.module.iot.component.core.heartbeat.IotComponentRegistry.IotComponentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

/**
 * IoT 组件实例心跳定时任务
 * <p>
 * 将组件的状态，定时上报给 server 服务器
 */
@RequiredArgsConstructor
@Slf4j
public class IotComponentInstanceHeartbeatJob {

    /**
     * 内嵌模式的端口值（固定为 0）
     */
    private static final Integer EMBEDDED_PORT = 0;

    private final IotDeviceUpstreamApi deviceUpstreamApi;
    private final IotDeviceDownstreamServer deviceDownstreamServer; // TODO @haohao：这个变量还需要哇？
    private final IotComponentCommonProperties commonProperties;
    private final IotComponentRegistry componentRegistry;

    /**
     * 初始化方法，由 Spring调 用：注册当前组件并发送上线心跳
     */
    public void init() {
        // 将当前组件注册到注册表
        String processId = getProcessId();
        String hostIp = SystemUtil.getHostInfo().getAddress();

        // 注册当前组件
        componentRegistry.registerComponent(
                commonProperties.getPluginKey(),
                hostIp,
                EMBEDDED_PORT,
                processId);

        // 发送所有组件的上线心跳
        for (IotComponentInfo component : componentRegistry.getAllComponents()) {
            try {
                CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(
                        buildPluginInstanceHeartbeatReqDTO(component, true));
                log.info("[init][组件({})上线结果：{})]", component.getPluginKey(), result);
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
        for (IotComponentInfo component : componentRegistry.getAllComponents()) {
            try {
                CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(
                        buildPluginInstanceHeartbeatReqDTO(component, false));
                log.info("[stop][组件({})下线结果：{})]", component.getPluginKey(), result);
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
        for (IotComponentInfo component : componentRegistry.getAllComponents()) {
            try {
                CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(
                        buildPluginInstanceHeartbeatReqDTO(component, true));
                log.info("[execute][组件({})心跳结果：{})]", component.getPluginKey(), result);
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
    private IotPluginInstanceHeartbeatReqDTO buildPluginInstanceHeartbeatReqDTO(IotComponentInfo component,
                                                                                Boolean online) {
        return new IotPluginInstanceHeartbeatReqDTO()
                .setPluginKey(component.getPluginKey()).setProcessId(component.getProcessId())
                .setHostIp(component.getHostIp()).setDownstreamPort(component.getDownstreamPort())
                .setOnline(online);
    }

    // TODO @haohao：要和 IotPluginCommonUtils 保持一致么？
    /**
     * 获取当前进程 ID
     *
     * @return 进程 ID
     */
    private String getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        // TODO @haohao：是不是 SystemUtil.getCurrentPID(); 直接获取 pid 哈？
        return name.split("@")[0];
    }
}
