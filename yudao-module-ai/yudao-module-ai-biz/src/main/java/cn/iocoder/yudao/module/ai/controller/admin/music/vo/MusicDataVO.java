package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示单个音乐数据的类
 */
@Data
public class MusicDataVO {
    /**
     * 音乐数据的 ID
     */
    private String id;

    /**
     * 音乐音频的标题
     */
    private String title;

    /**
     * 音乐音频的图片 URL
     */
    @JsonProperty("image_url")
    private String imageUrl;

    /**
     * 音乐音频的歌词
     */
    private String lyric;

    /**
     * 音乐音频的 URL
     */
    @JsonProperty("audio_url")
    private String audioUrl;

    /**
     * 音乐视频的 URL
     */
    @JsonProperty("video_url")
    private String videoUrl;

    /**
     * 音乐音频的创建时间
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * 生成音乐音频的提示
     */
    private String prompt;

    /**
     * 音乐音频的风格
     */
    private String style;

    public static List<MusicDataVO> convertFrom(List<SunoApi.SunoResp.MusicData> musicDataList) {
        return musicDataList.stream().map(musicData -> {
            MusicDataVO musicDataVO = new MusicDataVO();
            musicDataVO.setId(musicData.id());
            musicDataVO.setTitle(musicData.title());
            musicDataVO.setImageUrl(musicData.imageUrl());
            musicDataVO.setLyric(musicData.lyric());
            musicDataVO.setAudioUrl(musicData.audioUrl());
            musicDataVO.setVideoUrl(musicData.videoUrl());
            musicDataVO.setCreatedAt(musicData.createdAt());
            musicDataVO.setModel(musicData.model());
            musicDataVO.setPrompt(musicData.prompt());
            musicDataVO.setStyle(musicData.style());
            return musicDataVO;
        }).collect(Collectors.toList());
    }
}