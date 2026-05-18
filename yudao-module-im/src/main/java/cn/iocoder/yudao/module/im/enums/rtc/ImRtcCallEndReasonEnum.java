package cn.iocoder.yudao.module.im.enums.rtc;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 通话结束原因枚举；落历史消息时计算 content 文案
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImRtcCallEndReasonEnum implements ArrayValuable<Integer> {

    HANGUP(1, "通话结束"), // 接通后任一方主动挂断
    REJECT(2, "已拒绝"), // 被叫接通前点拒接
    CANCEL(3, "已取消"), // 主叫接通前主动取消
    NO_ANSWER(4, "无人接听"), // 振铃超时未接通；由参与者超时 Job 触发
    BUSY(5, "对方正忙"), // 私聊呼叫时对方在另一通话
    ERROR(9, "通话异常"); // 网络中断、设备失败等

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImRtcCallEndReasonEnum::getReason).toArray(Integer[]::new);

    /**
     * 原因值
     */
    private final Integer reason;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
