package cn.iocoder.yudao.module.im.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * IM 会话类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImConversationTypeEnum implements ArrayValuable<Integer> {

    NONE(0, "无会话"), // 无会话
    PRIVATE(1, "私聊"), // 私聊
    GROUP(2, "群聊"), // 群聊
    CHANNEL(3, "频道"); // 频道 / 公众号

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImConversationTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isNone(Integer type) {
        return Objects.equals(NONE.type, type);
    }

    public static boolean isPrivate(Integer type) {
        return Objects.equals(PRIVATE.type, type);
    }

    public static boolean isGroup(Integer type) {
        return Objects.equals(GROUP.type, type);
    }

    public static boolean isChannel(Integer type) {
        return Objects.equals(CHANNEL.type, type);
    }

}
