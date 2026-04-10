package cn.iocoder.yudao.module.mes.enums.dv;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 维修工单状态枚举
 *
 * 状态流转：草稿 → 维修中 → 待验收 → 已确认
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesDvRepairStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesDvRepairService#createRepair 方法
     */
    PREPARE(MesOrderStatusConstants.PREPARE, "草稿"),
    /**
     * 维修中（提交后，维修人接单）
     *
     * 对应 MesDvRepairService#submitRepair 方法
     */
    CONFIRMED(MesOrderStatusConstants.CONFIRMED, "维修中"),
    /**
     * 待验收（维修完成，等待验收）
     *
     * 对应 MesDvRepairService#confirmRepair 方法
     */
    APPROVING(MesOrderStatusConstants.APPROVING, "待验收"),
    /**
     * 已确认（验收通过或不通过，终态）
     *
     * 对应 MesDvRepairService#finishRepair 方法
     */
    FINISHED(MesOrderStatusConstants.FINISHED, "已确认");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesDvRepairStatusEnum::getStatus).toArray(Integer[]::new);

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
