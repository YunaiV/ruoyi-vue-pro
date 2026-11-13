package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.image.AiImageInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiImageModeEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 绘画 DO
 *
 * @author fansili
 */
@TableName(value = "ai_image", autoResultMap = true)
@KeySequence("ai_image_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiImageDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;

    /**
     * 提示词
     */
    private String prompt;
    /**
     * 不希望出现的内容（反向提示词）
     *
     * Midjourney 拼接为 --no；硅基流动映射为 negative_prompt
     */
    private String negativePrompt;
    /**
     * 输入图片：参考图、融图、换脸、cref、sref、蒙版
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<InputImage> inputImages;

    /**
     * 平台
     *
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 生成方式
     *
     * 枚举 {@link AiImageModeEnum}
     */
    private Integer mode;
    /**
     * 模型编号
     *
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
    /**
     * 模型标识
     *
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String model;
    /**
     * 提交时使用的 API 密钥编号，后续的按钮动作、同步沿用
     *
     * 关联 {@link AiApiKeyDO#getId()}
     */
    private Long apiKeyId;

    /**
     * 图片宽度
     */
    private Integer width;
    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 生成状态
     *
     * 枚举 {@link AiImageStatusEnum}
     */
    private Integer status;
    /**
     * 进度，0-100
     */
    private Integer progress;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 绘画错误信息
     */
    private String errorMessage;

    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 是否公开
     */
    private Boolean publicStatus;

    /**
     * 通用模型的绘制参数
     *
     * 只用于 OpenAI、通义、硅基、Stability 等通用模型
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Options options;
    /**
     * Midjourney 的绘制参数
     *
     * 只用于 platform = {@link AiPlatformEnum#MIDJOURNEY}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private MidjourneyOptions midjourneyOptions;
    /**
     * Seedream 的绘制参数
     *
     * 只用于 platform = {@link AiPlatformEnum#BYTEDANCE}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private SeedreamOptions seedreamOptions;

    /**
     * 上游服务商的任务编号
     *
     * 1. midjourney proxy：关联的 task id，不唯一（提交返回 21 时会复用已有任务）
     */
    private String taskId;
    /**
     * 来源记录编号：Midjourney 按钮动作、局部重绘的来源
     *
     * 关联 {@link AiImageDO#getId()}
     */
    private Long parentId;

    /**
     * 输入图片
     *
     * @author 芋道源码
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputImage {

        // TODO DONE @AI：搞到枚举类里把；（已收敛到 AiImageInputRoleEnum）
        /**
         * 角色
         *
         * 枚举 {@link AiImageInputRoleEnum}
         */
        private String role;
        /**
         * 图片地址
         */
        private String url;

    }

    /**
     * 通用模型的绘制参数
     *
     * 只用于 OpenAI、通义、硅基、Stability 等通用模型
     *
     * @author 芋道源码
     */
    @Data
    public static class Options {

        /**
         * 画质：OpenAI quality
         */
        private String quality;
        /**
         * 用户所选比例：只用于回填，不发给上游
         */
        private String aspectRatio;
        /**
         * 随机种子：硅基 seed；Stability seed
         */
        private Long seed;
        /**
         * 推理步数：硅基 num_inference_steps；Stability steps
         */
        private Integer steps;
        /**
         * 引导系数：硅基 guidance_scale；Stability cfg_scale
         *
         * 历史数据的键为 scale，只读兼容
         */
        @JsonAlias("scale")
        private Double cfgScale;
        /**
         * 采样器：Stability sampler
         */
        private String sampler;
        /**
         * 风格预设：Stability style_preset
         */
        private String stylePreset;
        /**
         * CLIP 引导：Stability clip_guidance_preset
         */
        private String clipGuidancePreset;
        /**
         * 历史 OpenAI DALL·E 风格：上游请求不支持，新记录不写，只为历史详情保留
         */
        private String style;

        /**
         * 历史未知键：读取时原样保留，写回时原样输出；从不发往上游
         */
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private Map<String, Object> extra;

        @JsonAnyGetter
        public Map<String, Object> getExtra() {
            return extra;
        }

        @JsonAnySetter
        public void putExtra(String key, Object value) {
            if (extra == null) {
                extra = new LinkedHashMap<>();
            }
            extra.put(key, value);
        }

    }

    /**
     * Midjourney 的绘制参数
     *
     * @author 芋道源码
     */
    @Data
    public static class MidjourneyOptions {

        // ========== 请求参数 ==========

        /**
         * 版本参数，例如说 --v 7、--niji 6
         */
        private String version;
        /**
         * 比例：--ar
         */
        private String aspectRatio;
        /**
         * 画质：--q
         */
        private Double quality;
        /**
         * 风格化：--s
         */
        private Integer stylize;
        /**
         * 创意度：--c
         */
        private Integer chaos;
        /**
         * 随机种子：--seed；为空表示随机
         */
        private Long seed;
        /**
         * 平铺：--tile
         */
        private Boolean tile;
        /**
         * RAW：--raw
         */
        private Boolean raw;
        /**
         * HD：--hd
         */
        private Boolean hd;
        /**
         * 图片权重：--iw
         */
        private Double imageWeight;
        /**
         * 角色权重：--cw
         */
        private Integer characterWeight;
        /**
         * 风格权重：--sw
         */
        private Integer styleWeight;
        /**
         * 风格版本：--sv
         */
        private Integer styleVersion;
        /**
         * 融图尺寸：PORTRAIT / SQUARE / LANDSCAPE
         */
        private String dimensions;
        /**
         * 执行的按钮：按钮动作、局部重绘
         */
        private String customId;

        // ========== 任务元数据：只由服务端写，从不发往中转 ==========

        /**
         * 实际提交的完整提示词
         */
        private String finalPrompt;
        /**
         * 中转返回的按钮
         */
        private List<MidjourneyApi.Button> buttons;
        /**
         * 按钮动作的名称，例如说 U2、V3
         */
        private String actionLabel;
        /**
         * 中转的任务状态，例如说 SUBMITTED、IN_PROGRESS、MODAL
         */
        private String providerStatus;
        /**
         * 局部重绘的中转流程（提交时的快照）
         *
         * 枚举 {@link MidjourneyApi.ModalProtocolEnum}
         */
        private String modalProtocol;
        /**
         * 是否由历史平铺 options 迁移而来
         */
        private Boolean legacy;

        /**
         * 历史未知键：读取时原样保留，写回时原样输出；从不发往中转
         */
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private Map<String, Object> extra;

        @JsonAnyGetter
        public Map<String, Object> getExtra() {
            return extra;
        }

        @JsonAnySetter
        public void putExtra(String key, Object value) {
            if (extra == null) {
                extra = new LinkedHashMap<>();
            }
            extra.put(key, value);
        }

    }

    /**
     * Seedream 的绘制参数
     *
     * @author 芋道源码
     */
    @Data
    public static class SeedreamOptions {

        // ========== 请求参数：实际发送的值 ==========

        /**
         * 尺寸档位，例如说 2K
         */
        private String size;
        /**
         * 提示词优化模式，例如说 standard
         */
        private String optimizePromptMode;
        /**
         * 是否添加水印
         */
        private Boolean watermark;

        // ========== 任务元数据：只由服务端写 ==========

        /**
         * 结果图片的尺寸，例如说 2048x2048
         */
        private String resultSize;
        /**
         * 用量
         */
        private Map<String, Object> usage;
        /**
         * 上游错误码；错误信息写入 {@link AiImageDO#getErrorMessage()}
         */
        private String errorCode;

    }

}
