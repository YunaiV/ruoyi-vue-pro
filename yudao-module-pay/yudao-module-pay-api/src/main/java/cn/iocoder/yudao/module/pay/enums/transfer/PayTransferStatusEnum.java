package cn.iocoder.yudao.module.pay.enums.transfer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 渠道的转账状态枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum PayTransferStatusEnum {

    WAITING(0, "等待转账"),
    PROCESSING(5, "转账进行中"),
    SUCCESS(10, "转账成功"),
    CLOSED(20, "转账关闭");

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

    public static boolean isProcessing(Integer status) {
        return Objects.equals(status, PROCESSING.getStatus());
    }

    /**
     * 是否处于待转账或者转账中的状态
     *
     * @param status 状态
     * @return 是否
     */
    public static boolean isWaitingOrProcessing(Integer status) {
        return isWaiting(status) || isProcessing(status);
    }

    /**
     * 是否处于成功或者关闭中的状态
     *
     * @param status 状态
     * @return 是否
     */
    public static boolean isSuccessOrClosed(Integer status) {
        return isSuccess(status) || isClosed(status);
    }

}
