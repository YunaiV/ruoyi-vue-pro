package cn.iocoder.yudao.module.pms.enums.pm.project;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 项目列表场景枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsProjectSceneTypeEnum implements ArrayValuable<Integer> {

    ALL(1, "全部项目"),
    MANAGED(2, "我负责的"),
    PARTICIPATED(3, "我参与的");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PmsProjectSceneTypeEnum::getType)
            .toArray(Integer[]::new);

    /**
     * 场景类型
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

    public static PmsProjectSceneTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
