package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * IoT Modbus TCP 配置缓存服务，负责：从 biz 拉取 Modbus 设备配置，缓存配置数据，并检测配置变更
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpConfigCacheService {

    private final IotDeviceCommonApi deviceApi;

    /**
     * 配置缓存：deviceId -> 配置
     */
    private final Map<Long, IotModbusDeviceConfigRespDTO> configCache = new ConcurrentHashMap<>();

    /**
     * 已知的设备 ID 集合（作用：用于检测已删除的设备）
     *
     * @see #cleanupRemovedDevices(List, Consumer)
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

            // 2. 更新缓存（注意：不在这里更新 knownDeviceIds，由 cleanupRemovedDevices 统一管理）
            for (IotModbusDeviceConfigRespDTO config : configs) {
                configCache.put(config.getDeviceId(), config);
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
     * 清理已删除设备的资源，并更新已知设备 ID 集合
     *
     * @param currentConfigs 当前有效的配置列表
     * @param cleanupAction  清理动作
     */
    public void cleanupRemovedDevices(List<IotModbusDeviceConfigRespDTO> currentConfigs, Consumer<Long> cleanupAction) {
        // 1.1 获取当前有效的设备 ID
        Set<Long> currentDeviceIds = convertSet(currentConfigs, IotModbusDeviceConfigRespDTO::getDeviceId);
        // 1.2 找出已删除的设备（基于旧的 knownDeviceIds）
        Set<Long> removedDeviceIds = new HashSet<>(knownDeviceIds);
        removedDeviceIds.removeAll(currentDeviceIds);

        // 2. 清理已删除设备
        for (Long deviceId : removedDeviceIds) {
            log.info("[cleanupRemovedDevices][清理已删除设备: {}]", deviceId);
            configCache.remove(deviceId);
            cleanupAction.accept(deviceId);
        }

        // 3. 更新已知设备 ID 集合为当前有效的设备 ID
        knownDeviceIds.clear();
        knownDeviceIds.addAll(currentDeviceIds);
    }

}
