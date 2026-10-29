package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.IotModbusTcpServerProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_PDU_INVALID;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_RESPONSE_INVALID;

/**
 * IoT Modbus TCP Server 下行消息订阅器：订阅消息总线的下行消息并转发给处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    private final IotModbusTcpServerDownstreamHandler downstreamHandler;
    private final IotModbusTcpServerConnectionManager connectionManager;

    public IotModbusTcpServerDownstreamSubscriber(IotModbusTcpServerProtocol protocol,
                                                  IotModbusTcpServerDownstreamHandler downstreamHandler,
                                                  IotModbusTcpServerConnectionManager connectionManager,
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
     * 透传写出：提交透传事务到连接的事务通道
     * <p>
     * 提交之前的失败（PDU 不合法、未连接或未认证）返回失败 Future，只记日志；
     * 提交之后，返回的 Future 在事务终态（响应 / 超时 / 断线 / 写失败 / 队列满）成功完成，
     * 结果由事务经 {@link #handleRawResult} 处理
     */
    @Override
    public Future<Void> handleRawMessage(IotDeviceMessage message, byte[] bytes, IotScriptSource source) {
        // 1.1 校验 PDU：与 biz 的 /send、encode 输出校验是同一份规则
        try {
            IotDeviceMessageUtils.validateModbusRequestPdu(bytes);
        } catch (IllegalArgumentException e) {
            return Future.failedFuture(exception(MODBUS_PDU_INVALID, e.getMessage()));
        }
        // 1.2 获取连接（未连接、未认证的连接没有事务通道）
        ConnectionInfo info = connectionManager.getConnectionInfoByDeviceId(message.getDeviceId());
        if (info == null || info.getChannel() == null) {
            return Future.failedFuture(exception(DEVICE_OFFLINE_OR_WRITE_FAILED));
        }

        // 2. 提交事务：终态时先完成 Future，再处理结果（设备响应含异常响应 → responsePdu；网关判定 → failure）
        Promise<Void> terminal = Promise.promise();
        IotModbusTx tx = IotModbusTx.ofServer(info.getChannel(), bytes, message,
                (responsePdu, failure) -> handleRawResult(terminal, message, source, responsePdu, failure));
        info.getChannel().submitRaw(tx);
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
