package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 到货通知单状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmArrivalNoticeStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesWmArrivalNoticeService#createArrivalNotice 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 待质检（需要检验时），对接 qc 模块的 MesQcIqcDO 后续流程
     *
     * 对应 MesWmArrivalNoticeService#submitArrivalNotice 方法
     */
    PENDING_QC(MesOrderStatusConstants.APPROVING, "待质检"),
    /**
     * 待入库（不需要检验时，或检验完成时），对接 wm 模块的 MesWmItemReceiptDO 后续流程
     *
     * 对应方法：
     * 1. 不需要检验时：MesWmArrivalNoticeService#submitArrivalNotice 方法
     * 2. 或检验完成时：MesWmArrivalNoticeService#updateArrivalNoticeWhenIqcFinish 方法
     */
    PENDING_RECEIPT(MesOrderStatusConstants.APPROVED, "待入库"),
    /**
     * 已完成
     *
     * 对应 MesWmArrivalNoticeService#finishArrivalNotice 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmArrivalNoticeStatusEnum::getStatus).toArray(Integer[]::new);

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
