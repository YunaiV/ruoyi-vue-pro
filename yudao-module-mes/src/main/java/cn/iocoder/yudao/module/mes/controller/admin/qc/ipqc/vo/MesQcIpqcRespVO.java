package cn.iocoder.yudao.module.mes.controller.admin.qc.ipqc.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 过程检验单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesQcIpqcRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "检验单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "IPQC20250101001")
    @ExcelProperty("检验单编号")
    private String code;

    @Schema(description = "检验单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "XX工单过程检验")
    @ExcelProperty("检验单名称")
    private String name;

    @Schema(description = "IPQC 检验类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("检验类型")
    private Integer type;

    @Schema(description = "检验模板 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long templateId;

    // ========== 来源单据 ==========

    @Schema(description = "来源单据类型", example = "304")
    private Integer sourceDocType;

    @Schema(description = "来源单据 ID", example = "200")
    private Long sourceDocId;

    @Schema(description = "来源单据行 ID", example = "300")
    private Long sourceLineId;

    @Schema(description = "来源单据编号", example = "FB20250101001")
    private String sourceDocCode;

    // ========== 生产关联 ==========

    @Schema(description = "生产工单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long workOrderId;

    @Schema(description = "工单编号", example = "WO20250101001")
    @ExcelProperty("工单编号")
    private String workOrderCode;

    @Schema(description = "工单名称", example = "生产工单A")
    private String workOrderName;

    @Schema(description = "生产任务 ID", example = "20")
    private Long taskId;

    @Schema(description = "工位 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Long workstationId;

    @Schema(description = "工位名称", example = "工位A")
    @ExcelProperty("工位名称")
    private String workstationName;

    @Schema(description = "工序 ID", example = "40")
    private Long processId;

    @Schema(description = "工序名称", example = "组装")
    @ExcelProperty("工序名称")
    private String processName;

    // ========== 物料 ==========

    @Schema(description = "产品物料 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Long itemId;

    @Schema(description = "产品物料编码", example = "ITEM001")
    @ExcelProperty("产品物料编码")
    private String itemCode;

    @Schema(description = "产品物料名称", example = "物料A")
    @ExcelProperty("产品物料名称")
    private String itemName;

    @Schema(description = "规格型号", example = "100mm*50mm")
    private String itemSpecification;

    @Schema(description = "单位名称", example = "个")
    private String unitName;

    // ========== 数量 ==========

    @Schema(description = "检测数量", example = "100")
    @ExcelProperty("检测数量")
    private BigDecimal checkQuantity;

    @Schema(description = "合格品数量", example = "95")
    @ExcelProperty("合格品数量")
    private BigDecimal qualifiedQuantity;

    @Schema(description = "不合格品数量", example = "5")
    @ExcelProperty("不合格品数量")
    private BigDecimal unqualifiedQuantity;

    @Schema(description = "工废数量", example = "2")
    @ExcelProperty("工废数量")
    private BigDecimal laborScrapQuantity;

    @Schema(description = "料废数量", example = "2")
    @ExcelProperty("料废数量")
    private BigDecimal materialScrapQuantity;

    @Schema(description = "其他废品数量", example = "1")
    @ExcelProperty("其他废品数量")
    private BigDecimal otherScrapQuantity;

    // ========== 缺陷统计 ==========

    @Schema(description = "致命缺陷率（%）", example = "0.00")
    private BigDecimal criticalRate;

    @Schema(description = "严重缺陷率（%）", example = "0.00")
    private BigDecimal majorRate;

    @Schema(description = "轻微缺陷率（%）", example = "10.00")
    private BigDecimal minorRate;

    @Schema(description = "致命缺陷数量", example = "0")
    private Integer criticalQuantity;

    @Schema(description = "严重缺陷数量", example = "0")
    private Integer majorQuantity;

    @Schema(description = "轻微缺陷数量", example = "1")
    private Integer minorQuantity;

    // ========== 检验 ==========

    @Schema(description = "检测结果", example = "1")
    @ExcelProperty("检测结果")
    private Integer checkResult;

    @Schema(description = "检测日期")
    @ExcelProperty("检测日期")
    private LocalDateTime inspectDate;

    @Schema(description = "检测人员用户 ID", example = "1")
    private Long inspectorUserId;

    @Schema(description = "检测人员昵称", example = "张三")
    @ExcelProperty("检测人员")
    private String inspectorNickname;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
