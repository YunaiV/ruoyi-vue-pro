package cn.iocoder.yudao.module.mes.enums.qc;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.mes.enums.MesOrderStatusConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 质检单状态枚举
 *
 * 适用于：IQC（来料检验）、IPQC（过程检验）、OQC（出货检验）、RQC（退货检验）
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesQcStatusEnum implements ArrayValuable<Integer> {

    DRAFT(MesOrderStatusConstants.PREPARE, "草稿"),
    FINISHED(MesOrderStatusConstants.FINISHED, "已完成");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(MesQcStatusEnum::getStatus).toArray(Integer[]::new);

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
