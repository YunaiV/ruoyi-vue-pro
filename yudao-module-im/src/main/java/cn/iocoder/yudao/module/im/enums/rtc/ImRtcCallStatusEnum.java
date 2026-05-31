package cn.iocoder.yudao.module.im.enums.rtc;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * IM 通话状态枚举（主表 status）
 * <p>
 * 状态机：CREATED → RUNNING → ENDED；CREATED 直接到 ENDED 表示无人接听 / 主叫取消
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImRtcCallStatusEnum implements ArrayValuable<Integer> {

    CREATED(10, "创建"), // 通话已创建；私聊：等被叫接听；群聊：发起人已进房，等其他人加入
    RUNNING(20, "进行中"), // 第一个非发起人接通后进入
    ENDED(30, "已结束"); // 任一方挂断 / 主叫 cancel / Webhook 兜底

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImRtcCallStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 活跃状态集合；通话未结束（CREATED / RUNNING）
     */
    public static final List<Integer> ACTIVE_STATUSES = ListUtil.of(CREATED.getStatus(), RUNNING.getStatus());

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isCreated(Integer status) {
        return Objects.equals(CREATED.status, status);
    }

    public static boolean isRunning(Integer status) {
        return Objects.equals(RUNNING.status, status);
    }

    public static boolean isEnded(Integer status) {
        return Objects.equals(ENDED.status, status);
    }

}
