package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 短剧角色 DO
 *
 * @author 芋道源码
 */
@TableName("ai_drama_character")
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

}
