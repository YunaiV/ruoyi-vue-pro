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

    TEXT(0, "文本"), // 对应 TextMessage 类
    IMAGE(1, "图片"), // 对应 ImageMessage 类
    FILE(2, "文件"), // 对应 FileMessage 类
    VOICE(3, "语音"), // 对应 AudioMessage 类
    VIDEO(4, "视频"), // 对应 VideoMessage 类

    RECALL(10, "撤回"), // 对应 RecallMessage 类
    READ(11, "已读"), // 暂无
    RECEIPT(12, "回执"), // 暂无
    TIP_TEXT(21, "提示文本"), // 对应 TextMessage 类

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
