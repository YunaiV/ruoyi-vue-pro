package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 销售退货单状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmReturnSalesStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesWmReturnSalesService#createReturnSales 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 待检验（已确认，等待质检）
     *
     * 对应 MesWmReturnSalesService#submitReturnSales 方法
     */
    CONFIRMED(MesOrderStatusConstants.CONFIRMED, "待检验"), // "UNCHECK"
    /**
     * 待执行（检验完成或免检，等待执行退货）
     *
     * 对应 MesWmReturnSalesService#submitReturnSales 方法
     */
    APPROVING(MesOrderStatusConstants.APPROVING, "待执行"), // "UNEXECUTE"
    /**
     * 待上架（退货执行完成，等待仓库上架）
     *
     * 对应 MesWmReturnSalesService#finishReturnSales 方法
     */
    APPROVED(MesOrderStatusConstants.APPROVED, "待上架"), // "UNSTOCK"
    /**
     * 已完成（上架完成，库存已更新）
     *
     * 对应 MesWmReturnSalesService#stockReturnSales 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成"),
    /**
     * 已取消
     *
     * 对应 MesWmReturnSalesService#cancelReturnSales 方法
     */
    CANCELED(MesOrderStatusConstants.CANCELLED, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmReturnSalesStatusEnum::getStatus).toArray(Integer[]::new);

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
