package cn.iocoder.yudao.module.promotion.enums.combination;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 拼团状态枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum CombinationRecordStatusEnum implements IntArrayValuable {

    WAITING(0, "未付款"),
    IN_PROGRESS(1, "进行中"),
    SUCCESS(2, "拼团成功"),
    FAILED(3, "拼团失败");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CombinationRecordStatusEnum::getStatus).toArray();

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

    public static boolean isSuccess(Integer status) {
        return ObjectUtil.equal(status, SUCCESS.getStatus());
    }

}
