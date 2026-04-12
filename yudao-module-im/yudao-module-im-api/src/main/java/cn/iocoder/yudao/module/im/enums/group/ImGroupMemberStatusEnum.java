package cn.iocoder.yudao.module.im.enums.group;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @AI：要不使用 commonstatusenum 替代掉这个；
/**
 * IM 群成员状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ImGroupMemberStatusEnum implements ArrayValuable<Integer> {

    NORMAL(0, "正常"),
    QUIT(1, "已退出");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImGroupMemberStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
