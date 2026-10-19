package cn.iocoder.yudao.module.oa.enums.meetingroom;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 会议室预定使用状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaMeetingRoomUseStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待使用"),
    IN_USE(1, "使用中"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaMeetingRoomUseStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 会议室预定使用状态
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
    public static OaMeetingRoomUseStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
