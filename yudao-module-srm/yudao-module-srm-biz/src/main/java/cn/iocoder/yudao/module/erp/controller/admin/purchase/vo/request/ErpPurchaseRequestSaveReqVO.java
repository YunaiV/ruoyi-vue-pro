package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP采购申请单新增/修改 Request VO")
@Data
public class ErpPurchaseRequestSaveReqVO {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "32561")
    private Long id;

    @Schema(description = "申请人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请人不能为空")
    private String applicant;

    @Schema(description = "申请部门")
    private String applicationDept;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单据日期不能为空")
    private LocalDateTime requestTime;

    @Schema(description = "商品信息")
    @NotNull(message = "商品信息不能为空")
    @NotEmpty(message = "商品信息不能为空")
    private List<@Valid ErpPurchaseRequestItemsSaveReqVO> items;

}