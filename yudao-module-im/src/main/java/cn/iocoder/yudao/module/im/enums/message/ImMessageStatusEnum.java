package cn.iocoder.yudao.module.im.enums.message;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 消息状态枚举
 * <p>
 * 私聊：SENDING(-1, 仅客户端) / UNREAD(0) / RECALL(2) / READ(3)
 * 群聊：SENDING(-1, 仅客户端) / UNREAD(0, 作为正常状态) / RECALL(2)
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImMessageStatusEnum implements ArrayValuable<Integer> {

    SENDING(-1, "发送中"), // 仅客户端使用
    UNREAD(0, "未读"), // 私聊=未读，群聊=正常（初始状态）
    RECALL(2, "已撤回"),
    READ(3, "已读"); // 仅私聊使用；群聊已读通过 Redis 已读位置实现

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