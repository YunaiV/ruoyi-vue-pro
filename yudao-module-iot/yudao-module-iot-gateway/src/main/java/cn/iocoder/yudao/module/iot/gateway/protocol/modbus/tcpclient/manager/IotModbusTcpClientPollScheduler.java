package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.AbstractIotModbusPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusTcpClientUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.upstream.IotModbusTcpClientUpstreamHandler;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

/**
 * IoT Modbus TCP Client 轮询调度器：管理点位的轮询定时器，调度读取任务并上报结果
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpClientPollScheduler extends AbstractIotModbusPollScheduler {

    private final IotModbusTcpClientConnectionManager connectionManager;
    private final IotModbusTcpClientUpstreamHandler upstreamHandler;
    private final IotModbusTcpClientConfigCacheService configCacheService;

    public IotModbusTcpClientPollScheduler(Vertx vertx,
                                           IotModbusTcpClientConnectionManager connectionManager,
                                           IotModbusTcpClientUpstreamHandler upstreamHandler,
                                           IotModbusTcpClientConfigCacheService configCacheService) {
        super(vertx);
        this.connectionManager = connectionManager;
        this.upstreamHandler = upstreamHandler;
        this.configCacheService = configCacheService;
    }

    // ========== 轮询执行 ==========

    @Override
    protected List<PollTask> buildPollTasks(IotModbusDeviceConfigRespDTO config) {
        return convertList(buildReadSegments(config), segment -> new PollTask(segment.getKey(), segment.getPollInterval()));
    }

    /**
     * 轮询读取段
     */
    @Override
    protected void pollTask(Long deviceId, String taskKey) {
        // 1.1 从 configCache 获取最新配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(deviceId);
        if (config == null || CollUtil.isEmpty(config.getPoints())) {
            log.warn("[pollTask][设备 {} 没有配置]", deviceId);
            return;
        }
        // 1.2 查找读取段。配置变化后，如果当前 taskKey 已不存在，直接跳过等待下一轮 updatePolling 清理 timer
        ReadSegment segment = findReadSegment(config, taskKey);
        if (segment == null) {
            log.debug("[pollTask][设备 {} 读取段 {} 未找到，跳过陈旧轮询任务]", deviceId, taskKey);
            return;
        }

        // 2.1 获取连接
        IotModbusTcpClientConnectionManager.ModbusConnection connection = connectionManager.getConnection(deviceId);
        if (connection == null) {
            log.warn("[pollTask][设备 {} 没有连接]", deviceId);
            return;
        }
        // 2.2 获取 slave ID
        Integer slaveId = connectionManager.getSlaveId(deviceId);
        Assert.notNull(slaveId, "设备 {} 没有配置 slaveId", deviceId);

        // 3. 执行 Modbus 批量读取
        IotModbusTcpClientUtils.read(connection, slaveId, segment.getFunctionCode(),
                        segment.getStartAddress(), segment.getRegisterCount())
                .onSuccess(rawValues -> handleSegmentReadResult(config, segment, rawValues))
                .onFailure(e -> log.error("[pollTask][读取点位段失败, deviceId={}, segment={}]",
                        deviceId, segment.getKey(), e));
    }

    /**
     * 轮询单个点位
     */
    @Override
    protected void pollPoint(Long deviceId, Long pointId) {
        // 1.1 从 configCache 获取最新配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(deviceId);
        if (config == null || CollUtil.isEmpty(config.getPoints())) {
            log.warn("[pollPoint][设备 {} 没有配置]", deviceId);
            return;
        }
        // 1.2 查找点位
        IotModbusPointRespDTO point = IotModbusCommonUtils.findPointById(config, pointId);
        if (point == null) {
            log.warn("[pollPoint][设备 {} 点位 {} 未找到]", deviceId, pointId);
            return;
        }

        // 2.1 获取连接
        IotModbusTcpClientConnectionManager.ModbusConnection connection = connectionManager.getConnection(deviceId);
        if (connection == null) {
            log.warn("[pollPoint][设备 {} 没有连接]", deviceId);
            return;
        }
        // 2.2 获取 slave ID
        Integer slaveId = connectionManager.getSlaveId(deviceId);
        Assert.notNull(slaveId, "设备 {} 没有配置 slaveId", deviceId);

        // 3. 执行 Modbus 读取
        IotModbusTcpClientUtils.read(connection, slaveId, point)
                .onSuccess(rawValue -> upstreamHandler.handleReadResult(config, point, rawValue))
                .onFailure(e -> log.error("[pollPoint][读取点位失败, deviceId={}, identifier={}]",
                        deviceId, point.getIdentifier(), e));
    }

    private void handleSegmentReadResult(IotModbusDeviceConfigRespDTO config,
                                         ReadSegment segment,
                                         int[] rawValues) {
        for (IotModbusPointRespDTO point : segment.getPoints()) {
            // 批量读取返回的是整个连续地址段，需要按点位地址切片后再复用单点上报逻辑
            int[] pointRawValues = extractPointRawValues(rawValues, segment, point);
            if (pointRawValues == null) {
                log.warn("[handleSegmentReadResult][读取段结果长度不足, deviceId={}, segment={}, identifier={}]",
                        config.getDeviceId(), segment.getKey(), point.getIdentifier());
                continue;
            }
            upstreamHandler.handleReadResult(config, point, pointRawValues);
        }
    }

    private ReadSegment findReadSegment(IotModbusDeviceConfigRespDTO config, String taskKey) {
        return CollUtil.findOne(buildReadSegments(config), segment -> segment.getKey().equals(taskKey));
    }

    /**
     * 构建批量读取段
     *
     * <p>只合并功能码、轮询间隔相同，且地址连续或重叠的点位；不跨功能码、不跨轮询间隔，避免改变原有轮询语义。
     * 同时按 Modbus 协议限制控制单次读取长度，超过限制时拆成多个读取段。
     */
    static List<ReadSegment> buildReadSegments(IotModbusDeviceConfigRespDTO config) {
        if (config == null) {
            return Collections.emptyList();
        }

        // 1. 按功能码和轮询间隔分组：两者任一不同，都不能共用同一次 Modbus 读请求
        List<IotModbusPointRespDTO> validPoints = filterList(config.getPoints(), IotModbusTcpClientPollScheduler::isValidReadPoint);
        if (CollUtil.isEmpty(validPoints)) {
            return Collections.emptyList();
        }
        Map<SegmentGroupKey, List<IotModbusPointRespDTO>> pointsByGroup = convertMultiMap(validPoints,
                point -> new SegmentGroupKey(point.getFunctionCode(), point.getPollInterval()));

        // 2. 组内按地址排序后，合并连续或重叠区间，生成实际轮询的读取段
        List<ReadSegment> segments = new ArrayList<>();
        for (Map.Entry<SegmentGroupKey, List<IotModbusPointRespDTO>> entry : pointsByGroup.entrySet()) {
            List<IotModbusPointRespDTO> points = entry.getValue();
            points.sort(Comparator.comparing(IotModbusPointRespDTO::getRegisterAddress)
                    .thenComparing(IotModbusPointRespDTO::getRegisterCount)
                    .thenComparing(IotModbusPointRespDTO::getId));
            buildReadSegments(entry.getKey(), points, segments);
        }
        // 3. 固定排序，保证生成的 taskKey 稳定，便于 updatePolling 做增量更新
        segments.sort(Comparator.comparing(ReadSegment::getFunctionCode)
                .thenComparing(ReadSegment::getPollInterval)
                .thenComparing(ReadSegment::getStartAddress));
        return segments;
    }

    private static void buildReadSegments(SegmentGroupKey groupKey,
                                          List<IotModbusPointRespDTO> points,
                                          List<ReadSegment> segments) {
        ReadSegment current = null;
        int maxRegisterCount = getMaxRegisterCount(groupKey.getFunctionCode());
        // points 已按 registerAddress 排序，因此可以线性合并连续/重叠地址段
        for (IotModbusPointRespDTO point : points) {
            int pointStartAddress = point.getRegisterAddress();
            int pointEndAddress = pointStartAddress + point.getRegisterCount();
            // 1. 当前点位无法合并时，新建一个读取段
            if (current == null || !canMerge(current, pointStartAddress, pointEndAddress, maxRegisterCount)) {
                current = new ReadSegment(groupKey.getFunctionCode(), groupKey.getPollInterval(),
                        pointStartAddress, point.getRegisterCount(), new ArrayList<>());
                segments.add(current);
            } else {
                // 2. 当前点位可合并时，扩展读取段覆盖范围
                current.setRegisterCount(Math.max(current.getEndAddress(), pointEndAddress) - current.getStartAddress());
            }
            // 3. 记录读取段包含的点位，读取成功后按点位逐个切片上报
            current.getPoints().add(point);
        }
    }

    /**
     * 判断点位是否可以合并到当前读取段
     *
     * <p>仅合并连续或重叠区间，不合并存在地址空洞的区间，避免额外读取无关寄存器。
     */
    private static boolean canMerge(ReadSegment segment, int pointStartAddress, int pointEndAddress, int maxRegisterCount) {
        if (pointStartAddress > segment.getEndAddress()) {
            return false;
        }
        int mergedRegisterCount = Math.max(segment.getEndAddress(), pointEndAddress) - segment.getStartAddress();
        return mergedRegisterCount <= maxRegisterCount;
    }

    /**
     * 从批量读取结果中提取单个点位的原始值
     *
     * <p>例如读取段从地址 10 开始，点位地址为 12、数量为 2，则取 rawValues[2..4)。
     */
    static int[] extractPointRawValues(int[] rawValues, ReadSegment segment, IotModbusPointRespDTO point) {
        if (rawValues == null) {
            return null;
        }
        // 1. 计算点位在批量读取结果中的相对偏移
        int offset = point.getRegisterAddress() - segment.getStartAddress();
        int end = offset + point.getRegisterCount();
        // 2. 防御异常响应长度，避免越界影响同一读取段内其它点位
        if (offset < 0 || end > rawValues.length) {
            return null;
        }
        // 3. 返回单个点位需要的原始寄存器值
        return Arrays.copyOfRange(rawValues, offset, end);
    }

    private static boolean isValidReadPoint(IotModbusPointRespDTO point) {
        return point != null
                && point.getId() != null
                && point.getFunctionCode() != null
                && point.getRegisterAddress() != null
                && point.getRegisterCount() != null
                && point.getRegisterCount() > 0
                && point.getPollInterval() != null
                && point.getPollInterval() > 0;
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private static int getMaxRegisterCount(Integer functionCode) {
        switch (functionCode) {
            case IotModbusCommonUtils.FC_READ_COILS:
            case IotModbusCommonUtils.FC_READ_DISCRETE_INPUTS:
                return 2000;
            case IotModbusCommonUtils.FC_READ_HOLDING_REGISTERS:
            case IotModbusCommonUtils.FC_READ_INPUT_REGISTERS:
                return 125;
            default:
                return 0;
        }
    }

    /**
     * 读取段分组 Key
     */
    @Data
    @AllArgsConstructor
    static class SegmentGroupKey {

        private Integer functionCode;
        private Integer pollInterval;

    }

    /**
     * 一次 Modbus 批量读请求对应的连续地址段
     */
    @Data
    @AllArgsConstructor
    static class ReadSegment {

        private Integer functionCode;
        private Integer pollInterval;
        private Integer startAddress;
        private Integer registerCount;
        private List<IotModbusPointRespDTO> points;

        String getKey() {
            return functionCode + ":" + pollInterval + ":" + startAddress + ":" + registerCount;
        }

        int getEndAddress() {
            return startAddress + registerCount;
        }

    }

}
