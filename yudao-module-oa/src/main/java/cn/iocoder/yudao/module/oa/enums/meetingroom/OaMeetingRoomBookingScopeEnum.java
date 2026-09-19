package cn.iocoder.yudao.module.oa.enums.meetingroom;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 会议室可预定范围枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaMeetingRoomBookingScopeEnum implements ArrayValuable<Integer> {

    ALL(0, "全部成员"),
    SPECIFIED(1, "指定成员");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaMeetingRoomBookingScopeEnum::getScope).toArray(Integer[]::new);

    /**
     * 会议室可预定范围
     */
    private final Integer scope;
    /**
     * 名称
     */
    private final String name;

    public static OaMeetingRoomBookingScopeEnum valueOf(Integer scope) {
        return ArrayUtil.firstMatch(item -> item.getScope().equals(scope), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
