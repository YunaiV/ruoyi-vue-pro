package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.util.List;

/**
 * AI 短剧角色形象版本 DO
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_character_appearance", autoResultMap = true)
@KeySequence("ai_drama_character_appearance_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaCharacterAppearanceDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 角色编号
     * <p>
     * 关联 {@link AiDramaCharacterDO#getId()}
     */
    private Long characterId;
    // DONE @AI：需要这个字段么？appearance？或者使用 version？这个 selectedIndex 字段会影响到么？（保留：它是角色下「形象 N」的展示序号，素材、分镜引用、生成任务标题都用它；与 selectedIndex（候选图下标）无关；不改名 version，避免误解为历史版本）
    /**
     * 形象序号，从 1 开始：角色下的第几个形象，用于展示「形象 N」
     */
    private Integer appearanceIndex;
    /**
     * 形象设定
     */
    private String description;
    /**
     * 出图提示词
     */
    private String prompt;
    /**
     * 候选图：最后一项可能是生成中、已失败的任务（url 为空），用于重新提交时取模型
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Candidate> candidates;
    /**
     * 选用的候选下标
     */
    private Integer selectedIndex;
    /**
     * 出图任务编号：每次提交出图生成一个；异步出图的最终回写以它为条件，被标记失败后又重新提交时，旧的执行结果不再写入
     */
    private String taskId;
    /**
     * 最近一次生成的状态
     * <p>
     * 枚举 {@link AiDramaStatusEnum}，为空表示未生成
     */
    private Integer status;
    /**
     * 生成失败原因
     */
    private String errorMessage;

    /**
     * 候选图：角色形象、场景共用
     */
    @Data
    public static class Candidate {

        /**
         * 图片地址（文件服务）；生成中、已失败时为空
         */
        private String url;
        /**
         * 描述：生成时的提示词
         */
        private String description;
        // DONE @AI：关联字段；（已补充关联 AiModelDO）
        /**
         * 生成使用的绘画模型编号
         * <p>
         * 关联 {@link AiModelDO#getId()}
         */
        private Long modelId;

    }

}
