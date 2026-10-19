package cn.iocoder.yudao.module.pms.enums.kb.library;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 知识库成员等级枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsKnowledgeLibraryMemberLevelEnum implements ArrayValuable<Integer> {

    CREATOR(1, "创建人"),
    ADMIN(2, "管理员"),
    MEMBER(3, "成员");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsKnowledgeLibraryMemberLevelEnum::getLevel).toArray(Integer[]::new);

    /**
     * 等级
     */
    private final Integer level;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PmsKnowledgeLibraryMemberLevelEnum valueOf(Integer level) {
        return ArrayUtil.firstMatch(item -> item.getLevel().equals(level), values());
    }

}
