package cn.iocoder.yudao.module.oa.enums.file;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

/**
 * 云盘文件分类枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaFileCategoryEnum implements ArrayValuable<Integer> {

    IMAGE(1, "图片", new String[]{"bmp", "gif", "jpeg", "jpg", "png", "svg", "webp", "ico",
            "heic", "heif", "raw", "psd", "ai", "eps", "tif", "tiff", "avif"}),
    DOCUMENT(2, "文档", new String[]{"doc", "docx", "pdf", "txt", "rtf", "odt", "ppt", "pptx",
            "xls", "xlsx", "csv", "md", "html", "htm", "xml", "json", "yaml", "yml", "log",
            "ods", "odp", "epub"}),
    VIDEO(3, "视频", new String[]{"mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v",
            "3gp", "rm", "rmvb", "mpg", "mpeg", "ts", "m2ts"}),
    AUDIO(4, "音频", new String[]{"mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "ape",
            "amr", "mid", "midi", "opus", "aif", "aiff"}),
    ARCHIVE(5, "压缩包", new String[]{"zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso",
            "dmg", "cab", "arj", "lzh", "tgz", "tbz2", "txz", "zst"}),
    OTHER(6, "其他", new String[]{});

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaFileCategoryEnum::getCategory).toArray(Integer[]::new);

    /**
     * 分类值
     */
    private final Integer category;
    /**
     * 分类名称
     */
    private final String name;
    /**
     * 文件扩展名列表，不含点号
     */
    private final String[] extensions;

    /**
     * 根据分类值获得枚举
     *
     * @param category 分类值
     * @return 分类枚举，不存在时返回 null
     */
    public static OaFileCategoryEnum valueOf(Integer category) {
        return ArrayUtil.firstMatch(item -> item.getCategory().equals(category), values());
    }

    /**
     * 根据扩展名获得分类
     *
     * @param extension 文件扩展名，兼容前导点号和大小写
     * @return 分类枚举，无扩展名或未匹配时返回其他
     */
    public static OaFileCategoryEnum getByExtension(String extension) {
        if (StrUtil.isBlank(extension)) {
            return OTHER;
        }
        String normalizedExtension = StrUtil.removePrefix(extension.trim(), ".").toLowerCase(Locale.ROOT);
        for (OaFileCategoryEnum category : values()) {
            if (Arrays.asList(category.getExtensions()).contains(normalizedExtension)) {
                return category;
            }
        }
        return OTHER;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
