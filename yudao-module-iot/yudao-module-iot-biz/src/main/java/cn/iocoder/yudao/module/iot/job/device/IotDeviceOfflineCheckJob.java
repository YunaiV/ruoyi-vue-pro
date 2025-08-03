package cn.iocoder.yudao.module.iot.job.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceStateUpdateReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.control.IotDeviceUpstreamService;
import cn.iocoder.yudao.module.iot.service.device.data.IotDevicePropertyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * IoT 设备离线检查 Job
 *
 * 检测逻辑：设备最后一条 {@link cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage} 消息超过一定时间，则认为设备离线
 *
 * @author 芋道源码
 */
@Component
public class IotDeviceOfflineCheckJob implements JobHandler {

    /**
     * 设备离线超时时间
     *
     * TODO 芋艿：暂定 10 分钟，后续看看要不要基于设备或者全局有配置文件
     */
    public static final Duration OFFLINE_TIMEOUT = Duration.ofMinutes(10);

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotDeviceUpstreamService deviceUpstreamService;

    @Override
    @TenantJob
    public String execute(String param) {
        // 1.1 获得在线设备列表
        List<IotDeviceDO> devices = deviceService.getDeviceListByState(IotDeviceStateEnum.ONLINE.getState());
        if (CollUtil.isEmpty(devices)) {
            return JsonUtils.toJsonString(Collections.emptyList());
        }
        // 1.2 获取超时的 deviceKey 集合
        Set<String> timeoutDeviceKeys = devicePropertyService.getDeviceKeysByReportTime(
                LocalDateTime.now().minus(OFFLINE_TIMEOUT));

        // 2. 下线设备
        List<String> offlineDeviceKeys = CollUtil.newArrayList();
        for (IotDeviceDO device : devices) {
            if (!timeoutDeviceKeys.contains(device.getDeviceKey())) {
                continue;
            }
            offlineDeviceKeys.add(device.getDeviceKey());
            // 为什么不直接更新状态呢？因为通过 IotDeviceMessage 可以经过一系列的处理，例如说记录日志等等
            deviceUpstreamService.updateDeviceState(((IotDeviceStateUpdateReqDTO)
                    new IotDeviceStateUpdateReqDTO().setRequestId(IdUtil.fastSimpleUUID()).setReportTime(LocalDateTime.now())
                            .setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName()))
                    .setState((IotDeviceStateEnum.OFFLINE.getState())));
        }
        return JsonUtils.toJsonString(offlineDeviceKeys);
    }

}
