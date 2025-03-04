package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErpPurchaseRequestEnableStatusReqVO {
    //@RequestParam("requestId") Long requestId, @RequestParam("itemId") List<Long> itemIds, @RequestParam("enable") Boolean enable
    //申请单id
    @Schema(description = "申请单id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long requestId;
    @Schema(description = "申请单商品ids")
    private List<Long> itemIds;
    //@Parameter(name = "enable", description = "开启、关闭",  requiredMode = Schema.RequiredMode.REQUIRED)
    @Schema(description = "开启、关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enable;
}
