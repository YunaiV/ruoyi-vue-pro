package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Schema(description = "管理后台 - 头程申请表明细新增/修改 Request VO")
@Data
public class TmsFirstMileRequestItemSaveReqVO {
    //id
    @Schema(description = "id")
    @Null(groups = Validation.OnCreate.class, message = "创建时，itemId必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，itemId不能为空")
    @DiffLogField(name = "明细ID")
    private Long id;

    @Schema(description = "产品id")
    @DiffLogField(name = "产品ID")
    private Long productId;

    @Schema(description = "FBA条码")
    @DiffLogField(name = "FBA条码")
    private String fbaBarCode;

    @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请数量不能为空")
    @DiffLogField(name = "申请数量")
    private Integer qty;

    @Schema(description = "备注")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "销售公司ID")
    @DiffLogField(name = "销售公司ID")
    private Long salesCompanyId;

    @Schema(description = "版本号")
    @NotNull(groups = Validation.OnUpdate.class, message = "版本号更新时不能为空")
    @Null(groups = Validation.OnCreate.class, message = "版本号新增需为空")
    private Integer revision;
}