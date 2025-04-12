package cn.iocoder.yudao.module.iot.net.component.server.heartbeat;

import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotPluginInstanceHeartbeatReqDTO;
import cn.iocoder.yudao.module.iot.net.component.server.config.IotNetComponentServerProperties;
import cn.iocoder.yudao.module.iot.net.component.server.downstream.IotComponentDownstreamServer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// TODO @haohao：有办法服用 yudao-module-iot-net-component-core 的么？就是 server，只是一个启动器，没什么特殊的功能；
/**
 * IoT 组件心跳任务
 * <p>
 * 定期向主程序发送心跳，报告组件服务状态
 *
 * @author haohao
 */
@Slf4j
public class IotComponentHeartbeatJob {

    private final IotDeviceUpstreamApi deviceUpstreamApi;
    private final IotComponentDownstreamServer downstreamServer;
    private final IotNetComponentServerProperties properties;
    private ScheduledExecutorService executorService;

    public IotComponentHeartbeatJob(IotDeviceUpstreamApi deviceUpstreamApi,
            IotComponentDownstreamServer downstreamServer,
            IotNetComponentServerProperties properties) {
        this.deviceUpstreamApi = deviceUpstreamApi;
        this.downstreamServer = downstreamServer;
        this.properties = properties;
    }

    /**
     * 初始化心跳任务
     */
    public void init() {
        log.info("[init][开始初始化心跳任务]");
        // 创建一个单线程的调度线程池
        executorService = new ScheduledThreadPoolExecutor(1);
        // 延迟 5 秒后开始执行，避免服务刚启动就发送心跳
        executorService.scheduleAtFixedRate(this::sendHeartbeat,
                5000, properties.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
        log.info("[init][心跳任务初始化完成]");
    }

    /**
     * 停止心跳任务
     */
    public void stop() {
        log.info("[stop][开始停止心跳任务]");
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        log.info("[stop][心跳任务已停止]");
    }

    /**
     * 发送心跳
     */
    private void sendHeartbeat() {
        try {
            // 创建心跳请求
            IotPluginInstanceHeartbeatReqDTO heartbeatReqDTO = new IotPluginInstanceHeartbeatReqDTO();
            // 设置插件标识
            heartbeatReqDTO.setPluginKey(properties.getServerKey());
            // 设置进程ID
            heartbeatReqDTO.setProcessId(String.valueOf(ProcessHandle.current().pid()));
            // 设置IP和端口
            try {
                String hostIp = SystemUtil.getHostInfo().getAddress();
                heartbeatReqDTO.setHostIp(hostIp);
                heartbeatReqDTO.setDownstreamPort(downstreamServer.getPort());
            } catch (Exception e) {
                log.warn("[sendHeartbeat][获取本地主机信息异常]", e);
            }
            // 设置在线状态
            heartbeatReqDTO.setOnline(true);

            // 发送心跳
            CommonResult<Boolean> result = deviceUpstreamApi.heartbeatPluginInstance(heartbeatReqDTO);
            if (result != null && result.isSuccess()) {
                log.debug("[sendHeartbeat][发送心跳成功：{}]", heartbeatReqDTO);
            } else {
                log.error("[sendHeartbeat][发送心跳失败：{}, 结果：{}]", heartbeatReqDTO, result);
            }
        } catch (Exception e) {
            log.error("[sendHeartbeat][发送心跳异常]", e);
        }
    }

}