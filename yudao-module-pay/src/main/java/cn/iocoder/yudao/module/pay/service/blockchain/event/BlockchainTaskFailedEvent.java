package cn.iocoder.yudao.module.pay.service.blockchain.event;

import lombok.Getter;

/**
 * 区块链存证任务失败事件
 * 供监控系统捕获并触发告警
 *
 * @author deepay
 */
@Getter
public class BlockchainTaskFailedEvent {

    private final String orderId;
    private final Throwable exception;

    public BlockchainTaskFailedEvent(String orderId, Throwable exception) {
        this.orderId = orderId;
        this.exception = exception;
    }

}
