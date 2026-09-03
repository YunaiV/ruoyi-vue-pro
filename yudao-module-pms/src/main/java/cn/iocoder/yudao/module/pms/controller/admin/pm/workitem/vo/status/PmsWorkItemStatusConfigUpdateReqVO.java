package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 更新工作项状态配置 Request VO")
@Data
public class PmsWorkItemStatusConfigUpdateReqVO {

    @Schema(description = "状态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "状态编号不能为空")
    private Long id;

    @Schema(description = "状态名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "待验收")
    @NotBlank(message = "状态名称不能为空")
    @Size(max = 50, message = "状态名称不能超过 50 个字符")
    private String name;

    @Schema(description = "语义状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "语义状态不能为空")
    @InEnum(PmsWorkItemStatusTypeEnum.class)
    private Integer statusType;

    @Schema(description = "状态描述", example = "需求已拆分，等待开发")
    @Size(max = 255, message = "状态描述不能超过 255 个字符")
    private String description;

}
