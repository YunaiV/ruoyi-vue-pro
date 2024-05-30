package cn.iocoder.yudao.module.promotion.enums.kehu;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 消息类型枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum MessageTypeEnum implements IntArrayValuable {

    MESSAGE(1, "普通消息"),
    PICTURE(2, "图片消息"),
    VOICE(3, "语音消息"),
    GOODS(4, "商品消息"),
    ORDER(5, "订单消息"),
    VIDEO(6, "视频消息");

    private static final int[] ARRAYS = Arrays.stream(values()).mapToInt(MessageTypeEnum::getType).toArray();

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
