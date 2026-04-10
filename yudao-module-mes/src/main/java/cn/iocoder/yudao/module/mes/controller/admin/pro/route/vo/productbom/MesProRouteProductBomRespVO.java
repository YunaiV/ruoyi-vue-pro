package cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.productbom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 工艺路线产品 BOM Response VO")
@Data
public class MesProRouteProductBomRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long routeId;

    @Schema(description = "工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long processId;

    @Schema(description = "产品物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @Schema(description = "BOM 物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "BOM 物料编码")
    private String itemCode;

    @Schema(description = "BOM 物料名称")
    private String itemName;

    @Schema(description = "规格型号")
    private String specification;

    @Schema(description = "单位名称")
    private String unitName;

    @Schema(description = "用料比例", example = "1.50")
    private BigDecimal quantity;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
