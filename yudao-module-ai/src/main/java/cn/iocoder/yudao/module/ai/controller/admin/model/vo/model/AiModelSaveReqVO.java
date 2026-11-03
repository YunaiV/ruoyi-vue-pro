package cn.iocoder.yudao.module.ai.controller.admin.model.vo.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiModelInputModalityEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - API 模型新增/修改 Request VO")
@Data
public class AiModelSaveReqVO {

    @Schema(description = "编号", example = "2630")
    private Long id;

    @Schema(description = "API 秘钥编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22042")
    @NotNull(message = "API 秘钥编号不能为空")
    private Long keyId;

    @Schema(description = "模型名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "模型名字不能为空")
    private String name;

    @Schema(description = "模型标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "gpt-3.5-turbo-0125")
    @NotEmpty(message = "模型标识不能为空")
    private String model;

    @Schema(description = "模型平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "OpenAI")
    @NotEmpty(message = "模型平台不能为空")
    @InEnum(AiPlatformEnum.class)
    private String platform;

    @Schema(description = "模型类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型类型不能为空")
    @InEnum(AiModelTypeEnum.class)
    private Integer type;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @InEnum(CommonStatusEnum.class)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "温度参数", example = "1")
    private Double temperature;

    @Schema(description = "单条回复的最大 Token 数量", example = "4096")
    private Integer maxTokens;

    @Schema(description = "上下文的最大 Message 数量", example = "8192")
    private Integer maxContexts;

    @Schema(description = "输入模态数组", example = "[\"text\", \"image\"]")
    @InEnum(AiModelInputModalityEnum.class)
    private List<String> inputModalities;

    @Schema(description = "思考配置；null 或 levels 为 [] 表示不配置，修改时清空", example = "{\"levels\":[{\"level\":\"off\",\"options\":{\"thinking\":{\"type\":\"disabled\"}}}],\"defaultLevel\":\"off\"}")
    @Valid
    private ReasoningConfig reasoningConfig;

    /**
     * 思考配置
     */
    @Schema(name = "AiModelReasoningConfigSaveReqVO", description = "管理后台 - AI 模型思考配置")
    @Data
    public static class ReasoningConfig {

        @Schema(description = "档位数组，从低到高；[] 表示清空", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "思考档位数组不能为空，清空请传 []")
        @Valid
        private List<@NotNull(message = "思考档位不能为空") ReasoningLevel> levels;

        @Schema(description = "默认档位，必须在档位数组中；清空时不传", example = "off")
        private String defaultLevel;

        /**
         * 校验档位值唯一
         */
        @AssertTrue(message = "思考档位值不能重复")
        @JsonIgnore
        public boolean isLevelsUnique() {
            if (CollUtil.isEmpty(levels)) {
                return true;
            }
            Set<String> names = new HashSet<>();
            for (ReasoningLevel level : levels) {
                if (level != null && level.getLevel() != null && !names.add(level.getLevel())) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 校验默认档位：清空时不能提交，配置时必须在档位数组中
         */
        @AssertTrue(message = "默认思考档位必须在档位数组中；清空时不能提交默认档位")
        @JsonIgnore
        public boolean isDefaultLevelValid() {
            if (CollUtil.isEmpty(levels)) {
                return StrUtil.isEmpty(defaultLevel);
            }
            if (StrUtil.isEmpty(defaultLevel)) {
                return false;
            }
            for (ReasoningLevel level : levels) {
                if (level != null && defaultLevel.equals(level.getLevel())) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * 思考档位
     */
    @Schema(name = "AiModelReasoningLevelSaveReqVO", description = "管理后台 - AI 模型思考档位")
    @Data
    public static class ReasoningLevel {

        @Schema(description = "档位值，off 表示关闭思考", requiredMode = Schema.RequiredMode.REQUIRED, example = "off")
        @NotNull(message = "思考档位值不能为空")
        @Pattern(regexp = "^[a-z0-9_-]{1,32}$", message = "思考档位值只能包含小写字母、数字、下划线、中划线，长度 1~32")
        private String level;

        @Schema(description = "思考参数，JSON 对象，任意层级都不允许 null", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "{\"thinking\":{\"type\":\"disabled\"}}")
        @NotNull(message = "思考参数不能为空")
        private Map<String, Object> options;

        /**
         * 校验思考参数不包含 null：对象的值、数组的元素都不允许为 null
         */
        @AssertTrue(message = "思考参数不能包含 null")
        @JsonIgnore
        public boolean isOptionsNonNull() {
            return options == null || isNonNull(options);
        }

        private static boolean isNonNull(Object value) {
            if (value == null) {
                return false;
            }
            if (value instanceof Map) {
                for (Object item : ((Map<?, ?>) value).values()) {
                    if (!isNonNull(item)) {
                        return false;
                    }
                }
            } else if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (!isNonNull(item)) {
                        return false;
                    }
                }
            }
            return true;
        }

    }

}