package cn.iocoder.yudao.module.ai.enums.write;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 写作类型的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiWriteFormatEnum implements IntArrayValuable {

    AUTO(1, "自动"),
    EMAIL(2, "电子邮件"),
    MESSAGE(3, "消息"),
    COMMENT(4, "评论"),
    PARAGRAPH(5, "段落"),
    ARTICLE(6, "文章"),
    BLOG_POST(7, "博客文章"),
    IDEA(8, "想法"),
    OUTLINE(9, "大纲");

    /**
     * 格式
     */
    private final Integer format;
    /**
     * 格式名
     */
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AiWriteFormatEnum::getFormat).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static AiWriteFormatEnum valueOfFormat(Integer format) {
        for (AiWriteFormatEnum formatEnum : AiWriteFormatEnum.values()) {
            if (formatEnum.getFormat().equals(format)) {
                return formatEnum;
            }
        }
        throw new IllegalArgumentException("未知格式： " + format);
    }

}
