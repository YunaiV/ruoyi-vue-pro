package cn.iocoder.yudao.module.im.enums.friend;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * IM 好友关系状态（合并「是否好友」+「是否拉黑」两态）
 * <p>
 * 用 state 而非 status：避免和 ImFriendDO 的 status 物理字段（CommonStatusEnum：ENABLE / DISABLE）混淆。
 * <p>
 * 用于 ImFriendService.getFriendState 返回值与 FRIEND_STATE 缓存值：私聊发送热点路径下，sender 调缓存即可同时判定「能否发」和「是否屏蔽接收方」
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImFriendStateEnum implements ArrayValuable<Integer> {

    NONE(0, "非好友 / 已删除"),
    FRIEND(1, "好友"),
    BLOCKED(2, "好友且已被对方拉黑");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImFriendStateEnum::getState).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer state;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isNone(Integer state) {
        return Objects.equals(NONE.state, state);
    }

    public static boolean isFriend(Integer state) {
        return Objects.equals(FRIEND.state, state);
    }

    public static boolean isBlocked(Integer state) {
        return Objects.equals(BLOCKED.state, state);
    }

}
