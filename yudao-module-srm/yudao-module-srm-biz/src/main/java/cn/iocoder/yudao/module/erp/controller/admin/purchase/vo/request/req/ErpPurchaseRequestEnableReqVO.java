package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - ERP采购申请单子开/关 Request VO")
@Data
@Builder
public class ErpPurchaseRequestEnableReqVO {

    @Schema(description = "申请单商品ids")
    @NotNull(message = "申请单商品ids不能为空")
    private List<Long> itemIds;
    //@Parameter(name = "enable", description = "开启、关闭",  requiredMode = Schema.RequiredMode.REQUIRED)
    @Schema(description = "开启、关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开启、关闭不能为空")
    private Boolean enable;
}
