package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 获取产品可用库存 Request VO")
@Data
public class TmsFirstMileRequestProductStockReqVO {

    @Schema(description = "部门编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "部门编号不能为空")
    private Long deptId;

    @Schema(description = "产品编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "产品编号列表不能为空")
    private List<Long> productIds;

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "国家不能为空")
    private String country;

} 