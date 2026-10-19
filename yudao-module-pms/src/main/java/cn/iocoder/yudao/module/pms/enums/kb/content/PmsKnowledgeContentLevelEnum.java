package cn.iocoder.yudao.module.pms.enums.kb.content;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * PMS 知识内容协作等级枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PmsKnowledgeContentLevelEnum implements ArrayValuable<Integer> {

    MANAGE(1, "管理员"),
    EDIT(2, "可编辑"),
    PREVIEW(3, "仅预览"),
    DOWNLOAD(4, "可下载"),
    UPLOAD_DOWNLOAD(5, "可上传下载");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(PmsKnowledgeContentLevelEnum::getLevel).toArray(Integer[]::new);

    /**
     * 等级
     */
    private final Integer level;
    /**
     * 名称
     */
    private final String name;

    public static PmsKnowledgeContentLevelEnum valueOf(Integer level) {
        return ArrayUtil.firstMatch(item -> item.getLevel().equals(level), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean canEdit(Integer level) {
        return MANAGE.level.equals(level) || EDIT.level.equals(level) || UPLOAD_DOWNLOAD.level.equals(level);
    }

    public static boolean canDelete(Integer level) {
        return MANAGE.level.equals(level) || EDIT.level.equals(level);
    }

    public static boolean canDownload(Integer level) {
        return level != null && ObjectUtil.notEqual(PREVIEW.level, level);
    }

}
