package cn.iocoder.yudao.module.im.enums.message;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IM 群消息回执状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ImGroupMessageReceiptStatusEnum implements ArrayValuable<Integer> {

    NO_RECEIPT(0, "不需要回执"),
    PENDING(1, "待完成"),
    DONE(2, "已完成");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImGroupMessageReceiptStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
