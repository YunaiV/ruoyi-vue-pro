package cn.iocoder.yudao.module.im.enums.rtc;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * IM 通话参与者状态枚举（明细表 status）
 * <p>
 * 终态闭合：通话 ENDED 时所有明细 status 必属 {LEFT / REJECTED / NO_ANSWER}
 * <p>
 * 1、INVITING → JOINED → LEFT（接通后挂断 / 离开）；
 * 2、INVITING → REJECTED（接通前点拒接）；
 * 3、INVITING → NO_ANSWER（通话结束仍未应答）
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImRtcParticipantStatusEnum implements ArrayValuable<Integer> {

    INVITING(10, "邀请中"), // 已发出 invite，等被叫响应
    JOINED(20, "已加入"), // 已 connect 进 LiveKit 房间
    REJECTED(30, "已拒绝"), // 接通前点拒接
    NO_ANSWER(40, "未应答"), // 通话已结束仍未应答；endSession 批量改
    LEFT(50, "已离开"); // 接通后挂断 / Webhook 兜底

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImRtcParticipantStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 活跃状态集合；尚未离开通话（INVITING / JOINED）
     */
    public static final List<Integer> ACTIVE_STATUSES = ListUtil.of(INVITING.getStatus(), JOINED.getStatus());

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

    public static boolean isInviting(Integer status) {
        return Objects.equals(INVITING.status, status);
    }

    public static boolean isJoined(Integer status) {
        return Objects.equals(JOINED.status, status);
    }

}
