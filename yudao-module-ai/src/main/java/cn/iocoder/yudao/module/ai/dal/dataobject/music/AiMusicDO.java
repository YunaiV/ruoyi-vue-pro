package cn.iocoder.yudao.module.ai.dal.dataobject.music;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateModeEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 音乐 DO
 *
 * 一首歌一行：中转一次任务返回 N 首，就是 N 行，共享 {@link #taskId}；唯一键 (task_id, clip_id) 防止重复拆分
 *
 * @author xiaoxin
 */
@TableName(value = "ai_music", autoResultMap = true)
@KeySequence("ai_music_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiMusicDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 生成模式
     * <p>
     * 枚举 {@link AiMusicGenerateModeEnum}
     */
    private Integer generateMode;
    /**
     * 平台
     * <p>
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
    /**
     * 模型标识（mv），冗余 {@link AiModelDO#getModel()}
     */
    private String model;
    /**
     * API 密钥编号（提交时绑定）
     * <p>
     * 关联 {@link AiApiKeyDO#getId()}；查询、续写、拼接都按它访问中转，不跟随模型之后的关联变化
     */
    private Long apiKeyId;

    /**
     * 音乐名称
     */
    private String title;
    /**
     * 歌曲描述（灵感模式）
     */
    private String prompt;
    /**
     * 歌词
     */
    private String lyric;
    /**
     * 音乐风格，多个使用英文逗号分隔
     */
    private String tags;
    /**
     * 是否纯音乐
     */
    private Boolean makeInstrumental;
    /**
     * 来源音乐编号（续写、拼接）
     * <p>
     * 关联 {@link AiMusicDO#getId()}
     */
    private Long refMusicId;
    /**
     * 续写起点（秒）
     */
    private Integer extendSeconds;

    /**
     * 封面地址
     */
    private String coverUrl;
    /**
     * 音频地址
     */
    private String audioUrl;
    /**
     * 视频地址：只记录中转返回值，不转存，可能为空
     */
    private String videoUrl;
    /**
     * 时长（秒）
     */
    private Double duration;

    /**
     * 状态
     * <p>
     * 枚举 {@link AiMusicStatusEnum}
     */
    private Integer status;
    /**
     * 进度（0-100）
     */
    private Integer progress;
    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 是否公开
     */
    private Boolean publicStatus;
    /**
     * 播放次数
     */
    private Integer playCount;

    /**
     * 供应商任务编号
     */
    private String taskId;
    /**
     * 供应商歌曲编号；占位行拆分前为空
     */
    private String clipId;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

}
