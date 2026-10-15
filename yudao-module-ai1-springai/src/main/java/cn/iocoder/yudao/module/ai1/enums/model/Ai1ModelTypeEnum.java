package cn.iocoder.yudao.module.ai1.enums.model;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI1 模型类型的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum Ai1ModelTypeEnum implements ArrayValuable<Integer> {

    CHAT(0, "对话"),
    EMBEDDING(1, "嵌入");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(Ai1ModelTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isChat(Integer type) {
        return ObjUtil.equal(CHAT.type, type);
    }

    public static boolean isEmbedding(Integer type) {
        return ObjUtil.equal(EMBEDDING.type, type);
    }

}
