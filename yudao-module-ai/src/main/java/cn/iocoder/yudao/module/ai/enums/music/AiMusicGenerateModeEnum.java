package cn.iocoder.yudao.module.ai.enums.music;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 音乐生成模式的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiMusicGenerateModeEnum implements ArrayValuable<Integer> {

    /**
     * 灵感模式：歌曲描述 + 是否纯音乐
     */
    INSPIRATION(1, "灵感模式"),
    /**
     * 自定义模式：歌词 + 风格 + 名称 + 是否纯音乐
     */
    CUSTOM(2, "自定义模式"),
    /**
     * 续写：自定义参数 + 来源作品的 task_id、clip_id + 续写起点
     */
    EXTEND(3, "续写"),
    /**
     * 拼接：把续写作品与原曲合并为完整歌曲
     */
    CONCAT(4, "拼接"),
    /**
     * 上传：用户上传的音频，生成后可作为续写来源
     */
    UPLOAD(5, "上传");

    /**
     * 模式
     */
    private final Integer mode;
    /**
     * 模式名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiMusicGenerateModeEnum::getMode).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
