package cn.iocoder.yudao.module.oa.enums.discussion;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 讨论类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaDiscussionTypeEnum implements ArrayValuable<Integer> {

    ANNOUNCEMENT(1, "公告"),
    DISCUSSION(2, "讨论"),
    VOTE(3, "投票");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaDiscussionTypeEnum::getType).toArray(Integer[]::new);

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

}
