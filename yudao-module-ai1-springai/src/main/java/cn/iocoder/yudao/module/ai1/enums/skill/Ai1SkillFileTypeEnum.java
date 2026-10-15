package cn.iocoder.yudao.module.ai1.enums.skill;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI1 SKILL 内容节点类型的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum Ai1SkillFileTypeEnum implements ArrayValuable<Integer> {

    DIRECTORY(0, "目录"),
    FILE(1, "文件");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(Ai1SkillFileTypeEnum::getType).toArray(Integer[]::new);

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

    public static boolean isDirectory(Integer type) {
        return ObjUtil.equal(DIRECTORY.type, type);
    }

    public static boolean isFile(Integer type) {
        return ObjUtil.equal(FILE.type, type);
    }

}
