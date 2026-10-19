package cn.iocoder.yudao.module.oa.enums.meetingroom;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 会议提醒类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaMeetingRoomReminderTypeEnum implements ArrayValuable<Integer> {

    NONE(1, "不提醒", null),
    BEFORE_FIVE_MINUTES(2, "提前 5 分钟", 5),
    BEFORE_TEN_MINUTES(3, "提前 10 分钟", 10),
    BEFORE_FIFTEEN_MINUTES(4, "提前 15 分钟", 15),
    BEFORE_THIRTY_MINUTES(5, "提前 30 分钟", 30);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaMeetingRoomReminderTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 会议提醒类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;
    /**
     * 提前提醒分钟数，为 null 时不提醒
     */
    private final Integer minutes;

    /**
     * 根据类型获得枚举
     *
     * @param type 类型
     * @return 对应枚举，不存在时返回 null
     */
    public static OaMeetingRoomReminderTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
