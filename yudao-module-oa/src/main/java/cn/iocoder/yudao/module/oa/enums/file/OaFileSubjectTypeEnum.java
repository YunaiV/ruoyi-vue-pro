package cn.iocoder.yudao.module.oa.enums.file;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 云盘共享主体类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaFileSubjectTypeEnum implements ArrayValuable<Integer> {

    USER(1, "用户"),
    DEPT(2, "部门");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaFileSubjectTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 云盘共享主体类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    /**
     * 获得共享主体类型枚举
     *
     * @param type 共享主体类型
     * @return 共享主体类型枚举，不存在时返回 null
     */
    public static OaFileSubjectTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
