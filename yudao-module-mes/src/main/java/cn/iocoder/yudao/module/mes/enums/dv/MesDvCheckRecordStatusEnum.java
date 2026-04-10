package cn.iocoder.yudao.module.mes.enums.dv;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 设备点检记录状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesDvCheckRecordStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿
     *
     * 对应 MesDvCheckRecordService#createCheckRecord 方法
     */
    DRAFT(10, "草稿"),
    /**
     * 已完成
     *
     * 对应 MesDvCheckRecordService#submitCheckRecord 方法
     */
    FINISHED(20, "已完成");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesDvCheckRecordStatusEnum::getStatus).toArray(Integer[]::new);

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
