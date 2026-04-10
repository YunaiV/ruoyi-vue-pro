package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 销售出库单状态枚举
 *
 * 对应字典 mes_wm_product_sales_status
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmProductSalesStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesWmProductSalesService#createProductSales 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 待检测（需要 OQC 检验时），对接 qc 模块的 MesQcOqcDO 后续流程
     *
     * 对应 MesWmProductSalesService#submitProductSales 方法
     */
    CONFIRMED(MesOrderStatusConstants.CONFIRMED, "待检测"),
    /**
     * 待拣货
     *
     * 对应方法：
     * 1. 不需要 OQC 检验时：MesWmProductSalesService#submitProductSales 方法
     * 2. 或 OQC 检验完成时：MesWmProductSalesService#confirmProductSales 方法
     */
    APPROVING(MesOrderStatusConstants.APPROVING, "待拣货"),
    /**
     * 待填写运单
     *
     * 对应 MesWmProductSalesService#shippingProductSales 方法
     */
    SHIPPING(10, "待填写运单"), // 10 是一个特殊的状态值，不在 MesOrderStatusConstants 中，单独定义
    /**
     * 待执行出库
     *
     * 对应 MesWmProductSalesService#stockProductSales 方法
     */
    APPROVED(MesOrderStatusConstants.APPROVED, "待执行出库"),
    /**
     * 已完成
     *
     * 对应 MesWmProductSalesService#finishProductSales 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成"),
    /**
     * 已取消
     *
     * 对应 MesWmProductSalesService#cancelProductSales 方法
     */
    CANCELED(MesOrderStatusConstants.CANCELLED, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmProductSalesStatusEnum::getStatus).toArray(Integer[]::new);

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
