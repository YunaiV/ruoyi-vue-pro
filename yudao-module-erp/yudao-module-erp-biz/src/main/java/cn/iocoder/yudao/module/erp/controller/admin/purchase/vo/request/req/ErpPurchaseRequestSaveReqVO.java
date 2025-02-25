package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP采购申请单新增/修改 Request VO")
@Data
public class ErpPurchaseRequestSaveReqVO {
    @Schema(description = "id")
    @Null(groups = validation.OnCreate.class, message = "创建时，申请单id必须为空")
    @NotNull(groups = validation.OnUpdate.class, message = "更新时，申请单id不能为空")
    private Long id;

    @Schema(description = "申请人id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请人不能为空")
    private Long applicantId;

    @Schema(description = "申请部门id")
    @NotNull(message = "申请部门不能为空")
    private Long applicationDeptId;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单据日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime requestTime;

    @Schema(description = "单据编号", example = "CGDD-20250108-000027")
    private String no;

    @Schema(description = "单据标签")
    private String tag;

    @Schema(description = "供应商id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @Schema(description = "收货地址")
    private String delivery;

    @Schema(description = "商品信息")
    @NotNull(message = "商品信息不能为空")
    @Size(min = 1, message = "商品信息至少一个")
    private List<@Valid ErpPurchaseRequestItemsSaveReqVO> items;
}

