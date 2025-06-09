package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 头程申请单提交审核 Request VO")
@Data
public class TmsFirstMileRequestSubmitAuditReqVO {

    @Schema(description = "头程申请单ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "头程申请单ID列表不能为空")
    private List<@NotNull(message = "头程申请单ID不能为空") Long> ids;
} 