package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusModeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * IoT Modbus TCP Client 配置缓存服务
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpClientConfigCacheService {

    private final IotDeviceCommonApi deviceApi;

    /**
     * 配置缓存：deviceId -> 配置
     */
    private final Map<Long, IotModbusDeviceConfigRespDTO> configCache = new ConcurrentHashMap<>();

    /**
     * 已知的设备 ID 集合（作用：用于检测已删除的设备）
     *
     * @see #cleanupRemovedDevices(List)
     */
    private final Set<Long> knownDeviceIds = ConcurrentHashMap.newKeySet();

    /**
     * 刷新配置
     *
     * @return 最新的配置列表；API 失败时返回 null（调用方应跳过 cleanup）
     */
    public List<IotModbusDeviceConfigRespDTO> refreshConfig() {
        try {
            // 1. 从远程获取配置
            CommonResult<List<IotModbusDeviceConfigRespDTO>> result = deviceApi.getModbusDeviceConfigList(
                    new IotModbusDeviceConfigListReqDTO().setStatus(CommonStatusEnum.ENABLE.getStatus())
                            .setMode(IotModbusModeEnum.POLLING.getMode()).setProtocolType(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType()));
            result.checkError();
            List<IotModbusDeviceConfigRespDTO> configs = result.getData();

            // 2. 更新缓存（注意：不在这里更新 knownDeviceIds，由 cleanupRemovedDevices 统一管理）
            for (IotModbusDeviceConfigRespDTO config : configs) {
                configCache.put(config.getDeviceId(), config);
            }
            return configs;
        } catch (Exception e) {
            log.error("[refreshConfig][刷新配置失败]", e);
            return null;
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
     * 计算已删除设备的 ID 集合，清理缓存，并更新已知设备 ID 集合
     *
     * @param currentConfigs 当前有效的配置列表
     * @return 已删除的设备 ID 集合
     */
    public Set<Long> cleanupRemovedDevices(List<IotModbusDeviceConfigRespDTO> currentConfigs) {
        // 1.1 获取当前有效的设备 ID
        Set<Long> currentDeviceIds = convertSet(currentConfigs, IotModbusDeviceConfigRespDTO::getDeviceId);
        // 1.2 找出已删除的设备（基于旧的 knownDeviceIds）
        Set<Long> removedDeviceIds = new HashSet<>(knownDeviceIds);
        removedDeviceIds.removeAll(currentDeviceIds);

        // 2. 清理已删除设备的缓存
        for (Long deviceId : removedDeviceIds) {
            log.info("[cleanupRemovedDevices][清理已删除设备: {}]", deviceId);
            configCache.remove(deviceId);
        }

        // 3. 更新已知设备 ID 集合为当前有效的设备 ID
        knownDeviceIds.clear();
        knownDeviceIds.addAll(currentDeviceIds);
        return removedDeviceIds;
    }

}
