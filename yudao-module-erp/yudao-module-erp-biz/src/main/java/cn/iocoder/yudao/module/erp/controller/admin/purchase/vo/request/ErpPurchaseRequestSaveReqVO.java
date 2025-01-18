package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP采购申请单新增/修改 Request VO")
@Data
public class ErpPurchaseRequestSaveReqVO {
    @Schema(description = "id")
    @Null(groups = validation.OnCreate.class, message = "创建时，id必须为空")
    @NotNull(groups = validation.OnUpdate.class, message = "更新时，id不能为空")
    private Long id;

    @Schema(description = "申请人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请人不能为空")
    private String applicant;

    @Schema(description = "申请部门")
    @NotNull(message = "申请部门不能为空")
    private String applicationDept;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单据日期不能为空")
    private LocalDateTime requestTime;

    @Schema(description = "币别id")
    private Long currencyId;

    @Schema(description = "单据编号", example = "CGDD-20250108-000027")
    private String no;

    @Schema(description = "单据标签")
    private String tag;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "收货地址")
    private String deliveryDelivery;

    @Schema(description = "商品信息")
    @NotNull(message = "商品信息不能为空")
    @NotEmpty(message = "商品信息不能为空")
    private List<@Valid ErpPurchaseRequestItemsSaveReqVO> items;
}
