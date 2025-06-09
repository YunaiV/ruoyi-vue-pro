package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 头程单 - 库存查询 Response VO
 *
 * @author wdy
 */
@Schema(description = "管理后台 - 头程单库存查询 Response VO")
@Data
public class TmsFirstMileStockRespVO {

    // ========== 产品信息 ==========

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long productId;

    // ========== 仓库信息 ==========

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long warehouseId;

    // ========== 公司信息 ==========

    @Schema(description = "公司编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long companyId;

    @Schema(description = "公司名称")
    private String companyName;

    // ========== 入库信息 ==========

    @Schema(description = "入库部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long inboundDeptId;

    @Schema(description = "入库公司编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long inboundCompanyId;

    @Schema(description = "入库公司名称")
    private String inboundCompanyName;

    @Schema(description = "实际入库量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer actualQty;

    @Schema(description = "计划入库量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer planQty;

    @Schema(description = "已上架量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer shelveClosedQty;

    @Schema(description = "批次号", requiredMode = Schema.RequiredMode.REQUIRED, example = "BATCH001")
    private String batchNo;

    @Schema(description = "入库时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime inboundTime;

    @Schema(description = "入库单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "IN001")
    private String inboundCode;

    // ========== 库位信息 ==========

    @Schema(description = "仓位可用库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer binAvailableQty;

    @Schema(description = "仓位可售库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "90")
    private Integer binSellableQty;

    @Schema(description = "仓位待出库库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer binOutboundPendingQty;


} 