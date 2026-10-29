package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * IoT Modbus TCP Server 待响应请求管理器
 * <p>
 * 管理轮询、透传下发的请求，用于匹配设备响应：
 * - TCP 模式：按 transactionId 精确匹配
 * - RTU 模式：按 slaveId + functionCode FIFO 匹配
 * <p>
 * 只匹配收到响应的这条连接自己发出的请求（{@link PendingRequest#getChannel()} 的引用身份）：
 * 同一设备的新连接替换旧连接后，旧连接的条目不能抢走新连接的响应
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerPendingRequestManager {

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
        /**
         * 发出它的连接通道：登记、发送、匹配、清理都用这条连接的身份
         */
        private IotModbusTcpServerTransactionChannel channel;
        /**
         * 透传事务；标准轮询为 null
         */
        private IotModbusTx tx;

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
     * <p>
     * 异常响应只用来收尾透传请求：标准轮询的异常响应照旧丢弃，它的条目等到期清理。
     * RTU 模式的异常响应保持 FIFO，不会跳过排在前面的标准轮询去配给后面的透传
     *
     * @param deviceId    设备 ID
     * @param frame       收到的响应帧
     * @param frameFormat 帧格式
     * @param channel     收到响应的这条连接的事务通道
     * @return 匹配到的 PendingRequest，没有匹配返回 null
     */
    public PendingRequest matchResponse(Long deviceId, IotModbusFrame frame,
                                        IotModbusFrameFormatEnum frameFormat,
                                        IotModbusTcpServerTransactionChannel channel) {
        Deque<PendingRequest> queue = pendingRequests.get(deviceId);
        if (CollUtil.isEmpty(queue)) {
            return null;
        }

        // TCP 模式：按 transactionId 精确匹配
        if (frameFormat == IotModbusFrameFormatEnum.MODBUS_TCP && frame.getTransactionId() != null) {
            return matchByTransactionId(queue, channel, frame);
        }
        // RTU 模式：FIFO，匹配 slaveId + functionCode + registerCount
        // 异常响应（解码器已还原原功能码）的 pdu[0] 是异常码，不是 byteCount，所以跳过数量校验，只按 unit + 原功能码找排在最前面的请求
        if (frame.isException()) {
            return matchExceptionByFifo(queue, channel, frame.getSlaveId(), frame.getFunctionCode());
        }
        int responseRegisterCount = IotModbusCommonUtils.extractRegisterCountFromResponse(frame);
        return matchByFifo(queue, channel, frame.getSlaveId(), frame.getFunctionCode(), responseRegisterCount);
    }

    /**
     * 按 transactionId 匹配
     * <p>
     * 透传请求另外核对 unit 与功能码（异常响应的 functionCode 已由解码器还原），不一致就不出队，等它到期；标准轮询保持只按 transactionId
     */
    private PendingRequest matchByTransactionId(Deque<PendingRequest> queue,
                                                IotModbusTcpServerTransactionChannel channel,
                                                IotModbusFrame frame) {
        int transactionId = frame.getTransactionId();
        Iterator<PendingRequest> it = queue.iterator();
        while (it.hasNext()) {
            PendingRequest req = it.next();
            if (req.getChannel() != channel
                    || req.getTransactionId() == null || req.getTransactionId() != transactionId) {
                continue;
            }
            if (req.getTx() == null) {
                // 标准轮询：异常响应照旧丢弃，不出队
                if (frame.isException()) {
                    return null;
                }
            } else if (req.getSlaveId() != frame.getSlaveId() || req.getFunctionCode() != frame.getFunctionCode()) {
                return null;
            }
            it.remove();
            return req;
        }
        return null;
    }

    /**
     * 按 FIFO 匹配（slaveId + functionCode + registerCount）
     * <p>
     * 这是原有的 RTU 匹配逻辑，透传请求只是加入同一个 FIFO，有原有的限制：
     * RTU 帧没有事务号，同 unit、同功能码、同数量的迟到响应（例如到期清理后才到的轮询响应），
     * 或者同功能码的标准写（不登记待响应请求）的回显 / 异常响应，都可能被配给排在前面的另一个请求
     */
    private PendingRequest matchByFifo(Deque<PendingRequest> queue, IotModbusTcpServerTransactionChannel channel,
                                       int slaveId, int functionCode, int responseRegisterCount) {
        Iterator<PendingRequest> it = queue.iterator();
        while (it.hasNext()) {
            PendingRequest req = it.next();
            if (req.getChannel() == channel
                    && req.getSlaveId() == slaveId
                    && req.getFunctionCode() == functionCode
                    && (responseRegisterCount <= 0 || req.getRegisterCount() == responseRegisterCount)) {
                it.remove();
                return req;
            }
        }
        return null;
    }

    /**
     * 按 FIFO 匹配 RTU 异常响应（slaveId + 原功能码，异常响应没有数量可核对）
     * <p>
     * 只看本连接排在最前面的同 unit、同功能码的请求，不跳过它：
     * 1. 它是透传请求：出队，由设备的异常响应收尾；
     * 2. 它是标准轮询：照旧丢弃这个异常响应，轮询请求不出队、等到期清理；排在它后面的透传也不出队（按原有的 FIFO，异常响应属于更早的请求）
     */
    private PendingRequest matchExceptionByFifo(Deque<PendingRequest> queue, IotModbusTcpServerTransactionChannel channel,
                                                int slaveId, int functionCode) {
        Iterator<PendingRequest> it = queue.iterator();
        while (it.hasNext()) {
            PendingRequest req = it.next();
            if (req.getChannel() != channel || req.getSlaveId() != slaveId || req.getFunctionCode() != functionCode) {
                continue;
            }
            if (req.getTx() == null) {
                return null;
            }
            it.remove();
            return req;
        }
        return null;
    }

    /**
     * 本连接是否有占用该 transactionId 的待响应请求（分配事务号时跳过）
     *
     * @param deviceId      设备 ID
     * @param channel       连接的事务通道
     * @param transactionId 事务号
     * @return 是否占用
     */
    public boolean containsTransactionId(Long deviceId, IotModbusTcpServerTransactionChannel channel,
                                         int transactionId) {
        Deque<PendingRequest> queue = pendingRequests.get(deviceId);
        if (CollUtil.isEmpty(queue)) {
            return false;
        }
        for (PendingRequest req : queue) {
            if (req.getChannel() == channel
                    && req.getTransactionId() != null && req.getTransactionId() == transactionId) {
                return true;
            }
        }
        return false;
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
     * 移除指定的待响应请求：按引用（PendingRequest 是 @Data，equals 按字段比较），已被匹配或清理时无副作用
     */
    public void removeRequest(PendingRequest request) {
        Deque<PendingRequest> queue = pendingRequests.get(request.getDeviceId());
        if (queue != null) {
            queue.removeIf(req -> req == request);
        }
    }

    /**
     * 清理指定连接的所有待响应请求：只删这条连接自己的条目，不影响同一设备新连接的条目
     */
    public void removeChannel(Long deviceId, IotModbusTcpServerTransactionChannel channel) {
        Deque<PendingRequest> queue = pendingRequests.get(deviceId);
        if (queue != null) {
            queue.removeIf(req -> req.getChannel() == channel);
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
