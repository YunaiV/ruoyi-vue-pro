package cn.iocoder.yudao.module.pay.enums.transfer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum PayTransferStatusEnum {

    WAITING(0, "等待转账"),
    /**
     * TODO 转账到银行卡. 会有T+0 T+1 到账的请情况。 还未实现
     */
    IN_PROGRESS(10, "转账进行中"),

    SUCCESS(20, "转账成功"),
    /**
     * 转账关闭 (失败，或者其它情况) // TODO 改成 转账失败状态
     */
    CLOSED(30, "转账关闭");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    public static boolean isSuccess(Integer status) {
        return Objects.equals(status, SUCCESS.getStatus());
    }

    public static boolean isClosed(Integer status) {
        return Objects.equals(status, CLOSED.getStatus());
    }

    public static boolean isWaiting(Integer status) {
        return Objects.equals(status, WAITING.getStatus());
    }
    public static boolean isInProgress(Integer status) {
        return Objects.equals(status, IN_PROGRESS.getStatus());
    }

    /**
     * 是否处于待转账或者转账中的状态
     * @param status 状态
     */
    public static boolean isPendingStatus(Integer status) {
        return Objects.equals(status, WAITING.getStatus()) || Objects.equals(status, IN_PROGRESS.getStatus());
    }

}
