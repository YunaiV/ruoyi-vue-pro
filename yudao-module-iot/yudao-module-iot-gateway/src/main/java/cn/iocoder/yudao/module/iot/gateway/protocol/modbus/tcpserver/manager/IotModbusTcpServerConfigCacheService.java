package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusModeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT Modbus TCP Server 配置缓存：认证时按需加载，断连时清理，定时刷新已连接设备
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpServerConfigCacheService {

    private final IotDeviceCommonApi deviceApi;

    /**
     * 配置缓存：deviceId -> 配置
     */
    private final Map<Long, IotModbusDeviceConfigRespDTO> configCache = new ConcurrentHashMap<>();

    /**
     * 加载单个设备的配置（认证成功后调用）
     *
     * @param deviceId 设备 ID
     * @return 设备配置
     */
    public IotModbusDeviceConfigRespDTO loadDeviceConfig(Long deviceId) {
        try {
            // 1. 从远程 API 获取配置
            IotModbusDeviceConfigListReqDTO reqDTO = new IotModbusDeviceConfigListReqDTO()
                    .setStatus(CommonStatusEnum.ENABLE.getStatus())
                    .setMode(IotModbusModeEnum.POLLING.getMode())
                    .setProtocolType(IotProtocolTypeEnum.MODBUS_TCP_SERVER.getType())
                    .setDeviceIds(Collections.singleton(deviceId));
            CommonResult<List<IotModbusDeviceConfigRespDTO>> result = deviceApi.getModbusDeviceConfigList(reqDTO);
            result.checkError();
            IotModbusDeviceConfigRespDTO modbusConfig = CollUtil.getFirst(result.getData());
            if (modbusConfig == null) {
                log.warn("[loadDeviceConfig][远程获取配置失败，未找到数据, deviceId={}]", deviceId);
                return null;
            }

            // 2. 更新缓存并返回
            configCache.put(modbusConfig.getDeviceId(), modbusConfig);
            return modbusConfig;
        } catch (Exception e) {
            log.error("[loadDeviceConfig][从远程获取配置失败, deviceId={}]", deviceId, e);
            return null;
        }
    }

    /**
     * 刷新已连接设备的配置缓存
     * <p>
     * 定时调用，从远程 API 拉取最新配置，只更新已连接设备的缓存。
     *
     * @param connectedDeviceIds 当前已连接的设备 ID 集合
     * @return 已连接设备的最新配置列表
     */
    public List<IotModbusDeviceConfigRespDTO> refreshConnectedDeviceConfigList(Set<Long> connectedDeviceIds) {
        if (CollUtil.isEmpty(connectedDeviceIds)) {
            return Collections.emptyList();
        }
        try {
            // 1. 从远程获取已连接设备的配置
            CommonResult<List<IotModbusDeviceConfigRespDTO>> result = deviceApi.getModbusDeviceConfigList(
                    new IotModbusDeviceConfigListReqDTO().setStatus(CommonStatusEnum.ENABLE.getStatus())
                            .setMode(IotModbusModeEnum.POLLING.getMode())
                            .setProtocolType(IotProtocolTypeEnum.MODBUS_TCP_SERVER.getType())
                            .setDeviceIds(connectedDeviceIds));
            List<IotModbusDeviceConfigRespDTO> modbusConfigs = result.getCheckedData();

            // 2. 更新缓存并返回
            for (IotModbusDeviceConfigRespDTO config : modbusConfigs) {
                configCache.put(config.getDeviceId(), config);
            }
            return modbusConfigs;
        } catch (Exception e) {
            log.error("[refreshConnectedDeviceConfigList][刷新配置失败]", e);
            return null;
        }
    }

    /**
     * 获取设备配置
     */
    public IotModbusDeviceConfigRespDTO getConfig(Long deviceId) {
        IotModbusDeviceConfigRespDTO config = configCache.get(deviceId);
        if (config != null) {
            return config;
        }
        // 缓存未命中，从远程 API 获取
        return loadDeviceConfig(deviceId);
    }

    /**
     * 移除设备配置缓存（设备断连时调用）
     */
    public void removeConfig(Long deviceId) {
        configCache.remove(deviceId);
    }

}
