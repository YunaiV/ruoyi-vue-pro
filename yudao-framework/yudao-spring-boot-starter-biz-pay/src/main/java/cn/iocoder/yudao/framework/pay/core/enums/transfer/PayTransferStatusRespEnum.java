package cn.iocoder.yudao.framework.pay.core.enums.transfer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 渠道的转账状态枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum PayTransferStatusRespEnum {

    WAITING(0, "等待转账"),
    SUCCESS(10, "转账成功"),
    FAILURE(20, "转账失败");

    private final Integer status;
    private final String name;
}
