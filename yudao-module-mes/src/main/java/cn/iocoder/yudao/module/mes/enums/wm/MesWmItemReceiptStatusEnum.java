package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 采购入库单状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmItemReceiptStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesWmItemReceiptService#createItemReceipt 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 待上架
     *
     * 对应 MesWmItemReceiptService#submitItemReceipt 方法
     */
    APPROVING(MesOrderStatusConstants.APPROVING, "待上架"),
    /**
     * 待入库
     *
     * 对应 MesWmItemReceiptService#stockItemReceipt 方法
     */
    APPROVED(MesOrderStatusConstants.APPROVED, "待执行入库"),
    /**
     * 已完成
     *
     * 对应 MesWmItemReceiptService#finishItemReceipt 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成"),
    /**
     * 已取消
     *
     * 对应 MesWmItemReceiptService#cancelItemReceipt 方法
     */
    CANCELED(MesOrderStatusConstants.CANCELLED, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmItemReceiptStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
