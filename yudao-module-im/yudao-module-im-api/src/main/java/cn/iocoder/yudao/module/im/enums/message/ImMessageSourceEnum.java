package cn.iocoder.yudao.module.im.enums.message;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

// TODO @AI：还需要这个枚举么？
/**
 * IM 消息的消息来源
 */
@RequiredArgsConstructor
@Getter
public enum ImMessageSourceEnum implements ArrayValuable<Integer> {

    USER_SEND(100, "用户发送"),
    SYSTEM_SEND(200, "系统发送");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImMessageSourceEnum::getSource).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer source;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}