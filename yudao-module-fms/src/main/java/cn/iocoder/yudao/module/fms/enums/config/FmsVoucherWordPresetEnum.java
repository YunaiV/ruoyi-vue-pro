package cn.iocoder.yudao.module.fms.enums.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FMS 预置凭证字枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsVoucherWordPresetEnum {

    RECORD("记", "记账凭证", true, 1),
    RECEIPT("收", "收款凭证", false, 2),
    TRANSFER("转", "转账凭证", false, 3),
    PAYMENT("付", "付款凭证", false, 4);

    /**
     * 凭证字
     */
    private final String name;
    /**
     * 打印标题
     */
    private final String printTitle;
    /**
     * 是否默认凭证字
     */
    private final Boolean defaultStatus;
    /**
     * 显示顺序
     */
    private final Integer sort;

}
