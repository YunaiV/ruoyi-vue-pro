package cn.iocoder.yudao.module.fms.enums.voucher;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 凭证状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsVoucherStatusEnum implements ArrayValuable<Integer> {

    PENDING_REVIEW(0, "待审核"),
    APPROVED(1, "已审核"),
    REJECTED(2, "已驳回"),
    REVIEWING(3, "审核中"),
    WITHDRAWN(4, "已撤回"),
    UNSUBMITTED(5, "未提交"),
    CREATED(6, "已创建"),
    DELETED(7, "已删除"),
    VOIDED(8, "已作废");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsVoucherStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static FmsVoucherStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
