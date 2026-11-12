package cn.iocoder.yudao.module.ai.enums.ppt;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI PPT 导出格式的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiPptExportFormatEnum implements ArrayValuable<String> {

    PPTX("pptx", "PPTX"),
    PDF("pdf", "PDF");

    /**
     * 格式，也是导出文件的扩展名
     */
    private final String format;
    /**
     * 格式名
     */
    private final String name;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiPptExportFormatEnum::getFormat).toArray(String[]::new);

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
