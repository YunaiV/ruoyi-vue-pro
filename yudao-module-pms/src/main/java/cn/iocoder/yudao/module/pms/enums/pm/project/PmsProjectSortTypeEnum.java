package cn.iocoder.yudao.module.pms.enums.pm.project;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 项目排序类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsProjectSortTypeEnum implements ArrayValuable<Integer> {

    ACCESS_TIME(1, "按访问时间"),
    CREATE_TIME(2, "按创建时间");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PmsProjectSortTypeEnum::getType)
            .toArray(Integer[]::new);

    /**
     * 排序类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PmsProjectSortTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
