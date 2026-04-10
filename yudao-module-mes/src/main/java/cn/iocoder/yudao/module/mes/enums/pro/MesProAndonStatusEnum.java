package cn.iocoder.yudao.module.mes.enums.pro;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 安灯处置状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesProAndonStatusEnum implements ArrayValuable<Integer> {

    /**
     * 未处置
     *
     * 对应 MesProAndonRecordService#createAndonRecord 方法
     */
    ACTIVE(0, "未处置"),
    /**
     * 已处置
     *
     * 对应 MesProAndonRecordService#updateAndonRecord 方法
     */
    HANDLED(1, "已处置");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesProAndonStatusEnum::getStatus).toArray(Integer[]::new);

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
