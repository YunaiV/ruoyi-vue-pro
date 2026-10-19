package cn.iocoder.yudao.module.oa.enums.meetingroom;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 会议室可用状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaMeetingRoomStatusEnum implements ArrayValuable<Integer> {

    NORMAL(0, "正常"),
    MAINTENANCE(1, "维修中"),
    DISABLED(2, "不可用");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaMeetingRoomStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 会议室可用状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    /**
     * 根据状态获得枚举
     *
     * @param status 状态
     * @return 对应枚举，不存在时返回 null
     */
    public static OaMeetingRoomStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
