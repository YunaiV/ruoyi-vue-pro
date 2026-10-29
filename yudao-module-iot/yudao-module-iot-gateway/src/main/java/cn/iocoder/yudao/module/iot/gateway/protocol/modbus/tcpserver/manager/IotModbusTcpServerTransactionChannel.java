package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager;

import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager.PendingRequest;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;

/**
 * IoT Modbus TCP Server 单个连接的事务通道
 * <p>
 * 认证注册时捕获这条连接的 socket 和 owner（event loop Context）：标准轮询、标准写、透传的登记、发送、匹配、清理都用这一个连接身份，
 * 不会在 A 连接上登记、从 B 连接发出。
 * <ul>
 *     <li>标准轮询、标准写：只通过 {@link #allocateTransactionId()} 取事务号、{@link #send} 写出，其余保持原逻辑：
 *     不排队、不受透传名额限制，超时只由清理定时器移除、不断开连接，标准写不等待回显；</li>
 *     <li>透传：排队 + 在途不超过 {@link #MAX_RAW_OUTSTANDING}；TCP 帧格式可以多个同时在途，
 *     RTU 帧格式同一时刻只有 1 个透传在途（轮询、标准写不受此限制）；实际发送时才登记待响应请求，
 *     排队过期、在途超时、写失败、连接关闭都只进入一次终态，同时释放名额和待响应请求。</li>
 * </ul>
 * 透传状态（queue / sent / closed）只在 owner 上读写，不加锁：submitRaw / onCleanup / onClose 可能来自总线消费线程、
 * 清理定时器、配置刷新定时器、stop 线程，一律先 runOnContext 切回 owner；onRawResponse（RecordParser 回调）、写出回调本来就在 owner 上
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerTransactionChannel {

    /**
     * 每个连接透传请求的排队 + 在途上限
     */
    public static final int MAX_RAW_OUTSTANDING = 32;

    /**
     * 本连接的 event loop Context：透传状态只在它上面读写
     */
    private final Context owner;
    /**
     * 认证注册时捕获的连接：标准轮询、标准写、透传都从它写出
     */
    private final NetSocket socket;
    /**
     * 设备编号
     */
    private final Long deviceId;
    /**
     * 从站地址
     */
    private final int slaveId;
    /**
     * 帧格式：TCP 可以多个透传同时在途（按事务号匹配），RTU 同一时刻只有 1 个透传在途（按 FIFO 匹配）
     */
    private final IotModbusFrameFormatEnum frameFormat;
    /**
     * 帧编码器：把 PDU 封装成 TCP（MBAP）/ RTU（CRC）帧
     */
    private final IotModbusFrameEncoder frameEncoder;
    /**
     * 协议共享的待响应请求管理器：透传实际发送时登记，终态时按引用移除
     */
    private final IotModbusTcpServerPendingRequestManager pendingRequestManager;
    /**
     * 协议共享的事务号计数器（16 位）
     */
    private final AtomicInteger transactionIdCounter;
    /**
     * 请求超时时间（毫秒）：透传排队截止、待响应到期都按它计算
     */
    @Getter
    private final int requestTimeout;
    /**
     * 本连接的 worker 队列（owner 上的有序有界队列，只在 owner 上提交）：
     * 标准轮询响应的点位翻译、发送到消息总线，以及断开后的下线消息会阻塞，按收到的顺序交给 worker 执行，不占用 owner
     */
    @Getter
    private final IotWorkerTaskQueue upstreamQueue;

    /**
     * 排队中的透传
     */
    private final Deque<IotModbusTx> queue = new ArrayDeque<>();
    /**
     * 在途的透传 → 它登记的待响应请求
     */
    private final Map<IotModbusTx, PendingRequest> sent = new HashMap<>();
    /**
     * 连接已关闭：不再发送透传
     */
    private boolean closed;

    public IotModbusTcpServerTransactionChannel(Context owner, NetSocket socket,
                                                Long deviceId, int slaveId, IotModbusFrameFormatEnum frameFormat,
                                                IotModbusFrameEncoder frameEncoder,
                                                IotModbusTcpServerPendingRequestManager pendingRequestManager,
                                                AtomicInteger transactionIdCounter, int requestTimeout) {
        this.owner = owner;
        this.socket = socket;
        this.deviceId = deviceId;
        this.slaveId = slaveId;
        this.frameFormat = frameFormat;
        this.frameEncoder = frameEncoder;
        this.pendingRequestManager = pendingRequestManager;
        this.transactionIdCounter = transactionIdCounter;
        this.requestTimeout = requestTimeout;
        this.upstreamQueue = new IotWorkerTaskQueue(owner);
    }

    // ========== 标准轮询、标准写的入口（任意线程） ==========

    /**
     * 分配事务号：RTU 帧格式返回 null
     * <p>
     * 复用协议共享的计数器（16 位），跳过本连接仍有待响应请求（轮询、在途透传）占用的编号。
     * 标准写不登记待响应请求，它的编号不受保护：计数器回绕时，仍在路上的写回显可能与新编号相同
     *
     * @return 事务号
     */
    public Integer allocateTransactionId() {
        if (!isTcpFormat()) {
            return null;
        }
        int id;
        int attempts = 0;
        do {
            id = transactionIdCounter.incrementAndGet() & 0xFFFF;
        } while (pendingRequestManager.containsTransactionId(deviceId, this, id) && ++attempts <= 0xFFFF);
        return id;
    }

    /**
     * 写出到本连接捕获的 socket
     *
     * @param data 完整帧
     * @return 写出结果：只代表 socket 写出成功或失败，不代表设备已经响应
     */
    public Future<Void> send(byte[] data) {
        return socket.write(Buffer.buffer(data));
    }

    // ========== 透传的入口（任意线程，先切回 owner） ==========

    /**
     * 提交透传事务
     *
     * @param tx 事务
     */
    public void submitRaw(IotModbusTx tx) {
        owner.runOnContext(v -> submitRaw0(tx));
    }

    /**
     * 清理周期（复用待响应请求的清理定时器）：在途透传是否超时、排队透传是否超过截止时间
     */
    public void onCleanup() {
        owner.runOnContext(v -> onCleanup0());
    }

    /**
     * 连接关闭（closeHandler，或 ConnectionManager 的 closeConnection / closeAll / 旧连接替换）；可以重复调用
     */
    public void onClose() {
        owner.runOnContext(v -> onClose0());
    }

    /**
     * 收到匹配到透传请求的响应（owner：handlePollingResponse 先 matchResponse 出队，再调用这里）
     *
     * @param frame   响应帧
     * @param request 匹配到的待响应请求
     */
    public void onRawResponse(IotModbusFrame frame, PendingRequest request) {
        // 1. 已超时、写失败或已关闭：迟到的响应丢弃
        IotModbusTx tx = request.getTx();
        if (sent.remove(tx) == null) {
            log.debug("[onRawResponse][设备 {} 迟到的透传响应, 丢弃, FC={}]", deviceId, frame.getFunctionCode());
            return;
        }
        // 2. 进入终态：异常响应原样回给透传请求；正常响应核对格式
        byte[] responsePdu = IotModbusCommonUtils.buildResponsePdu(frame);
        if (!frame.isException() && !IotModbusCommonUtils.isResponseShapeValid(tx.getPdu(), responsePdu)) {
            log.warn("[onRawResponse][设备 {} 透传响应格式与请求不一致, FC={}]", deviceId, frame.getFunctionCode());
            tx.fail(MODBUS_RESPONSE_INVALID);
        } else {
            tx.complete(responsePdu);
        }
        // 3. 释放了在途位置：继续发送排队中的透传
        pump();
    }

    // ========== owner 上的处理 ==========

    /**
     * owner：透传入队；连接已关闭、排队 + 在途已满时直接失败
     */
    private void submitRaw0(IotModbusTx tx) {
        // 1. 校验：连接已关闭、名额已满
        if (closed) {
            tx.fail(MODBUS_DISCONNECTED);
            return;
        }
        if (queue.size() + sent.size() >= MAX_RAW_OUTSTANDING) {
            tx.fail(MODBUS_QUEUE_FULL);
            return;
        }
        // 2. 入队，尝试发送
        queue.add(tx);
        pump();
    }

    /**
     * owner：按帧格式允许的在途数量，依次发送排队中的透传（TCP 不限，RTU 只在没有在途时发送 1 个）
     */
    private void pump() {
        while (!closed && !queue.isEmpty() && (isTcpFormat() || sent.isEmpty())) {
            IotModbusTx tx = queue.poll();
            // 1. 排队超过截止时间：不再发送
            if (tx.isExpired()) {
                tx.fail(MODBUS_TIMEOUT);
                continue;
            }
            // 2.1 分配事务号、封装帧
            Integer transactionId = allocateTransactionId();
            byte[] data = frameEncoder.wrapFrame(slaveId, tx.getPdu(), frameFormat, transactionId);
            // 2.2 只在实际发送时登记待响应请求，并占用在途位置
            PendingRequest request = buildPendingRequest(tx, transactionId);
            pendingRequestManager.addRequest(request);
            sent.put(tx, request);
            // 3. 写出：回调在 owner 上
            send(data).onFailure(e -> onRawWriteFailed(tx, e));
        }
    }

    /**
     * owner：透传写出失败，结束这一笔并继续发送排队中的透传
     */
    private void onRawWriteFailed(IotModbusTx tx, Throwable e) {
        log.warn("[onRawWriteFailed][设备 {} 透传写出失败]", deviceId, e);
        // 只结束这一笔透传，不主动关闭连接：连接真的异常时由 closeHandler 收尾
        if (release(tx)) {
            tx.fail(DEVICE_OFFLINE_OR_WRITE_FAILED);
            pump();
        }
    }

    /**
     * owner：清理周期，结束超时的在途透传、过期的排队透传
     */
    private void onCleanup0() {
        if (closed) {
            return;
        }
        // 1. 在途透传：登记的待响应请求已到期（与 cleanupExpired 的判断一致），不断开连接
        long now = System.currentTimeMillis();
        List<IotModbusTx> timeoutTxs = new ArrayList<>();
        for (Map.Entry<IotModbusTx, PendingRequest> entry : sent.entrySet()) {
            if (entry.getValue().getExpireAt() < now) {
                timeoutTxs.add(entry.getKey());
            }
        }
        for (IotModbusTx tx : timeoutTxs) {
            if (release(tx)) {
                tx.fail(MODBUS_TIMEOUT);
            }
        }
        // 2. 排队中的透传：超过截止时间的不再发送
        List<IotModbusTx> expiredTxs = new ArrayList<>();
        for (Iterator<IotModbusTx> it = queue.iterator(); it.hasNext(); ) {
            IotModbusTx tx = it.next();
            if (tx.isExpired()) {
                it.remove();
                expiredTxs.add(tx);
            }
        }
        for (IotModbusTx tx : expiredTxs) {
            tx.fail(MODBUS_TIMEOUT);
        }
        // 3. 释放了在途位置：继续发送排队中的透传
        pump();
    }

    /**
     * owner：连接关闭，排队中、在途的透传全部以断线结束；可能重复到达，照常清理；清理后各集合为空，重复执行无副作用
     */
    private void onClose0() {
        // 1. 标记关闭，取出排队中、在途的透传
        closed = true;
        List<IotModbusTx> txs = new ArrayList<>(queue);
        txs.addAll(sent.keySet());
        queue.clear();
        sent.clear();
        // 2. 只删这条连接自己登记的待响应请求（轮询 + 透传），不影响同一设备新连接的条目
        pendingRequestManager.removeChannel(deviceId, this);
        // 3. 全部以断线结束
        for (IotModbusTx tx : txs) {
            tx.fail(MODBUS_DISCONNECTED);
        }
    }

    /**
     * owner：把透传移出在途位置，并按引用移除它登记的待响应请求
     *
     * @return false 表示它已经不在途（重复的终态信号直接忽略）
     */
    private boolean release(IotModbusTx tx) {
        PendingRequest request = sent.remove(tx);
        if (request == null) {
            return false;
        }
        pendingRequestManager.removeRequest(request);
        return true;
    }

    /**
     * 构造透传登记的待响应请求：到期时间 = 现在 + requestTimeout
     *
     * @param tx            事务
     * @param transactionId 事务号；RTU 帧格式为 null
     * @return 待响应请求
     */
    private PendingRequest buildPendingRequest(IotModbusTx tx, Integer transactionId) {
        byte[] pdu = tx.getPdu();
        int functionCode = pdu[0] & 0xFF;
        int registerAddress = ((pdu[1] & 0xFF) << 8) | (pdu[2] & 0xFF);
        // registerCount 用于 RTU FIFO 按原逻辑核对 03 / 04 的正常响应
        return new PendingRequest(deviceId, null, null, slaveId, functionCode, registerAddress,
                IotModbusCommonUtils.getRequestQuantity(pdu), transactionId,
                System.currentTimeMillis() + requestTimeout, this, tx);
    }

    /**
     * @return 是否是 TCP 帧格式（MBAP，按事务号匹配响应）
     */
    private boolean isTcpFormat() {
        return frameFormat == IotModbusFrameFormatEnum.MODBUS_TCP;
    }

}
