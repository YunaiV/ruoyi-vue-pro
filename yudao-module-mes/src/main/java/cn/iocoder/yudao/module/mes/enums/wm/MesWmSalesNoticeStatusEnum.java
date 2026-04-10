package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 发货通知单状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmSalesNoticeStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesWmSalesNoticeService#createSalesNotice 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 待出库，对接 wm 模块的 MesWmProductSalesDO 后续流程
     *
     * 对应 MesWmSalesNoticeService#submitSalesNotice 方法
     */
    APPROVED(MesOrderStatusConstants.APPROVED, "待出库");
    // TODO @芋艿：【对齐】暂时无后续流程，看看后续要怎么支持下。

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmSalesNoticeStatusEnum::getStatus).toArray(Integer[]::new);

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
