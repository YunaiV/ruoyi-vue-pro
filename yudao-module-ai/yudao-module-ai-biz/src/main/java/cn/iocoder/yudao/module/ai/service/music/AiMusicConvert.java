package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 音乐 Convert
 *
 * @author xiaoxin
 */
public class AiMusicConvert {

    public static AiMusicDO convertFrom(SunoApi.MusicData musicData) {
        return new AiMusicDO()
                .setTaskId(musicData.id())
                .setPrompt(musicData.prompt())
                .setGptDescriptionPrompt(musicData.gptDescriptionPrompt())
                .setAudioUrl(musicData.audioUrl())
                .setVideoUrl(musicData.videoUrl())
                .setImageUrl(musicData.imageUrl())
                .setLyric(musicData.lyric())
                .setTitle(musicData.title())
                .setStatus(musicData.status())
                .setModel(musicData.modelName())
                .setTags(StrUtil.isNotBlank(musicData.tags()) ? List.of(musicData.tags().split(StrPool.COMMA)) : null);
    }

    // TODO @xin：一般情况下，不用 convert，直接逻辑里 convert 就好啦。
    public static List<AiMusicDO> convertFrom(List<SunoApi.MusicData> list) {
        // TODO @xin：可以使用 CollectionUtils.convertList 简洁一点
        return list.stream()
                .map(AiMusicConvert::convertFrom)
                .collect(Collectors.toList());
    }


}
