package cn.iocoder.yudao.module.mes.enums.dv;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 设备保养记录状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesDvMaintenRecordStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesDvMaintenRecordService#createMaintenRecord 方法
     */
    PREPARE(1, "草稿"),
    /**
     * 已提交
     *
     * 对应 MesDvMaintenRecordService#submitMaintenRecord 方法
     */
    SUBMITTED(2, "已提交");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesDvMaintenRecordStatusEnum::getStatus)
            .toArray(Integer[]::new);

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
