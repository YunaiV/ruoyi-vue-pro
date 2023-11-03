package cn.iocoder.yudao.module.promotion.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 促销活动的状态枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum PromotionActivityStatusEnum implements IntArrayValuable {

    WAIT(10, "未开始"),
    RUN(20, "进行中"),
    END(30, "已结束"),
    CLOSE(40, "已关闭");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PromotionActivityStatusEnum::getStatus).toArray();

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
