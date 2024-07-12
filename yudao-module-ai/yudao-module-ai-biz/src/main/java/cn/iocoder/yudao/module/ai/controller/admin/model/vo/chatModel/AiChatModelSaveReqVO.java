package cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatModel;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - API 聊天模型新增/修改 Request VO")
@Data
public class AiChatModelSaveReqVO {

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
    private String platform;

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

}