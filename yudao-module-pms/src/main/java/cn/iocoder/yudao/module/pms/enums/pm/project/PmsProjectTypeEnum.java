package cn.iocoder.yudao.module.pms.enums.pm.project;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 项目类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsProjectTypeEnum implements ArrayValuable<Integer> {

    GENERAL(1, "通用项目"),
    AGILE(2, "敏捷开发项目");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PmsProjectTypeEnum::getType)
            .toArray(Integer[]::new);

    /**
     * 类型
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

    public static PmsProjectTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
