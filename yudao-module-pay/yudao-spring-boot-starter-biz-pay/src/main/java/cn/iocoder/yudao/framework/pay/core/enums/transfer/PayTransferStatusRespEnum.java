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

    /**
     * TODO 转账到银行卡. 会有T+0 T+1 到账的请情况。 还未实现
     * TODO @jason：可以看看其它开源项目，针对这个场景，处理策略是怎么样的？例如说，每天主动轮询？这个状态的单子？
     */
    IN_PROGRESS(10, "转账进行中"),

    SUCCESS(20, "转账成功"),
    /**
     * 转账关闭 (失败，或者其它情况)
     */
    CLOSED(30, "转账关闭");

    private final Integer status;
    private final String name;

    public static boolean isSuccess(Integer status) {
        return Objects.equals(status, SUCCESS.getStatus());
    }

    public static boolean isClosed(Integer status) {
        return Objects.equals(status, CLOSED.getStatus());
    }

    public static boolean isInProgress(Integer status) {
        return Objects.equals(status, IN_PROGRESS.getStatus());
    }
}
