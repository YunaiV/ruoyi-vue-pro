package cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 模型分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1ModelPageReqVO extends PageParam {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商编号不能为空")
    private Long providerId;

    @Schema(description = "展示名称，模糊匹配", example = "DeepSeek")
    private String name;

    @Schema(description = "类型", example = "0")
    private Integer type;

}
