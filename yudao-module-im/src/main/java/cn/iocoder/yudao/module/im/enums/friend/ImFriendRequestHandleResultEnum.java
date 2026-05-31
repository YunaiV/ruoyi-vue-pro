package cn.iocoder.yudao.module.im.enums.friend;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * IM 好友申请处理结果枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImFriendRequestHandleResultEnum implements ArrayValuable<Integer> {

    UNHANDLED(0, "未处理"),
    AGREED(1, "同意"),
    REFUSED(2, "拒绝");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImFriendRequestHandleResultEnum::getResult).toArray(Integer[]::new);

    /**
     * 结果
     */
    private final Integer result;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 判断申请是否还未处理
     */
    public static boolean isUnhandled(Integer result) {
        return Objects.equals(UNHANDLED.result, result);
    }

}
