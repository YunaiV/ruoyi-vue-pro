package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusModeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT Modbus TCP Slave 配置缓存：认证时按需加载，断连时清理，定时刷新已连接设备
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpSlaveConfigCacheService {

    private final IotDeviceCommonApi deviceApi;

    /**
     * 配置缓存：deviceId -> 配置
     */
    private final Map<Long, IotModbusDeviceConfigRespDTO> configCache = new ConcurrentHashMap<>();

    // ==================== 按需加载（认证时） ====================

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
                    .setProtocolType(IotProtocolTypeEnum.MODBUS_TCP_SLAVE.getType())
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

    // ==================== 定时刷新（已连接设备） ====================

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
                            .setProtocolType(IotProtocolTypeEnum.MODBUS_TCP_SLAVE.getType())
                            .setDeviceIds(connectedDeviceIds));
            List<IotModbusDeviceConfigRespDTO> modbusConfigs = result.getCheckedData();

            // 2. 追加 Mock 测试数据（仅 mockEnabled=true 时）
            // TODO @芋艿：测试完成后移除
            // TODO @claude-code：【严重】同上，if(true) 导致 mockEnabled 开关失效，Mock 数据永远加载
            if (true) {
                modbusConfigs.addAll(buildMockConfigs());
            }

            // 2. 只保留已连接设备的配置，更新缓存
            // TODO @AI：是不是直接添加到 configCache 缓存（或者覆盖），然后返回 modbusConfigs 就 ok 了？！
            List<IotModbusDeviceConfigRespDTO> connectedConfigs = new ArrayList<>();
            for (IotModbusDeviceConfigRespDTO config : modbusConfigs) {
                if (connectedDeviceIds.contains(config.getDeviceId())) {
                    configCache.put(config.getDeviceId(), config);
                    connectedConfigs.add(config);
                }
            }
            return connectedConfigs;
        } catch (Exception e) {
            log.error("[refreshConnectedDeviceConfigList][刷新配置失败]", e);
            return null;
        }
    }

    // ==================== 缓存操作 ====================

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

    // ==================== Mock 数据 ====================

    /**
     * 构建 Mock 测试配置数据（一次性测试用途）
     *
     * 设备：PRODUCT_KEY=4aymZgOTOOCrDKRT, DEVICE_NAME=small
     * 点位：temperature（FC03, 地址 0）、humidity（FC03, 地址 1）
     *
     * TODO @芋艿：测试完成后移除
     */
    private List<IotModbusDeviceConfigRespDTO> buildMockConfigs() {
        IotModbusDeviceConfigRespDTO config = new IotModbusDeviceConfigRespDTO();
        config.setDeviceId(25L);
        config.setProductKey("4aymZgOTOOCrDKRT");
        config.setDeviceName("small");
        config.setSlaveId(1);
        config.setMode(1); // 云端轮询
        config.setFrameFormat(IotModbusFrameFormatEnum.MODBUS_TCP.getFormat());

        // 点位列表
        List<IotModbusPointRespDTO> points = new ArrayList<>();

        // 点位 1：温度 - 保持寄存器 FC03, 地址 0, 1 个寄存器, INT16
        IotModbusPointRespDTO point1 = new IotModbusPointRespDTO();
        point1.setId(1L);
        point1.setIdentifier("temperature");
        point1.setName("温度");
        point1.setFunctionCode(3); // FC03 读保持寄存器
        point1.setRegisterAddress(0);
        point1.setRegisterCount(1);
        point1.setRawDataType("INT16");
        point1.setByteOrder("BIG_ENDIAN");
        point1.setScale(new BigDecimal("0.1"));
        point1.setPollInterval(5000); // 5 秒轮询一次
        points.add(point1);

        // 点位 2：湿度 - 保持寄存器 FC03, 地址 1, 1 个寄存器, UINT16
        IotModbusPointRespDTO point2 = new IotModbusPointRespDTO();
        point2.setId(2L);
        point2.setIdentifier("humidity");
        point2.setName("湿度");
        point2.setFunctionCode(3); // FC03 读保持寄存器
        point2.setRegisterAddress(1);
        point2.setRegisterCount(1);
        point2.setRawDataType("UINT16");
        point2.setByteOrder("BIG_ENDIAN");
        point2.setScale(new BigDecimal("0.1"));
        point2.setPollInterval(5000); // 5 秒轮询一次
        points.add(point2);

        config.setPoints(points);
        log.info("[buildMockConfigs][已加载 Mock 配置, deviceId={}, points={}]", config.getDeviceId(), points.size());
        return Collections.singletonList(config);
    }

}
