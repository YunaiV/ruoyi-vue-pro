package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 库存调拨单新增/修改 Request VO")
@Data
public class ErpStockMoveSaveReqVO {

    @Schema(description = "调拨编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
    private Long id;

    @Schema(description = "客户编号", example = "3113")
    private Long customerId;

    @Schema(description = "调拨时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "调拨时间不能为空")
    private LocalDateTime moveTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "附件 URL", example = "https://www.iocoder.cn/1.doc")
    private String fileUrl;

    @Schema(description = "调拨项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "调拨项列表不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "调拨项编号", example = "11756")
        private Long id;

        @Schema(description = "调出仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "调出仓库编号不能为空")
        private Long fromWarehouseId;

        @Schema(description = "调入仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "888")
        @NotNull(message = "调入仓库编号不能为空")
        private Long toWarehouseId;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "产品单价", example = "100.00")
        private BigDecimal productPrice;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "产品数量不能为空")
        private BigDecimal count;

        @Schema(description = "备注", example = "随便")
        private String remark;

        @AssertTrue(message = "调出、调仓仓库不能相同")
        @JsonIgnore
        public boolean isWarehouseValid() {
            return ObjectUtil.notEqual(fromWarehouseId, toWarehouseId);
        }

    }

}