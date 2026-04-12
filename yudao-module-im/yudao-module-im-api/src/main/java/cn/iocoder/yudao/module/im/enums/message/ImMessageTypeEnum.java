package cn.iocoder.yudao.module.im.enums.message;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IM 消息类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ImMessageTypeEnum implements ArrayValuable<Integer> {

    TEXT(0, "文本"),
    IMAGE(1, "图片"),
    FILE(2, "文件"),
    VOICE(3, "语音"),
    VIDEO(4, "视频"),

    RECALL(10, "撤回"),
    READ(11, "已读"),
    RECEIPT(12, "回执"),
    TIP_TEXT(21, "提示文本");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImMessageTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
