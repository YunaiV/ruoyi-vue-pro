package cn.iocoder.yudao.module.oa.enums.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 工作计划状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaPlanStatusEnum implements ArrayValuable<Integer> {

    UNFINISHED(1, "未完成"),
    FINISHED(2, "已完成"),
    CANCELED(3, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaPlanStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 名称
     */
    private final String name;

    public static OaPlanStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
