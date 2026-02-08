package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT Modbus TCP Slave 配置缓存服务
 * <p>
 * 与 tcpmaster 的 {@code IotModbusTcpConfigCacheService} 不同：
 * - tcpmaster 启动时拉全量配置 → 主动建连，需要全量 diff 检测新增/删除设备
 * - tcpslave 设备主动连接 → 认证时按需加载配置，断连时清理，定时刷新已连接设备的配置
 * <p>
 * 配置生命周期：
 * 1. 认证时：{@link #loadDeviceConfig(Long)} 按 deviceId 从 API 加载配置到缓存
 * 2. 断连时：{@link #removeConfig(Long)} 从缓存中移除
 * 3. 定时刷新：{@link #refreshConnectedDeviceConfigList(Set)} 批量刷新已连接设备的配置
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
     * <p>
     * 从远程 API 获取全量配置，然后按 deviceId 匹配。
     * 如果远程获取失败，尝试从 Mock 数据中匹配。
     *
     * @param deviceId 设备 ID
     * @return 设备配置，未找到返回 null
     */
    public IotModbusDeviceConfigRespDTO loadDeviceConfig(Long deviceId) {
        try {
            // 1. 从远程 API 获取全量配置
            // TODO @AI：等待修复，不着急；
            CommonResult<List<IotModbusDeviceConfigRespDTO>> result = deviceApi.getEnabledModbusDeviceConfigs();
            if (result != null && result.isSuccess() && result.getData() != null) {
                for (IotModbusDeviceConfigRespDTO config : result.getData()) {
                    // 顺便更新缓存（其他已连接设备也受益）
                    configCache.put(config.getDeviceId(), config);
                    if (config.getDeviceId().equals(deviceId)) {
                        return config;
                    }
                }
            }
        } catch (Exception e) {
            log.error("[loadDeviceConfig][从远程获取配置失败, deviceId={}]", deviceId, e);
        }

        // 2. 远程未找到，尝试 Mock 数据
        // TODO @芋艿：测试完成后移除
        for (IotModbusDeviceConfigRespDTO mockConfig : buildMockConfigs()) {
            configCache.put(mockConfig.getDeviceId(), mockConfig);
            if (mockConfig.getDeviceId().equals(deviceId)) {
                return mockConfig;
            }
        }

        return configCache.get(deviceId);
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
            // 1. 从远程获取全量配置
            // TODO @AI：传递 ids 批量查询；需要分批啦；
            CommonResult<List<IotModbusDeviceConfigRespDTO>> result = deviceApi.getEnabledModbusDeviceConfigs();
            List<IotModbusDeviceConfigRespDTO> allConfigs;
            if (result != null && result.isSuccess() && result.getData() != null) {
                allConfigs = new ArrayList<>(result.getData());
            } else {
                log.warn("[refreshConnectedDeviceConfigList][获取 Modbus 配置失败: {}]", result);
                allConfigs = new ArrayList<>();
            }

            // 2. 追加 Mock 测试数据
            // TODO @芋艿：测试完成后移除
            allConfigs.addAll(buildMockConfigs());

            // 3. 只保留已连接设备的配置，更新缓存
            List<IotModbusDeviceConfigRespDTO> connectedConfigs = new ArrayList<>();
            for (IotModbusDeviceConfigRespDTO config : allConfigs) {
                if (connectedDeviceIds.contains(config.getDeviceId())) {
                    configCache.put(config.getDeviceId(), config);
                    connectedConfigs.add(config);
                }
            }
            return connectedConfigs;
        } catch (Exception e) {
            log.error("[refreshConnectedDeviceConfigList][刷新配置失败]", e);
            // 降级：返回缓存中已连接设备的配置
            List<IotModbusDeviceConfigRespDTO> fallback = new ArrayList<>();
            for (Long deviceId : connectedDeviceIds) {
                IotModbusDeviceConfigRespDTO config = configCache.get(deviceId);
                if (config != null) {
                    fallback.add(config);
                }
            }
            return fallback;
        }
    }

    // ==================== 缓存操作 ====================

    /**
     * 获取设备配置
     */
    public IotModbusDeviceConfigRespDTO getConfig(Long deviceId) {
        return configCache.get(deviceId);
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
        config.setFrameFormat("modbus_tcp");

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
