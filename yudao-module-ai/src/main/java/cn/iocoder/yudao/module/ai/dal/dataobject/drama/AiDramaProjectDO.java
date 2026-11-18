package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAspectRatioEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaProjectStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStyleEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTransitionTypeEnum;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 短剧项目 DO
 *
 * 只保留最近一次成片的结果
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_project", autoResultMap = true)
@KeySequence("ai_drama_project_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaProjectDO extends BaseDO {

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
     * 项目名称
     */
    private String name;
    /**
     * 项目简介：AI 润色故事想法的结果，也作为生成剧本的输入
     */
    private String description;
    /**
     * 故事想法
     */
    private String idea;
    /**
     * 画风
     * <p>
     * 枚举 {@link AiDramaStyleEnum}
     */
    private Integer style;
    /**
     * 审美技能编号：出图时使用的视觉风格技能；为空表示不使用
     * <p>
     * 关联 {@link AiSkillDO#getId()}
     */
    private Long aestheticSkillId;
    /**
     * 导演技能编号：导演风格技能；为空表示不使用
     * <p>
     * 关联 {@link AiSkillDO#getId()}
     */
    private Long directorSkillId;
    /**
     * 分镜技能编号数组：规划、审阅分镜时按顺序使用
     * <p>
     * 关联 {@link AiSkillDO#getId()}
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Long> storyboardSkillIds;
    /**
     * 项目状态
     * <p>
     * 枚举 {@link AiDramaProjectStatusEnum}
     */
    private Integer status;
    /**
     * 是否公开：只有成片成功后才能公开
     */
    private Boolean publicStatus;

    /**
     * 画幅，默认 9:16：项目的设置，用于生成镜头视频与成片
     * <p>
     * 枚举 {@link AiDramaAspectRatioEnum}
     */
    private String composeAspectRatio;
    /**
     * 转场类型
     * <p>
     * 枚举 {@link AiDramaTransitionTypeEnum}
     */
    private Integer composeTransitionType;
    /**
     * 转场时长（秒）
     */
    private Double composeTransitionDurationSeconds;
    /**
     * 最近一次成片的状态
     * <p>
     * 枚举 {@link AiDramaStatusEnum}，为空表示未合成
     */
    private Integer composeStatus;
    /**
     * 合成任务编号：每次提交合成生成一个；异步合成的最终回写以它为条件，被标记失败后又重新合成时，旧的执行结果不再写入
     */
    private String composeTaskId;
    /**
     * 合成进度，0 ~ 100
     */
    private Integer composeProgress;
    /**
     * 成片地址（文件服务）
     */
    private String composeFileUrl;
    /**
     * 成片时长（秒）
     */
    private Double composeDuration;
    /**
     * 成片镜头数：成片实际拼接的镜头数，合成时可只选部分镜头
     */
    private Integer composeStoryboardCount;
    /**
     * 合成失败原因
     */
    private String composeErrorMessage;
    /**
     * 最近一次合成完成时间
     */
    private LocalDateTime composeFinishTime;
    /**
     * 成片使用的镜头视频：按拼接顺序，合成成功时记录提交合成时的镜头与视频地址；
     * 用于判断成片是否需要重新合成（镜头视频被替换、镜头被删除等），为空表示没有记录（不判断）
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<ComposeVideo> composeVideos;

    /**
     * 成片使用的镜头视频
     *
     * @author 芋道源码
     */
    @Data
    public static class ComposeVideo {

        /**
         * 镜头编号
         * <p>
         * 关联 {@link AiDramaStoryboardDO#getId()}
         */
        private Long storyboardId;
        /**
         * 合成时的镜头视频地址
         */
        private String videoUrl;
        /**
         * 镜头所属的剧本编号：判断剧本版本的分镜是否已变化
         * <p>
         * 关联 {@link AiDramaScriptDO#getId()}
         */
        private Long scriptId;
        /**
         * 提交合成时项目最大的镜头编号（包括未选、视频未完成的镜头）：同一次合成相同
         * <p>
         * 判断「合成后新增镜头」的边界：编号更大的镜头为提交后新增，完成视频后成片需要重新合成；
         * 镜头编号自增，不受创建时间精度影响；只作边界比较，该镜头之后被删除时仍然有效
         * <p>
         * 关联 {@link AiDramaStoryboardDO#getId()}
         */
        private Long maxStoryboardId;

    }

}
