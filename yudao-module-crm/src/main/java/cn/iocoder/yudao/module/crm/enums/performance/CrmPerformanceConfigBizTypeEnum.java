package cn.iocoder.yudao.module.crm.enums.performance;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 业绩目标业务类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum CrmPerformanceConfigBizTypeEnum implements ArrayValuable<Integer> {

    CRM_CONTRACT(CrmBizTypeEnum.CRM_CONTRACT.getType(), "销售目标"),
    CRM_RECEIVABLE(CrmBizTypeEnum.CRM_RECEIVABLE.getType(), "回款目标");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(CrmPerformanceConfigBizTypeEnum::getBizType).toArray(Integer[]::new);

    /**
     * 业务类型
     */
    private final Integer bizType;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
