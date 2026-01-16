package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

// TODO @AI：是不是 1、2、3 注释可以合并下；
/**
 * IoT Modbus TCP 配置缓存服务
 *
 * 负责：
 * 1. 从 biz 拉取 Modbus 设备配置
 * 2. 缓存配置数据
 * 3. 检测配置变更
 *
 * @author 芋道源码
 */
// TODO @AI：希望它的初始化，在 configuration 里；
@Service
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpConfigCacheService {

    private final IotDeviceCommonApi deviceApi;

    /**
     * 配置缓存：deviceId -> 配置
     */
    private final Map<Long, IotModbusDeviceConfigRespDTO> configCache = new ConcurrentHashMap<>();

    /**
     * 已知的设备 ID 集合
     */
    private final Set<Long> knownDeviceIds = ConcurrentHashMap.newKeySet();

    /**
     * 刷新配置
     *
     * @return 最新的配置列表
     */
    public List<IotModbusDeviceConfigRespDTO> refreshConfig() {
        try {
            // 1. 从远程获取配置
            CommonResult<List<IotModbusDeviceConfigRespDTO>> result = deviceApi.getEnabledModbusDeviceConfigs();
            if (result == null || !result.isSuccess() || result.getData() == null) {
                log.warn("[refreshConfig][获取 Modbus 配置失败: {}]", result);
                return new ArrayList<>(configCache.values());
            }
            List<IotModbusDeviceConfigRespDTO> configs = result.getData();

            // 2. 更新缓存
            for (IotModbusDeviceConfigRespDTO config : configs) {
                configCache.put(config.getDeviceId(), config);
                knownDeviceIds.add(config.getDeviceId());
            }
            return configs;
        } catch (Exception e) {
            log.error("[refreshConfig][刷新配置失败]", e);
            return new ArrayList<>(configCache.values());
        }
    }

    /**
     * 获取设备配置
     *
     * @param deviceId 设备 ID
     * @return 配置
     */
    public IotModbusDeviceConfigRespDTO getConfig(Long deviceId) {
        return configCache.get(deviceId);
    }

    /**
     * 清理已删除设备的资源
     *
     * @param currentConfigs 当前有效的配置列表
     * @param cleanupAction  清理动作
     */
    public void cleanupRemovedDevices(List<IotModbusDeviceConfigRespDTO> currentConfigs, Consumer<Long> cleanupAction) {
        // 1.1 获取当前有效的设备 ID
        // TODO @AI：convertSet 简化；
        Set<Long> currentDeviceIds = new HashSet<>();
        for (IotModbusDeviceConfigRespDTO config : currentConfigs) {
            currentDeviceIds.add(config.getDeviceId());
        }
        // 1.2 找出已删除的设备
        Set<Long> removedDeviceIds = new HashSet<>(knownDeviceIds);
        removedDeviceIds.removeAll(currentDeviceIds);

        // 2. 清理已删除设备
        for (Long deviceId : removedDeviceIds) {
            log.info("[cleanupRemovedDevices][清理已删除设备: {}]", deviceId);
            configCache.remove(deviceId);
            knownDeviceIds.remove(deviceId);
            cleanupAction.accept(deviceId);
        }
    }

}
