package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusTcpClientUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.IotModbusTcpClientProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager.ModbusConnection;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;

/**
 * IoT Modbus TCP 下行消息订阅器：订阅消息总线的下行消息并转发给处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpClientDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    /**
     * 每个连接透传请求的在途 + 排队上限
     */
    public static final int MAX_RAW_OUTSTANDING = 32;

    private final IotModbusTcpClientDownstreamHandler downstreamHandler;
    private final IotModbusTcpClientConnectionManager connectionManager;

    public IotModbusTcpClientDownstreamSubscriber(IotModbusTcpClientProtocol protocol,
                                                  IotModbusTcpClientDownstreamHandler downstreamHandler,
                                                  IotModbusTcpClientConnectionManager connectionManager,
                                                  IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
        this.connectionManager = connectionManager;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

    /**
     * 透传写出：占用 raw 名额，提交到连接的有序 executeBlocking
     * <p>
     * 提交之前的失败（PDU 不合法、未连接、raw 名额已满、提交或注册完成回调被同步拒绝）返回失败 Future，只记日志；
     * 提交之后，executeBlocking 结束（成功、异常、j2mod 超时）就是事务终态：返回的 Future 随即成功完成，
     * 结果由事务经 {@link #handleRawResult} 处理。raw 名额在事务终态回调里释放，事务只进入一次终态，名额也只释放一次
     */
    @Override
    public Future<Void> handleRawMessage(IotDeviceMessage message, byte[] bytes, IotScriptSource source) {
        // 1.1 校验 PDU：与 biz 的 /send、encode 输出校验是同一份规则
        try {
            IotDeviceMessageUtils.validateModbusRequestPdu(bytes);
        } catch (IllegalArgumentException e) {
            return Future.failedFuture(exception(MODBUS_PDU_INVALID, e.getMessage()));
        }
        // 1.2 获取连接和 slave ID（一个连接可挂多个从站）
        ModbusConnection connection = connectionManager.getConnection(message.getDeviceId());
        Integer slaveId = connection != null ? connection.getSlaveId(message.getDeviceId()) : null;
        if (slaveId == null) {
            return Future.failedFuture(exception(DEVICE_OFFLINE_OR_WRITE_FAILED));
        }
        // 1.3 占用 raw 名额：尚未进入事务，失败 Future 只记日志
        if (!connection.tryAcquireRawSlot(MAX_RAW_OUTSTANDING)) {
            return Future.failedFuture(exception(MODBUS_QUEUE_FULL));
        }

        // 2.1 构造事务：截止时间取本连接现有的 timeout（也是 j2mod 的读超时），提交前算一次；
        //     终态回调先释放名额：事务的终态只生效一次（CAS），名额也只释放一次
        Promise<Void> terminal = Promise.promise();
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(connection.getTimeout());
        IotModbusTx tx = IotModbusTx.ofClient(message, bytes, deadlineNanos, (responsePdu, failure) -> {
            connection.releaseRawSlot();
            handleRawResult(terminal, message, source, responsePdu, failure);
        });
        // 2.2 提交到有序 executeBlocking：结束即终态（这里不重连，重连由下一个任务开始时的守卫完成）
        try {
            IotModbusTcpClientUtils.executeRaw(connection, slaveId, tx).onComplete(ar -> {
                if (ar.succeeded()) {
                    tx.complete(ar.result());
                } else {
                    log.warn("[handleRawMessage][设备 {} 透传失败]", message.getDeviceId(), ar.cause());
                    tx.fail(IotModbusTcpClientUtils.toRawFailure(ar.cause()));
                }
            });
        } catch (RuntimeException e) {
            // 2.3 提交或注册完成回调被同步拒绝（例如 Context 已关闭）：先让返回的 Future 失败，再结束事务释放名额；
            //     完成回调已经执行过（事务已是终态）时，两步都不生效，不会重复释放
            log.warn("[handleRawMessage][设备 {} 透传无法提交到连接的 Context]", message.getDeviceId(), e);
            terminal.tryFail(exception(DEVICE_OFFLINE_OR_WRITE_FAILED));
            tx.fail(DEVICE_OFFLINE_OR_WRITE_FAILED);
        }
        return terminal.future();
    }

    /**
     * 透传事务终态：先完成 Future，再处理结果
     * <p>
     * 设备的实际响应（包括异常响应）经 {@link #replyRawResult} 回复给 biz；
     * 网关判定的失败（超时、断线、写失败、队列满、响应不合法）只记日志，不给 biz 构造回复
     */
    private void handleRawResult(Promise<Void> terminal, IotDeviceMessage message, IotScriptSource source,
                                 byte[] responsePdu, ErrorCode failure) {
        terminal.tryComplete();
        if (failure != null || ArrayUtil.isEmpty(responsePdu)) {
            ErrorCode code = failure != null ? failure : MODBUS_RESPONSE_INVALID;
            log.warn("[handleRawResult][下行({}) 事务失败 code({}) msg({})]", message.getRequestId(),
                    code.getCode(), code.getMsg());
            return;
        }
        replyRawResult(message, source, responsePdu, IotModbusCommonUtils.getRawResponseError(responsePdu));
    }

    @Override
    public boolean supportsRawDownstream() {
        return true;
    }

}
