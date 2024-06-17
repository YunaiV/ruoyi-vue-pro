package cn.iocoder.yudao.module.ai.dal.dataobject.music;

import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author xiaoxin
 * @Date 2024/6/5
 */
@TableName("ai_music")
@Data
public class AiMusicDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "用户编号")
    private Long userId;

    @Schema(description = "音乐名称")
    private String title;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "歌词")
    private String lyric;

    @Schema(description = "音频地址")
    private String audioUrl;

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "音乐状态")
    private String status;

    @Schema(description = "描述词")
    private String gptDescriptionPrompt;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "模型")
    private String model;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "音乐风格标签")
    private String tags;

    @Schema(description = "任务id")
    private String taskId;



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
                .setTags(musicData.tags());
    }

    public static List<AiMusicDO> convertFrom(List<SunoApi.MusicData> musicDataList) {
        return musicDataList.stream()
                .map(AiMusicDO::convertFrom)
                .collect(Collectors.toList());
    }


}
