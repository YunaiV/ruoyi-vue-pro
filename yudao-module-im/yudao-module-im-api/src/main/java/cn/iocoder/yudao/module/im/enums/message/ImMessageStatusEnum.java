package cn.iocoder.yudao.module.im.enums.message;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 私聊消息状态枚举
 * <p>
 * 私聊：UNREAD / READ / RECALL
 * 群聊：NORMAL / RECALL
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImMessageStatusEnum implements ArrayValuable<Integer> {

    // ========== 私聊状态 ==========
    UNREAD(0, "未读"),
    READ(1, "已读"),

    // ========== 通用状态 ==========
    RECALL(2, "已撤回");

    // TODO @AI：0 这个，不是有 UNREAD 可以用了？！
    /**
     * 群聊消息的正常状态，与 UNREAD 共用值 0
     */
    public static final int NORMAL = 0;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImMessageStatusEnum::getStatus).toArray(Integer[]::new);

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