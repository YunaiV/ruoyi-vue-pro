package cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 敏感词创建/修改 Request VO")
@Data
public class SensitiveWordSaveVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "敏感词", requiredMode = Schema.RequiredMode.REQUIRED, example = "敏感词")
    @NotNull(message = "敏感词不能为空")
    private String name;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED, example = "短信,评论")
    @NotNull(message = "标签不能为空")
    private List<String> tags;

    @Schema(description = "状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "描述", example = "污言秽语")
    private String description;

}
