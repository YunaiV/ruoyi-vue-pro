package cn.iocoder.yudao.module.im.enums.message;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IM 消息场景枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ImMessageSceneEnum implements ArrayValuable<Integer> {

    PRIVATE(1, "私聊"),
    GROUP(2, "群聊");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImMessageSceneEnum::getScene).toArray(Integer[]::new);

    /**
     * 场景
     */
    private final Integer scene;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
