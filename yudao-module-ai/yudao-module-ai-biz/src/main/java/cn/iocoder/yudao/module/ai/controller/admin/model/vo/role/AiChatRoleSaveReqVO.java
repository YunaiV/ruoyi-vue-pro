package cn.iocoder.yudao.module.ai.controller.admin.model.vo.role;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - AI 聊天角色新增/修改 Request VO")
@Data
public class AiChatRoleSaveReqVO {

    @Schema(description = "角色编号", example = "32746")
    private Long id;

    @Schema(description = "模型编号", example = "17640")
    private Long modelId;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "角色名称不能为空")
    private String name;

    @Schema(description = "角色头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    @NotEmpty(message = "角色头像不能为空")
    private String avatar;

    @Schema(description = "角色类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "创作")
    @NotEmpty(message = "角色类别不能为空")
    private String category;

    @Schema(description = "角色排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "角色排序不能为空")
    private Integer sort;

    @Schema(description = "角色描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你说的对")
    @NotEmpty(message = "角色描述不能为空")
    private String description;

    @Schema(description = "角色欢迎语", requiredMode = Schema.RequiredMode.REQUIRED, example = "Talk is cheap, i will show code")
    @NotEmpty(message = "角色欢迎语不能为空")
    private String welcomeMessage;

    @Schema(description = "角色上下文", requiredMode = Schema.RequiredMode.REQUIRED, example = "现在开始你扮演一位程序员，你是一名优秀的程序员，具有很强的逻辑思维能力，总能高效的解决问题")
    @NotEmpty(message = "角色上下文不能为空")
    private String systemMessage;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "是否公开不能为空")
    private Boolean publicStatus;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}