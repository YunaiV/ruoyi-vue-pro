package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerTransactionChannel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * IoT Modbus 透传事务（thing.raw.push，或编码脚本输出的 PDU），server、client 共用
 * <p>
 * 标准轮询、标准写不创建事务，保持原有的发送与匹配方式。
 * 只有一个终态：{@link #complete} / {@link #fail} 通过 CAS 只生效一次，之后的终态信号（迟到的响应、重复的超时、断线）直接忽略。
 * 终态回调在触发终态的线程上执行（server：连接的 owner；client：executeBlocking 的完成回调），只能做非阻塞动作
 *
 * @author 芋道源码
 */
@Getter
@Slf4j
public class IotModbusTx {

    /**
     * 原下行请求
     */
    private final IotDeviceMessage request;
    /**
     * 请求 PDU（功能码 + 数据，不带 slaveId、CRC、MBAP）
     */
    private final byte[] pdu;
    /**
     * 截止时间（System.nanoTime）：排队超过它就不再发送
     */
    private final long deadlineNanos;

    /**
     * 终态回调：responsePdu = 设备响应 PDU（含异常响应 fc|0x80 + 异常码），failure = 网关判定的失败，二者只有一个非空
     */
    @Getter(AccessLevel.NONE)
    private final BiConsumer<byte[], ErrorCode> callback;
    @Getter(AccessLevel.NONE)
    private final AtomicBoolean done = new AtomicBoolean();

    /**
     * 只通过 {@link #ofServer} / {@link #ofClient} 创建
     */
    private IotModbusTx(IotDeviceMessage request, byte[] pdu, long deadlineNanos,
                        BiConsumer<byte[], ErrorCode> callback) {
        this.request = request;
        this.pdu = pdu;
        this.deadlineNanos = deadlineNanos;
        this.callback = callback;
    }

    /**
     * 创建 server 端的透传事务：截止时间 = 现在 + 通道的 requestTimeout
     *
     * @param channel  提交它的连接通道：只用于读取 requestTimeout
     * @param pdu      请求 PDU
     * @param request  原下行请求
     * @param callback 终态回调（可为 null）
     * @return 事务
     */
    public static IotModbusTx ofServer(IotModbusTcpServerTransactionChannel channel, byte[] pdu,
                                       IotDeviceMessage request, BiConsumer<byte[], ErrorCode> callback) {
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(channel.getRequestTimeout());
        return new IotModbusTx(request, pdu, deadlineNanos, callback);
    }

    /**
     * 创建 client 端的透传事务
     *
     * @param request       原下行请求
     * @param pdu           请求 PDU
     * @param deadlineNanos 截止时间（System.nanoTime）
     * @param callback      终态回调
     * @return 事务
     */
    public static IotModbusTx ofClient(IotDeviceMessage request, byte[] pdu, long deadlineNanos,
                                       BiConsumer<byte[], ErrorCode> callback) {
        return new IotModbusTx(request, pdu, deadlineNanos, callback);
    }

    /**
     * 收到设备响应（包括异常响应）：进入成功终态
     *
     * @param responsePdu 响应 PDU
     * @return 是否由本次调用进入终态
     */
    public boolean complete(byte[] responsePdu) {
        return terminate(responsePdu, null);
    }

    /**
     * 网关判定的失败（超时、断线、写失败、队列满、响应不合法）：进入失败终态
     *
     * @param failure 错误码
     * @return 是否由本次调用进入终态
     */
    public boolean fail(ErrorCode failure) {
        return terminate(null, failure);
    }

    /**
     * @return 是否已经超过截止时间
     */
    public boolean isExpired() {
        return System.nanoTime() - deadlineNanos >= 0;
    }

    /**
     * 进入终态：只有第一次调用生效，并执行终态回调
     *
     * @param responsePdu 响应 PDU；失败时为 null
     * @param failure     网关判定的失败；成功时为 null
     * @return 是否由本次调用进入终态
     */
    private boolean terminate(byte[] responsePdu, ErrorCode failure) {
        // 1. CAS：已进入终态时直接忽略（迟到的响应、重复的超时、断线）
        if (!done.compareAndSet(false, true)) {
            return false;
        }
        if (callback == null) {
            return true;
        }
        // 2. 执行终态回调：异常只记日志，不影响调用方（owner 上的清理、executeBlocking 的完成回调）
        try {
            callback.accept(responsePdu, failure);
        } catch (Exception e) {
            log.error("[terminate][Modbus 透传事务终态回调异常, deviceId={}]",
                    request != null ? request.getDeviceId() : null, e);
        }
        return true;
    }

}
