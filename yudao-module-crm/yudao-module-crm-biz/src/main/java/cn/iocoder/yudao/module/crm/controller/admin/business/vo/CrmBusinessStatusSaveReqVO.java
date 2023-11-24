package cn.iocoder.yudao.module.crm.controller.admin.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 商机状态新增/修改 Request VO")
@Data
public class CrmBusinessStatusSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23899")
    private Long id;

    @Schema(description = "状态类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7139")
    @NotNull(message = "状态类型编号不能为空")
    private Long typeId;

    @Schema(description = "状态名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "状态名不能为空")
    private String name;

    @Schema(description = "赢单率")
    private String percent;

    @Schema(description = "排序")
    private Integer sort;

}
