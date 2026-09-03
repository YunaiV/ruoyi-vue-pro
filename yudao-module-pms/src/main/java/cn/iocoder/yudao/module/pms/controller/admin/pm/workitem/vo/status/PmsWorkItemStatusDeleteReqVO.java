package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - PMS 删除工作项状态 Request VO")
@Data
public class PmsWorkItemStatusDeleteReqVO {

    @Schema(description = "待删除状态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "待删除状态不能为空")
    private Long id;

    @Schema(description = "工作项迁入状态编号；待删除状态存在工作项时必填", example = "2048")
    private Long transferStatusId;

}
