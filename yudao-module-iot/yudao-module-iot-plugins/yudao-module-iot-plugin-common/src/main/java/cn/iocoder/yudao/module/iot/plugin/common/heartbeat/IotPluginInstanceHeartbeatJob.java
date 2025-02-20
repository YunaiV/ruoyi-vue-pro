package cn.iocoder.yudao.module.iot.plugin.common.heartbeat;

import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotPluginInstanceHeartbeatReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.config.IotPluginCommonProperties;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamServer;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

/**
 * IoT 插件实例心跳 Job
 *
 * 用于定时发送心跳给服务端
 */
@RequiredArgsConstructor
@Slf4j
public class IotPluginInstanceHeartbeatJob {

    private final IotDeviceUpstreamApi deviceUpstreamApi;
    private final IotDeviceDownstreamServer deviceDownstreamServer;
    private final IotPluginCommonProperties commonProperties;

    public void init() {
        CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(buildPluginInstanceHeartbeatReqDTO(true));
        log.info("[init][上线结果：{})]", result);
    }

    public void stop() {
        CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(buildPluginInstanceHeartbeatReqDTO(false));
        log.info("[stop][下线结果：{})]", result);
    }

    @Scheduled(initialDelay = 3, fixedRate = 3, timeUnit = TimeUnit.MINUTES) // 3 分钟执行一次
    public void execute() {
        CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(buildPluginInstanceHeartbeatReqDTO(true));
        log.info("[execute][心跳结果：{})]", result);
    }

    private IotPluginInstanceHeartbeatReqDTO buildPluginInstanceHeartbeatReqDTO(Boolean online) {
        return new IotPluginInstanceHeartbeatReqDTO()
                .setPluginKey(commonProperties.getPluginKey()).setProcessId(IotPluginCommonUtils.getProcessId())
                .setHostIp(SystemUtil.getHostInfo().getAddress()).setDownstreamPort(deviceDownstreamServer.getPort())
                .setOnline(online);
    }

}
