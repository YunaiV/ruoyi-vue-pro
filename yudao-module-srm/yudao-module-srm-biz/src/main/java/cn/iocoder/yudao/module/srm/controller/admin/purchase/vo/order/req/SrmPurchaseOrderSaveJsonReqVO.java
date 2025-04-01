package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单修改Json属性 Request VO")
@Data
public class SrmPurchaseOrderSaveJsonReqVO {

    @Schema(description = "id")
    @Null(message = "修改json属性时，订单id不能为null")
    private Long id;

    @Schema(description = "订单清单列表")
    @NotNull(message = "订单项不能为空")
    @Size(min = 1, message = "商品信息至少一个")
    private List<@Valid SrmPurchaseOrderSaveJsonReqVO.Item> items;

    @Schema(description = "版本号")
    private Long version;

    @Data
    public class Item {

        @Schema(description = "订单项编号")
        @NotNull(message = "修改json属性时订单项id不能为空")
        private Long id;

        @Schema(description = "验货单json")
        private String inspectionJson;

        @Schema(description = "总验货通过数量")
        private Integer totalInspectionPassCount;

        @Schema(description = "完工单json")
        private String completionJson;

        @Schema(description = "总完工单通过数量")
        private Integer totalCompletionPassCount;

        @Schema(description = "版本号")
        private Long version;
    }

}
