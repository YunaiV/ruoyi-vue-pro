package cn.iocoder.yudao.module.ai.controller.admin.write.vo;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteLanguageEnum;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Objects;

import static cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum.*;

@Schema(description = "管理后台 - AI 写作生成 Request VO")
@Data
public class AiWriteGenerateReqVO {

    @Schema(description = "写作场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "写作场景不能为空")
    @InEnum(value = AiWriteTypeEnum.class, message = "写作场景必须是 {value}")
    private Integer type;

    @Schema(description = "模型编号，必须是启用的对话模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型不能为空")
    private Long modelId;

    @Schema(description = "写作主题 / 要求 / 回复要点 / 补充说明（按场景）", example = "田忌赛马")
    private String prompt;

    @Schema(description = "原文 / 本周完成事项（按场景）", example = "领导我要辞职")
    private String originalContent;

    @Schema(description = "长度", example = "1")
    private Integer length;

    @Schema(description = "格式", example = "1")
    private Integer format;

    @Schema(description = "语气", example = "1")
    private Integer tone;

    @Schema(description = "输出语言", example = "1")
    private Integer language;

    @Schema(description = "场景参数（周报、小红书、润色 / 改写、文本翻译使用）")
    @Valid
    private SceneParams sceneParams;

    @Schema(description = "AI 写作场景参数")
    @Data
    public static class SceneParams {

        // ========== 周报 ==========

        @Schema(description = "周期", example = "2026-11-09 ~ 2026-11-13")
        @Size(max = 50, message = "周期不能超过 50 个字")
        private String period;

        @Schema(description = "岗位 / 团队", example = "后端开发")
        @Size(max = 50, message = "岗位 / 团队不能超过 50 个字")
        private String role;

        // ========== 小红书 ==========

        @Schema(description = "目标受众", example = "刚入职的程序员")
        @Size(max = 100, message = "目标受众不能超过 100 个字")
        private String audience;

        @Schema(description = "风格", example = "种草")
        @Pattern(regexp = "种草|测评|攻略|日常分享", message = "风格必须是 种草 / 测评 / 攻略 / 日常分享")
        private String style;

        @Schema(description = "是否使用 emoji", example = "true")
        private Boolean emoji;

        @Schema(description = "话题标签数", example = "5")
        @Min(value = 0, message = "话题标签数不能小于 0")
        @Max(value = 10, message = "话题标签数不能大于 10")
        private Integer tagCount;

        // ========== 润色 / 改写 ==========

        @Schema(description = "处理方式", example = "润色")
        @Pattern(regexp = "润色|改写|精简|扩写", message = "处理方式必须是 润色 / 改写 / 精简 / 扩写")
        private String mode;

        // ========== 文本翻译 ==========

        @Schema(description = "源语言，1 表示自动检测", example = "1")
        @InEnum(value = AiWriteLanguageEnum.class, message = "源语言必须是 {value}")
        private Integer sourceLanguage;

        @Schema(description = "目标语言", example = "3")
        @InEnum(value = AiWriteLanguageEnum.class, message = "目标语言必须是 {value}")
        private Integer targetLanguage;

        @Schema(description = "领域", example = "通用")
        @Pattern(regexp = "通用|技术|法律|医学|商务|文学", message = "领域必须是 通用 / 技术 / 法律 / 医学 / 商务 / 文学")
        private String domain;

        @Schema(description = "是否保留格式（换行、列表、Markdown、代码、链接）", example = "true")
        private Boolean preserveFormat;

    }

    // ========== 按场景校验必填参数 ==========

    @AssertTrue(message = "写作要求不能为空")
    @JsonIgnore
    public boolean isPromptValid() {
        return !isType(WRITING, REPLY, GENERAL, XIAOHONGSHU) || StrUtil.isNotBlank(prompt);
    }

    @AssertTrue(message = "原文不能为空")
    @JsonIgnore
    public boolean isOriginalContentValid() {
        return !isType(REPLY, WEEKLY_REPORT, REWRITE, TRANSLATE) || StrUtil.isNotBlank(originalContent);
    }

    @AssertTrue(message = "格式不能为空")
    @JsonIgnore
    public boolean isFormatValid() {
        return !isType(WRITING, REPLY, GENERAL) || format != null;
    }

    @AssertTrue(message = "语气不能为空")
    @JsonIgnore
    public boolean isToneValid() {
        return isType(XIAOHONGSHU) || tone != null;
    }

    @AssertTrue(message = "输出语言、长度不能为空")
    @JsonIgnore
    public boolean isLanguageAndLengthValid() {
        return !isType(WRITING, REPLY, GENERAL, WEEKLY_REPORT, XIAOHONGSHU) || (language != null && length != null);
    }

    @AssertTrue(message = "目标受众、风格、是否使用 emoji、话题标签数不能为空")
    @JsonIgnore
    public boolean isXiaohongshuValid() {
        return !isType(XIAOHONGSHU) || (sceneParams != null && StrUtil.isNotBlank(sceneParams.getAudience())
                && sceneParams.getStyle() != null && sceneParams.getEmoji() != null && sceneParams.getTagCount() != null);
    }

    @AssertTrue(message = "处理方式不能为空")
    @JsonIgnore
    public boolean isRewriteValid() {
        return !isType(REWRITE) || (sceneParams != null && sceneParams.getMode() != null);
    }

    @AssertTrue(message = "源语言、目标语言、领域、是否保留格式不能为空")
    @JsonIgnore
    public boolean isTranslateValid() {
        return !isType(TRANSLATE) || (sceneParams != null && sceneParams.getSourceLanguage() != null
                && sceneParams.getTargetLanguage() != null && sceneParams.getDomain() != null
                && sceneParams.getPreserveFormat() != null);
    }

    @AssertTrue(message = "目标语言不能是自动，也不能与源语言相同")
    @JsonIgnore
    public boolean isTargetLanguageValid() {
        if (!isType(TRANSLATE) || sceneParams == null || sceneParams.getTargetLanguage() == null) {
            return true;
        }
        return !Objects.equals(sceneParams.getTargetLanguage(), AiWriteLanguageEnum.AUTO.getLanguage())
                && !Objects.equals(sceneParams.getTargetLanguage(), sceneParams.getSourceLanguage());
    }

    private boolean isType(AiWriteTypeEnum... types) {
        return ArrayUtil.contains(types, AiWriteTypeEnum.valueOfType(type));
    }

}
