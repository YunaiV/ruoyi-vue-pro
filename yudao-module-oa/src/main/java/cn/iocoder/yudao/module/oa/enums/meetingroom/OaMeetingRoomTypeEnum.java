package cn.iocoder.yudao.module.oa.enums.meetingroom;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 会议室类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaMeetingRoomTypeEnum implements ArrayValuable<Integer> {

    INTERNAL(1, "内部会议室"),
    EXTERNAL(2, "外部会议室"),
    TRAINING(3, "培训室"),
    MULTIFUNCTION(4, "多功能厅");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaMeetingRoomTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 会议室类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    /**
     * 根据类型获得枚举
     *
     * @param type 类型
     * @return 对应枚举，不存在时返回 null
     */
    public static OaMeetingRoomTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
