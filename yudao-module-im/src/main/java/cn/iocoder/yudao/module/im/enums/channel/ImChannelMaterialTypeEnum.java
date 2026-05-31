package cn.iocoder.yudao.module.im.enums.channel;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IM 频道素材内容类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ImChannelMaterialTypeEnum implements ArrayValuable<Integer> {

    /**
     * 站内富文本；点击素材在客户端内置详情页拉 content 渲染
     */
    CONTENT(1, "站内富文本"),
    /**
     * 外链；点击素材跳 url 打开浏览器
     */
    LINK(2, "外链");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImChannelMaterialTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
