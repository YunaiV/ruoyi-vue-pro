package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaSceneTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.util.List;
import java.util.Map;

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
     * 镜头序号
     */
    private Integer sceneNo;
    /**
     * 镜头标题
     */
    private String title;
    /**
     * 剧本原文：本镜对应的剧本片段
     */
    private String sourceText;
    /**
     * 画面描述：本镜画面内容的文字说明，用于展示
     */
    private String sceneText;
    /**
     * 场景类型
     * <p>
     * 枚举 {@link AiDramaSceneTypeEnum}
     */
    private Integer sceneType;
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
     * 摄影规则：光线、景深、构图、色调等，JSON 对象
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> photographyRules;
    /**
     * 表演指导：逐个出镜角色的表演指令
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ActingNote> actingNotes;
    /**
     * 图片提示词：生成起始帧图片使用
     */
    private String imagePrompt;
    /**
     * 视频提示词
     */
    private String videoPrompt;
    /**
     * 台词正文：按顺序的发言人与台词
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Dialogue> dialogues;
    /**
     * 出场角色：每个角色引用一个形象，并指定站位
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CharacterRef> characters;
    /**
     * 引用的场景编号
     * <p>
     * 关联 {@link AiDramaLocationDO#getId()}
     */
    private Long locationId;
    /**
     * 本镜显式指定的道具编号：优先于道具的默认关联 {@link AiDramaVisualAssetDO.ReferenceImages#getShotNumbers()}
     *
     * 1. 为空（null）：未指定，按道具的默认关联确定本镜道具
     * 2. 空数组：明确本镜没有道具
     * <p>
     * 关联 {@link AiDramaVisualAssetDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> visualAssetIds;
    /**
     * 连续性设置：逐镜可选的生成设置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Continuity continuity;
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
     * 镜头视频尾帧地址（文件服务）：供下一镜承接
     */
    private String lastFrameUrl;
    /**
     * 镜头的视频模型编号：编辑时可预设，生成时优先使用；也记录生成使用的，查询任务时按它取密钥
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long videoModelId;
    /**
     * 视频模型标识：生成时的快照
     * <p>
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String videoModel;
    /**
     * 生成使用的分辨率，例如 720p、768P（大小写按模型）：重新提交时沿用；空串表示不传
     */
    private String videoResolution;
    /**
     * 镜头视频失败原因
     */
    private String errorMessage;

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

    /**
     * 表演指导
     *
     * @author 芋道源码
     */
    @Data
    public static class ActingNote {

        /**
         * 角色名
         */
        private String name;
        /**
         * 表演指令：本镜该角色的神态、动作与情绪
         */
        private String acting;

    }

    /**
     * 出场角色引用
     */
    @Data
    public static class CharacterRef {

        /**
         * 角色形象编号：角色通过形象反查，不另存角色编号
         * <p>
         * 关联 {@link AiDramaCharacterAppearanceDO#getId()}
         */
        private Long appearanceId;
        /**
         * 站位：取自场景的可站位置
         * <p>
         * 对应 {@link AiDramaLocationDO#getAvailableSlots()} 中的值
         */
        private String slot;

    }

    /**
     * 连续性设置
     */
    @Data
    public static class Continuity {

        /**
         * 参考音频的发言角色编号：显式覆盖；为空时按 {@link AiDramaStoryboardDO#getDialogues()} 的发言角色推导
         * <p>
         * 关联 {@link AiDramaCharacterDO#getId()}
         */
        private List<Long> voiceSpeakerIds;
        /**
         * 手动起始帧：设置时优先作为本镜起始帧；未设置时使用本镜生效的关键帧
         * <p>
         * 关键帧为 {@link AiDramaVisualAssetDO} 中 shot_frame 类型、{@link AiDramaVisualAssetDO#getStoryboardId()} 为本镜的记录
         */
        private ManualStartFrame manualStartFrame;
        /**
         * 是否使用起始帧生成视频：缺省 true；关闭时即使有起始帧，也按参考生视频或文生视频
         */
        private Boolean useFirstFrame;
        /**
         * 视频参考图是否包含场景图：缺省 true
         */
        private Boolean includeLocation;
        /**
         * 视频参考模式，例如 multi_shot（一段连续表演包含多个剪辑镜头）；为空按单镜
         */
        private String videoReferenceMode;
        /**
         * 视频参考图镜像：key 为本镜原参考图地址，value 为提交服务商时替换使用的地址
         */
        private Map<String, String> videoReferenceMirrors;
        /**
         * 动作参考视频地址：参考生视频时作为参考视频
         */
        private List<String> referenceVideoUrls;
        /**
         * 素材图片地址（文件服务）：有素材时直接渲染为镜头视频
         */
        private List<String> materialUrls;

        // ========== 关键帧设置 ==========

        /**
         * 关键帧画面焦点：审阅后指定的焦点角色名，优先于自动推断
         */
        private String frameFocus;
        /**
         * 关键帧导演指令：生成关键帧时追加的构图要求
         */
        private String keyframeDirective;
        /**
         * 关键帧参考模式，例如 text_only_candidate（纯文字重画候选，不传参考图）；为空按参考图生成
         */
        private String keyframeReferenceMode;

        // ========== 分镜规划 ==========

        /**
         * 原文场次：本镜所属剧本原文的场次，同一场内不因地点、时间变化递增
         * <p>
         * 区别于 {@link AiDramaStoryboardDO#getSceneNo()}：后者为本镜在剧本中的序号
         */
        private Integer sceneNumber;
        /**
         * 原文场景上下文：本镜所属原文场的背景摘要
         */
        private String sourceSceneContext;
        /**
         * 片段序号：场内的片段序号
         */
        private Integer segmentNumber;
        /**
         * 片段目标：当前片段唯一的剧情任务
         */
        private String segmentGoal;
        /**
         * 片段结果：当前片段结束时的剧情变化
         */
        private String segmentResult;
        /**
         * 承接上一片段的桥梁
         */
        private String bridgeIn;
        /**
         * 引向下一片段的桥梁
         */
        private String bridgeOut;
        /**
         * 剧情职责：setup、trigger、reaction、decision、action、result、transition 之一
         */
        private String beatType;
        /**
         * 叙事起因
         */
        private String narrativeCause;
        /**
         * 角色目标
         */
        private String characterGoal;
        /**
         * 本镜行动
         */
        private String storyAction;
        /**
         * 行动结果
         */
        private String storyResult;
        /**
         * 下一镜钩子
         */
        private String nextHook;
        /**
         * 起始状态：本镜 0 秒的画面状态
         */
        private String startState;
        /**
         * 结束状态：本镜结束时的画面状态
         */
        private String endState;
        /**
         * 衔接动作：承接上一镜的连续动作
         */
        private String continuityAction;
        /**
         * 空间锚点：本镜的空间方位参照
         */
        private String spatialAnchor;
        /**
         * 0 秒在画面中的角色名
         */
        private List<String> presentCharacters;
        /**
         * 背景群演：匿名群演的文字描述，例如人数、服饰、位置、姿态
         */
        private String backgroundExtras;
        /**
         * 拍法设计：焦点、景别、轴线、运动、转场、状态变化等，JSON 对象
         */
        private Map<String, Object> shotDesign;
        /**
         * 表演节拍：逐个出镜角色的表演，规划阶段产出
         * <p>
         * 区别于 {@link AiDramaStoryboardDO#getActingNotes()}：后者为独立的表演指导结果
         */
        private List<ActingNote> performanceBeats;
        /**
         * 时长分配：台词读音、语速、动作与停顿秒数等，JSON 对象
         */
        private Map<String, Object> timing;

    }

    /**
     * 手动起始帧
     *
     * @author 芋道源码
     */
    @Data
    public static class ManualStartFrame {

        /**
         * 图片地址（文件服务）
         */
        private String url;

    }

}
