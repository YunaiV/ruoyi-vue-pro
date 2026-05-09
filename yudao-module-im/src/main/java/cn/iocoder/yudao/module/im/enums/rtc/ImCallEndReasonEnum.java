package cn.iocoder.yudao.module.im.enums.rtc;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 通话结束原因枚举；用于落历史消息时计算 content 文案
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImCallEndReasonEnum implements ArrayValuable<Integer> {

    // TODO @AI：这个对齐的哪个东西；
    // TODO @AI：尾注释；
    /** 正常挂断：双方在通话中后由任一方主动挂断 */
    HANGUP(1, "通话结束"),
    /** 拒绝：被叫接通前点拒接 */
    REJECT(2, "已拒绝"),
    /** 取消：主叫接通前主动取消 */
    CANCEL(3, "已取消"),
    /** 超时：振铃 30 秒未应答 */
    TIMEOUT(4, "未接通"),
    /** 对方忙线 */
    BUSY(5, "对方正忙"),
    /** 异常：网络中断、设备失败等 */
    ERROR(9, "通话异常");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImCallEndReasonEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
