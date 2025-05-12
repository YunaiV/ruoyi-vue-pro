package cn.iocoder.yudao.framework.pay.core.enums.transfer;

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
public enum PayTransferStatusRespEnum {

    WAITING(0, "等待转账"),
    PROCESSING(5, "转账进行中"),
    SUCCESS(10, "转账成功"),
    CLOSED(20, "转账关闭");

    private final Integer status;
    private final String name;

    public static boolean isSuccess(Integer status) {
        return Objects.equals(status, SUCCESS.getStatus());
    }

    public static boolean isClosed(Integer status) {
        return Objects.equals(status, CLOSED.getStatus());
    }

    public static boolean isProcessing(Integer status) {
        return Objects.equals(status, PROCESSING.getStatus());
    }

}
