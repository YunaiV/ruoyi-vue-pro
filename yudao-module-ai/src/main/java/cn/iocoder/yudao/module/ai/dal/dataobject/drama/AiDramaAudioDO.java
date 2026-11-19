package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;

import java.util.List;

/**
 * AI 短剧音频 DO：角色样音、旁白、对白、配乐、参考音频
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_audio", autoResultMap = true)
@KeySequence("ai_drama_audio_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaAudioDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     * <p>
     * 关联 {@link AiDramaProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 角色编号：角色样音时为样音所属的角色；旁白、对白为空（对白的发言角色按分镜的台词确定）
     * <p>
     * 关联 {@link AiDramaCharacterDO#getId()}
     */
    private Long characterId;
    /**
     * 类型
     * <p>
     * 枚举 {@link AiDramaAudioTypeEnum}
     */
    private Integer type;
    /**
     * 名称
     */
    private String name;
    /**
     * 文案：合成本条音频的文本
     */
    private String text;
    /**
     * 音色：合成本条音频使用的 TTS 音色
     */
    private String voice;
    /**
     * 分镜编号：对白时为所属的分镜；其它类型为空（配乐、参考音频见 {@link #storyboardIds}）
     * <p>
     * 关联 {@link AiDramaStoryboardDO#getId()}
     */
    private Long storyboardId;
    /**
     * 时长（秒）：保留小数，配乐、参考音频、角色样音按实际时长裁剪与混音
     */
    private Double duration;
    /**
     * 文件地址（文件服务）
     */
    private String fileUrl;
    /**
     * 使用的分镜编号数组：配乐、参考音频使用；为空表示未选用（候选）
     * <p>
     * 关联 {@link AiDramaStoryboardDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> storyboardIds;
    /**
     * 音量：0 ~ 1，配乐混音使用
     */
    private Double volume;
    /**
     * 起播位置（秒）：从音频的第几秒开始使用
     */
    private Double startOffset;
    /**
     * AI 音乐编号：本项目生成的配乐使用，生成中为占位的那首，选用后为选中的那首；同批生成的其它版本由 AI 音乐记录提供；上传的音频为空
     * <p>
     * 关联 {@link AiMusicDO#getId()}
     */
    private Long musicId;

}
