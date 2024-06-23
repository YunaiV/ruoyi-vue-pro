package cn.iocoder.yudao.module.ai.dal.dataobject.music;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.Data;

import java.util.List;

/**
 * @Author xiaoxin
 * @Date 2024/6/5
 */
@TableName("ai_music")
@Data
public class AiMusicDO extends BaseDO {

    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 音乐名称
     */
    private String title;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 歌词
     */
    private String lyric;

    /**
     * 音频地址
     */
    private String audioUrl;

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 音乐状态
     * <p>
     * 枚举 {@link AiMusicStatusEnum}
     */
    private String status;

    /**
     * 描述词
     */
    private String gptDescriptionPrompt;
    /**
     * 提示词
     */
    private String prompt;

    /**
     * 生成模式
     */
    private String generateMode;

    /**
     * 平台
     * <p>
     * 枚举 {@link cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum}
     */
    private String platform;

    /**
     * 模型
     */
    private String model;

    /**
     * 错误信息
     */
    private String errorMessage;


    /**
     * 音乐风格标签
     */
    @TableField(typeHandler = AiMusicTagsHandler.class)
    private List<String> tags;

    /**
     * 任务编号
     */
    private String taskId;


    public static class AiMusicTagsHandler extends AbstractJsonTypeHandler<Object> {

        @Override
        protected Object parse(String json) {
            return JsonUtils.parseArray(json, String.class);
        }

        @Override
        protected String toJson(Object obj) {
            return JsonUtils.toJsonString(obj);
        }
    }
}
