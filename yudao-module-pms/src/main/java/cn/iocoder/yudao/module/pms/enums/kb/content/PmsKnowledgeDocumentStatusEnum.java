package cn.iocoder.yudao.module.pms.enums.kb.content;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * PMS 知识库文档状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsKnowledgeDocumentStatusEnum implements ArrayValuable<Integer> {

    RECYCLED(-1, "回收站"),
    DRAFT(0, "草稿"),
    NORMAL(1, "正常"),
    TEMPLATE(2, "模板");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsKnowledgeDocumentStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 非回收站状态集合
     */
    public static final Set<Integer> ACTIVE_STATUSES = SetUtils.asSet(
            DRAFT.getStatus(), NORMAL.getStatus(), TEMPLATE.getStatus());

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PmsKnowledgeDocumentStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
