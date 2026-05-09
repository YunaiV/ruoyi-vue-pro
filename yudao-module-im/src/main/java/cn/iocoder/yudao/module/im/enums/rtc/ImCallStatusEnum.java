package cn.iocoder.yudao.module.im.enums.rtc;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

// TODO @AI：是否一定要有 status 状态的记录？？？？【可以在调研下】
/**
 * IM 通话状态枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImCallStatusEnum implements ArrayValuable<Integer> {

    INVITING(10, "邀请中"), // 邀请中：1v1 主叫等待被叫接听
    ONGOING(20, "通话中"), // 通话中：至少一名被叫已接听；群通话从发起人加入即进入此状态
    ENDED(30, "已结束"); // 已结束：会话即将销毁；写入消息流后从内存移除

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImCallStatusEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isInviting(Integer type) {
        return Objects.equals(INVITING.type, type);
    }

    public static boolean isOngoing(Integer type) {
        return Objects.equals(ONGOING.type, type);
    }

    public static boolean isEnded(Integer type) {
        return Objects.equals(ENDED.type, type);
    }

}
