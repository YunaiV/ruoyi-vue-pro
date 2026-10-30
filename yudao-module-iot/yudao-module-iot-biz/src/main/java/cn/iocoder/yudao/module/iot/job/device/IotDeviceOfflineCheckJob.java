package cn.iocoder.yudao.module.iot.job.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.framework.iot.config.YudaoIotProperties;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT 设备离线检查 Job
 *
 * 检测逻辑：设备最后一条 {@link IotDeviceMessage} 消息超过一定时间，则认为设备离线
 * 1. 活跃型设备（HTTP、CoAP、UDP 直连设备、网关子设备）：超时即离线
 * 2. 连接型设备（MQTT、TCP 等长连接）：在线状态由连接决定，超时后还要所在网关的协议实例已停止或重启过，才离线
 *
 * @see <a href="https://help.aliyun.com/zh/iot/support/faq-about-device-status#98f7056b2957y">阿里云 IoT —— 设备离线分析</a>
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotDeviceOfflineCheckJob implements JobHandler {

    @Resource
    private YudaoIotProperties iotProperties;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotDeviceMessageService deviceMessageService;
    @Resource
    private IotProductService productService;

    @Override
    @TenantJob
    public String execute(String param) {
        // 1.1 获得在线设备列表
        List<IotDeviceDO> devices = deviceService.getDeviceListByState(IotDeviceStateEnum.ONLINE.getState());
        if (CollUtil.isEmpty(devices)) {
            return JsonUtils.toJsonString(Collections.emptyList());
        }
        // 1.2 获取超时的设备集合
        // DONE @AI：一次性，就不要抽方法了。（已内联到这里，去掉 getTimeout 方法）
        Duration timeout = Duration.ofNanos((long) (iotProperties.getKeepAliveTime().toNanos() * iotProperties.getKeepAliveFactor()));
        Map<Long, LocalDateTime> timeoutDevices = devicePropertyService.getDeviceReportTimeMapByReportTime(
                LocalDateTime.now().minus(timeout));

        // 2. 下线设备
        List<String[]> offlineDevices = CollUtil.newArrayList();
        Map<Long, IotProductDO> productCache = new HashMap<>(); // 本轮内缓存，避免重复读取
        Map<String, Long> startTimeCache = new HashMap<>();
        for (IotDeviceDO device : devices) {
            LocalDateTime reportTime = timeoutDevices.get(device.getId());
            if (reportTime == null) {
                continue;
            }
            // 连接型设备：连接还在（所在网关的协议实例没有停止或重启过），则不离线
            if (isConnectionBased(device, productCache) && !isConnectionStale(device, productCache, startTimeCache)) {
                continue;
            }
            offlineDevices.add(new String[]{device.getProductKey(), device.getDeviceName()});
            // 为什么不直接更新状态呢？因为通过 IotDeviceMessage 可以经过一系列的处理，例如说记录日志等等
            // 事件时间 = 最后活跃时间 + 超时时间：期间如果设备重新上线，上线时间更晚，这条离线不会生效
            deviceMessageService.sendDeviceMessage(IotDeviceMessage.buildStateOffline(), device, null, reportTime.plus(timeout));
        }
        return JsonUtils.toJsonString(offlineDevices);
    }

    /**
     * 是否为连接型设备
     *
     * 网关子设备、产品不存在、协议未知时，按活跃型处理
     */
    private boolean isConnectionBased(IotDeviceDO device, Map<Long, IotProductDO> productCache) {
        if (IotProductDeviceTypeEnum.isGatewaySub(device.getDeviceType())) {
            return false;
        }
        IotProtocolTypeEnum protocolType = getProtocolType(device, productCache);
        return protocolType != null && protocolType.isConnectionBased();
    }

    /**
     * 连接型设备的连接是否已失效：所在网关的协议实例已停止（存活标记不存在），或者在设备上线后重启过
     */
    private boolean isConnectionStale(IotDeviceDO device, Map<Long, IotProductDO> productCache,
                                      Map<String, Long> startTimeCache) {
        // 1. EMQX 的连接由 EMQX 维护，不使用网关存活标记
        if (getProtocolType(device, productCache) == IotProtocolTypeEnum.EMQX) {
            return false;
        }
        // DONE @AI：方法内注释；（已按 1、2、3 步骤补充）
        // 2. 获得设备所在网关协议实例的启动时间：同一轮内，相同 serverId 只读取一次
        Long startTime;
        try {
            String serverId = devicePropertyService.getDeviceServerId(device.getId());
            if (serverId == null) { // 没有记录所在的网关，无法确认连接还在
                return true;
            }
            if (startTimeCache.containsKey(serverId)) {
                startTime = startTimeCache.get(serverId);
            } else {
                startTime = devicePropertyService.getGatewayServerStartTime(serverId);
                startTimeCache.put(serverId, startTime);
            }
        } catch (Exception e) {
            // Redis 读取失败，不等于存活标记不存在：本轮跳过，不判离线
            log.warn("[isConnectionStale][设备({}) 读取网关存活标记失败，本轮跳过]", device.getId(), e);
            return false;
        }
        // 3. 存活标记不存在（网关已停止或宕机），或者设备上线早于启动时间（网关重启过、设备未重连），则连接已失效
        if (startTime == null || device.getOnlineTime() == null) {
            return true;
        }
        // 按秒比较：上线时间是秒级精度（见 IotDeviceServiceImpl#updateDeviceState），同一秒内上线的设备，视为启动后上线，不判离线
        return device.getOnlineTime().truncatedTo(ChronoUnit.SECONDS)
                .isBefore(LocalDateTimeUtil.of(startTime).truncatedTo(ChronoUnit.SECONDS));
    }

    private IotProtocolTypeEnum getProtocolType(IotDeviceDO device, Map<Long, IotProductDO> productCache) {
        IotProductDO product = productCache.computeIfAbsent(device.getProductId(), productService::getProductFromCache);
        return product != null ? IotProtocolTypeEnum.of(product.getProtocolType()) : null;
    }

}
