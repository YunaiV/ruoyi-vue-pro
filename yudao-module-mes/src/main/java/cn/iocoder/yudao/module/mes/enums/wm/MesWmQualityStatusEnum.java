package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 质量状态枚举
 *
 * 对应字典 mes_wm_quality_status
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmQualityStatusEnum implements ArrayValuable<Integer> {

    /**
     * 待检验
     */
    PENDING(0, "待检验"),
    /**
     * 合格
     */
    PASS(1, "合格"),
    /**
     * 不合格
     */
    FAIL(2, "不合格");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmQualityStatusEnum::getStatus).toArray(Integer[]::new);

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
