package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

// TODO @AI：和 IotModbusTcpConfigCacheService 基本一致？！
/**
 * IoT Modbus TCP Slave 配置缓存服务
 * <p>
 * 负责：从 biz 拉取 Modbus 设备配置，缓存配置数据，并检测配置变更
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

    // TODO @AI：它的 diff 算法，是不是不用和 IotModbusTcpConfigCacheService 完全一致；更多是1）首次连接时，查找；2）断开连接，移除；3）定时轮询更新；
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
            // TODO @AI：需要过滤下，只查找连接的设备列表；并且只有主动轮询的，才会处理；方法名，应该是 List 结尾；
            CommonResult<List<IotModbusDeviceConfigRespDTO>> result = deviceApi.getEnabledModbusDeviceConfigs();
            if (result == null || !result.isSuccess() || result.getData() == null) {
                log.warn("[refreshConfig][获取 Modbus 配置失败: {}]", result);
                return new ArrayList<>(configCache.values());
            }
            List<IotModbusDeviceConfigRespDTO> configs = new ArrayList<>(result.getData());

            // 2. 追加 Mock 测试数据（一次性测试用途）
            // TODO @芋艿：测试完成后移除
            configs.addAll(buildMockConfigs());

            // 3. 更新缓存
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
     * 构建 Mock 测试配置数据（一次性测试用途）
     *
     * 设备：PRODUCT_KEY=4aymZgOTOOCrDKRT, DEVICE_NAME=small
     * 点位：temperature（FC03, 地址 0）、humidity（FC03, 地址 1）
     *
     * TODO @芋艿：测试完成后移除
     */
    private List<IotModbusDeviceConfigRespDTO> buildMockConfigs() {
        IotModbusDeviceConfigRespDTO config = new IotModbusDeviceConfigRespDTO();
        config.setDeviceId(1L);
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

    /**
     * 获取设备配置
     */
    public IotModbusDeviceConfigRespDTO getConfig(Long deviceId) {
        return configCache.get(deviceId);
    }

    // TODO @AI：这个逻辑，是不是非必须？
    /**
     * 通过 clientId + username + password 查找设备配置（认证用）
     * 暂通过遍历缓存实现，后续可优化为索引
     */
    public IotModbusDeviceConfigRespDTO findConfigByAuth(String clientId, String username, String password) {
        // TODO @芋艿：测试完成后移除 mock 逻辑，改为正式查找
        // Mock：通过 clientId（格式 productKey.deviceName）匹配缓存中的设备
        if (clientId != null && clientId.contains(".")) {
            String[] parts = clientId.split("\\.", 2);
            String productKey = parts[0];
            String deviceName = parts[1];
            for (IotModbusDeviceConfigRespDTO config : configCache.values()) {
                if (productKey.equals(config.getProductKey()) && deviceName.equals(config.getDeviceName())) {
                    return config;
                }
            }
        }
        return null;
    }

    /**
     * 清理已删除设备的资源
     */
    public void cleanupRemovedDevices(List<IotModbusDeviceConfigRespDTO> currentConfigs, Consumer<Long> cleanupAction) {
        Set<Long> currentDeviceIds = convertSet(currentConfigs, IotModbusDeviceConfigRespDTO::getDeviceId);
        Set<Long> removedDeviceIds = new HashSet<>(knownDeviceIds);
        removedDeviceIds.removeAll(currentDeviceIds);

        for (Long deviceId : removedDeviceIds) {
            log.info("[cleanupRemovedDevices][清理已删除设备: {}]", deviceId);
            configCache.remove(deviceId);
            cleanupAction.accept(deviceId);
        }

        knownDeviceIds.clear();
        knownDeviceIds.addAll(currentDeviceIds);
    }

}
