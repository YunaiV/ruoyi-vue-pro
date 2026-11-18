package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;

/**
 * AI 短剧镜头视频提交记录 DO：每次提交镜头视频一条，记录提交参数、上游任务与结果，以及提交前的视频
 *
 * 提交人、提交时间、更新时间使用 {@link BaseDO} 的 creator、createTime、updateTime
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_video_submission", autoResultMap = true)
@KeySequence("ai_drama_video_submission_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaVideoSubmissionDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 请求编号：前端每次提交生成的 UUID，同一请求重复提交时据此识别
     */
    private String requestId;
    /**
     * 项目编号
     * <p>
     * 关联 {@link AiDramaProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 分镜编号
     * <p>
     * 关联 {@link AiDramaStoryboardDO#getId()}
     */
    private Long storyboardId;
    /**
     * 参数哈希：提交参数的 SHA-256，同一请求编号的参数不一致时拒绝；只存哈希，不存完整参数
     */
    private String parametersHash;
    /**
     * 视频模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
    /**
     * 视频模型标识：提交时的快照
     * <p>
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String model;
    /**
     * 本次提交的首帧地址：图生视频时为提交的首帧，参考生视频、文生视频时为空
     * <p>
     * 查询服务商时按它构建：可灵的查询路径依赖创建时是否有首帧，不能按提交后的关键帧、资产重新推导
     */
    private String firstFrameUrl;
    /**
     * 上游视频任务编号：服务商受理后返回
     */
    private String taskId;
    /**
     * 状态
     * <p>
     * 枚举 {@link AiDramaStatusEnum}：待处理为提交服务商期间，此时没有上游任务编号；进程中断后仍为待处理且没有上游任务编号的，提交结果未知
     */
    private Integer status;
    /**
     * 视频地址（文件服务）
     */
    private String videoUrl;
    /**
     * 视频尾帧地址（文件服务）
     */
    private String lastFrameUrl;
    /**
     * 失败原因
     */
    private String errorMessage;
    /**
     * 提交前的视频：提交时分镜的视频快照
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private VideoSnapshot previousVideo;

    /**
     * 视频快照
     *
     * @author 芋道源码
     */
    @Data
    public static class VideoSnapshot {

        /**
         * 上游视频任务编号
         * <p>
         * 对应 {@link AiDramaStoryboardDO#getTaskId()}
         */
        private String taskId;
        /**
         * 镜头视频状态
         * <p>
         * 枚举 {@link AiDramaStatusEnum}，对应 {@link AiDramaStoryboardDO#getVideoStatus()}
         */
        private Integer videoStatus;
        /**
         * 镜头视频地址
         * <p>
         * 对应 {@link AiDramaStoryboardDO#getVideoUrl()}
         */
        private String videoUrl;
        /**
         * 镜头视频尾帧地址
         * <p>
         * 对应 {@link AiDramaStoryboardDO#getLastFrameUrl()}
         */
        private String lastFrameUrl;

    }

}
