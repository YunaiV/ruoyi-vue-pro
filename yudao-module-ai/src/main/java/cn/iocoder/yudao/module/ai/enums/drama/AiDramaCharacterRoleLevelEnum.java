package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧角色层级的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaCharacterRoleLevelEnum implements ArrayValuable<String> {

    S("S", "绝对主角"),
    A("A", "核心配角"),
    B("B", "重要配角"),
    C("C", "次要角色"),
    D("D", "群众演员");

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiDramaCharacterRoleLevelEnum::getLevel).toArray(String[]::new);

    /**
     * 层级
     */
    private final String level;
    /**
     * 层级名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
