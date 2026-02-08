package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * IoT Modbus TCP Slave 待响应请求管理器
 * <p>
 * 管理轮询下发的请求，用于匹配设备响应：
 * - TCP 模式：按 transactionId 精确匹配
 * - RTU 模式：按 slaveId + functionCode FIFO 匹配
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpSlavePendingRequestManager {

    /**
     * deviceId → 有序队列
     */
    private final Map<Long, Deque<PendingRequest>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * 待响应请求信息
     */
    @Data
    @AllArgsConstructor
    public static class PendingRequest {

        private Long deviceId;
        private Long pointId;
        private String identifier;
        private int slaveId;
        private int functionCode;
        private int registerAddress;
        private int registerCount;
        private Integer transactionId;
        private long expireAt;

    }

    /**
     * 添加待响应请求
     */
    public void addRequest(PendingRequest request) {
        pendingRequests.computeIfAbsent(request.getDeviceId(), k -> new ConcurrentLinkedDeque<>())
                .addLast(request);
    }

    /**
     * 匹配响应（TCP 模式按 transactionId，RTU 模式按 FIFO）
     *
     * @param deviceId    设备 ID
     * @param frame       收到的响应帧
     * @param frameFormat 帧格式
     * @return 匹配到的 PendingRequest，没有匹配返回 null
     */
    public PendingRequest matchResponse(Long deviceId, IotModbusFrame frame,
                                        IotModbusFrameFormatEnum frameFormat) {
        Deque<PendingRequest> queue = pendingRequests.get(deviceId);
        if (CollUtil.isEmpty(queue)) {
            return null;
        }

        // TCP 模式：按 transactionId 精确匹配
        if (frameFormat == IotModbusFrameFormatEnum.MODBUS_TCP && frame.getTransactionId() != null) {
            return matchByTransactionId(queue, frame.getTransactionId());
        }
        // RTU 模式：FIFO，匹配 slaveId + functionCode + registerCount
        int responseRegisterCount = extractRegisterCountFromResponse(frame);
        return matchByFifo(queue, frame.getSlaveId(), frame.getFunctionCode(), responseRegisterCount);
    }

    /**
     * 按 transactionId 匹配
     */
    private PendingRequest matchByTransactionId(Deque<PendingRequest> queue, int transactionId) {
        Iterator<PendingRequest> it = queue.iterator();
        while (it.hasNext()) {
            PendingRequest req = it.next();
            if (req.getTransactionId() != null && req.getTransactionId() == transactionId) {
                it.remove();
                return req;
            }
        }
        return null;
    }

    /**
     * 按 FIFO 匹配（slaveId + functionCode + registerCount）
     */
    private PendingRequest matchByFifo(Deque<PendingRequest> queue, int slaveId, int functionCode,
                                        int responseRegisterCount) {
        Iterator<PendingRequest> it = queue.iterator();
        while (it.hasNext()) {
            PendingRequest req = it.next();
            if (req.getSlaveId() == slaveId
                    && req.getFunctionCode() == functionCode
                    && (responseRegisterCount <= 0 || req.getRegisterCount() == responseRegisterCount)) {
                it.remove();
                return req;
            }
        }
        return null;
    }

    // TODO @AI：是不是放到 modbus 工具类里，更合适？
    /**
     * 从响应帧中提取 registerCount（通过 PDU 的 byteCount 推断）
     *
     * @return registerCount，无法提取时返回 -1（匹配时跳过校验）
     */
    private int extractRegisterCountFromResponse(IotModbusFrame frame) {
        byte[] pdu = frame.getPdu();
        if (pdu == null || pdu.length < 1) {
            return -1;
        }
        int byteCount = pdu[0] & 0xFF;
        int fc = frame.getFunctionCode();
        // FC03/04 寄存器读响应：registerCount = byteCount / 2
        if (fc == IotModbusCommonUtils.FC_READ_HOLDING_REGISTERS
                || fc == IotModbusCommonUtils.FC_READ_INPUT_REGISTERS) {
            return byteCount / 2;
        }
        // FC01/02 线圈/离散输入读响应：registerCount = byteCount * 8（线圈数量）
        // 但因为按 bit 打包有余位，无法精确反推，返回 -1 跳过校验
        return -1;
    }

    /**
     * 清理过期请求
     */
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        for (Map.Entry<Long, Deque<PendingRequest>> entry : pendingRequests.entrySet()) {
            Deque<PendingRequest> queue = entry.getValue();
            int removed = 0;
            Iterator<PendingRequest> it = queue.iterator();
            while (it.hasNext()) {
                PendingRequest req = it.next();
                if (req.getExpireAt() < now) {
                    it.remove();
                    removed++;
                }
            }
            if (removed > 0) {
                log.debug("[cleanupExpired][设备 {} 清理了 {} 个过期请求]", entry.getKey(), removed);
            }
        }
    }

    /**
     * 清理指定设备的所有待响应请求
     */
    public void removeDevice(Long deviceId) {
        pendingRequests.remove(deviceId);
    }

    /**
     * 清理所有待响应请求
     */
    public void clear() {
        pendingRequests.clear();
    }

}
