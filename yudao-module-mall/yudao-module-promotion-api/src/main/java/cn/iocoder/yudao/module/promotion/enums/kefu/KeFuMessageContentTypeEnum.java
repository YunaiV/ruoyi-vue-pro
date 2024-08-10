package cn.iocoder.yudao.module.promotion.enums.kefu;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 客服消息的类型枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum KeFuMessageContentTypeEnum implements IntArrayValuable {

    TEXT(1, "文本消息"),
    IMAGE(2, "图片消息"),
    VOICE(3, "语音消息"),
    VIDEO(4, "视频消息"),
    SYSTEM(5, "系统消息"),

    // ========== 商城特殊消息 ==========
    PRODUCT(10, "商品消息"),
    ORDER(11, "订单消息");

    private static final int[] ARRAYS = Arrays.stream(values()).mapToInt(KeFuMessageContentTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
