package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaCharacterCostumeTierEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaCharacterRoleLevelEnum;

import java.util.List;

/**
 * AI 短剧角色 DO
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_character", autoResultMap = true)
@KeySequence("ai_drama_character_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaCharacterDO extends BaseDO {

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
     * 角色名称
     */
    private String name;
    /**
     * 别名、称呼，多个以逗号分隔：用于识别剧本、分镜中的角色
     */
    private String aliases;
    /**
     * 角色层级
     * <p>
     * 枚举 {@link AiDramaCharacterRoleLevelEnum}
     */
    private String roleLevel;
    /**
     * 性别
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.system.enums.common.SexEnum}
     */
    private Integer sex;
    /**
     * 年龄段，例如「约二十五岁」「成年」
     */
    private String ageRange;
    /**
     * 性格标签数组
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> personalityTags;
    /**
     * 服装华丽度
     * <p>
     * 枚举 {@link AiDramaCharacterCostumeTierEnum}
     */
    private Integer costumeTier;
    /**
     * 角色设定
     */
    private String description;
    /**
     * 角色出图提示词
     */
    private String prompt;
    /**
     * 角色参考图
     */
    private String referenceImageUrl;
    /**
     * 声音描述：自然语言描述角色的声音，例如「三十岁男声，低沉、语速偏慢」，生成角色样音时使用
     */
    private String voiceDescription;
    /**
     * 样音文本：生成角色样音时朗读的正文
     */
    private String sampleText;
    /**
     * 当前选用的角色样音编号：为空表示未选
     * <p>
     * 关联 {@link AiDramaAudioDO#getId()}
     */
    private Long sampleAudioId;

}
