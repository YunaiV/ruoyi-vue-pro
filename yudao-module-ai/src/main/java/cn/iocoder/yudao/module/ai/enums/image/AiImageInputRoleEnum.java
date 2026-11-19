package cn.iocoder.yudao.module.ai.enums.image;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 绘图输入图片角色的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiImageInputRoleEnum implements ArrayValuable<String> {

    REFERENCE("reference", "参考图"),
    BLEND("blend", "融图"),
    SWAP_SOURCE("swap_source", "换脸来源"),
    SWAP_TARGET("swap_target", "换脸目标"),
    CREF("cref", "角色参考"),
    SREF("sref", "风格参考"),
    MASK("mask", "蒙版");

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiImageInputRoleEnum::getRole).toArray(String[]::new);

    /**
     * 角色
     */
    private final String role;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
