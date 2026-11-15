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
 * AI 短剧分镜（镜头） DO
 *
 * 镜头视频自存上游任务编号、状态与结果地址，不写 ai_video
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_storyboard", autoResultMap = true)
@KeySequence("ai_drama_storyboard_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaStoryboardDO extends BaseDO {

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
     * 采用的剧本编号
     * <p>
     * 关联 {@link AiDramaScriptDO#getId()}
     */
    private Long scriptId;
    /**
     * 场次
     */
    private Integer sceneNo;
    /**
     * 镜头标题
     */
    private String title;
    /**
     * 景别
     */
    private String shotType;
    /**
     * 运镜
     */
    private String cameraMove;
    /**
     * 预计时长（秒）
     */
    private Integer duration;
    /**
     * 视频提示词
     */
    private String videoPrompt;
    /**
     * 台词
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Dialogue> dialogues;
    // DONE @AI：为什么这种字段要对象？又不是数组 ？（已去掉 AssetRefs 对象，拆成 appearanceIds、locationId、visualAssetIds 三个字段）
    /**
     * 引用的角色形象编号
     * <p>
     * 关联 {@link AiDramaCharacterAppearanceDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> appearanceIds;
    /**
     * 引用的场景编号
     * <p>
     * 关联 {@link AiDramaLocationDO#getId()}
     */
    private Long locationId;
    /**
     * 引用的视觉资产编号
     * <p>
     * 关联 {@link AiDramaVisualAssetDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> visualAssetIds;
    /**
     * 起始帧（可选）
     */
    private String firstFrameUrl;
    /**
     * 上游视频任务编号
     *
     * 待处理（提交服务商期间）为本次的提交编号；提交、查询的回写都以它为条件，被标记失败后又重新提交时，旧的结果不再写入
     */
    private String taskId;
    /**
     * 镜头视频状态
     * <p>
     * 枚举 {@link AiDramaStatusEnum}，为空表示未生成
     */
    private Integer videoStatus;
    /**
     * 镜头视频地址（文件服务）
     */
    private String videoUrl;
    /**
     * 生成使用的视频模型编号：查询任务时按它取密钥
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long videoModelId;
    /**
     * 镜头视频失败原因
     */
    private String errorMessage;

    // DONE @AI：为什么这种字段要对象？又不是数组 ？（保留：dialogues 是数组，每句台词要同时存发言角色编号与台词内容）
    /**
     * 台词
     */
    @Data
    public static class Dialogue {

        /**
         * 发言人角色编号；旁白为空
         */
        private Long characterId;
        /**
         * 台词
         */
        private String line;

    }

}
