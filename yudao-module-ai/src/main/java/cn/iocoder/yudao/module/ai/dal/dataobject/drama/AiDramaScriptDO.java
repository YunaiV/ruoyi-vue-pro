package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaScriptCreationModeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaScriptSourceEnum;

/**
 * AI 短剧剧本 DO
 *
 * 同一项目可有多条，最新一条为当前剧本
 *
 * @author 芋道源码
 */
@TableName("ai_drama_script")
@KeySequence("ai_drama_script_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaScriptDO extends BaseDO {

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
     * 剧本标题
     */
    private String title;
    /**
     * 叙事基调
     */
    private String tone;
    /**
     * 剧情大纲：公开作品作为剧情简介
     */
    private String outline;
    /**
     * 剧本正文
     */
    private String content;
    /**
     * 剧本来源
     * <p>
     * 枚举 {@link AiDramaScriptSourceEnum}
     */
    private Integer source;
    /**
     * 创作模式：生成、打磨剧本时的创作方式
     * <p>
     * 枚举 {@link AiDramaScriptCreationModeEnum}
     */
    private Integer creationMode;
    /**
     * 创作资料：生成、打磨剧本时参考的原始资料
     */
    private String sourceMaterial;
    /**
     * 世界观设定：生成、打磨剧本时使用
     */
    private String worldbuilding;
    /**
     * 修订要求：本版剧本当前的制作与修订要求，修改时整体替换；之前的要求保留在旧版本剧本中
     */
    private String revisionNote;

}
