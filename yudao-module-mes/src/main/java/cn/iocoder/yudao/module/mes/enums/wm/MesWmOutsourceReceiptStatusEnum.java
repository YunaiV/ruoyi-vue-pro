package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 外协入库单状态枚举
 *
 * 对应字典 mes_wm_outsource_receipt_status
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmOutsourceReceiptStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesWmOutsourceReceiptService#createOutsourceReceipt 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 待检验（已确认，等待质检）
     *
     * 对应 MesWmOutsourceReceiptService#submitOutsourceReceipt 方法
     */
    CONFIRMED(MesOrderStatusConstants.CONFIRMED, "待检验"),
    /**
     * 待上架（检验完成，等待仓库上架）
     *
     * 对应 MesWmOutsourceReceiptService#submitOutsourceReceipt 方法
     */
    APPROVING(MesOrderStatusConstants.APPROVING, "待上架"),
    /**
     * 待执行入库（上架完成，等待执行入库操作）
     *
     * 对应 MesWmOutsourceReceiptService#stockOutsourceReceipt 方法
     */
    APPROVED(MesOrderStatusConstants.APPROVED, "待执行入库"),
    /**
     * 已完成（入库执行完成，库存已更新）
     *
     * 对应 MesWmOutsourceReceiptService#finishOutsourceReceipt 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成"),
    /**
     * 已取消
     *
     * 对应 MesWmOutsourceReceiptService#cancelOutsourceReceipt 方法
     */
    CANCELED(MesOrderStatusConstants.CANCELLED, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmOutsourceReceiptStatusEnum::getStatus).toArray(Integer[]::new);

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
