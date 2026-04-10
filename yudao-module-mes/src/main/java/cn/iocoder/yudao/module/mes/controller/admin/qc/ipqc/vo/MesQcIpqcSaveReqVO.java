package cn.iocoder.yudao.module.mes.controller.admin.qc.ipqc.vo;

import cn.hutool.core.util.ObjUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 过程检验单新增/修改 Request VO")
@Data
public class MesQcIpqcSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "检验单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "IPQC20250101001")
    @NotEmpty(message = "检验单编号不能为空")
    private String code;

    @Schema(description = "检验单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "XX工单过程检验")
    @NotEmpty(message = "检验单名称不能为空")
    private String name;

    @Schema(description = "IPQC 检验类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验类型不能为空")
    private Integer type;

    // ========== 来源单据 ==========

    @Schema(description = "来源单据类型", example = "304")
    private Integer sourceDocType;

    @Schema(description = "来源单据 ID", example = "200")
    private Long sourceDocId;

    @Schema(description = "来源单据行 ID", example = "300")
    private Long sourceLineId;

    // ========== 生产关联 ==========

    @Schema(description = "生产工单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "生产工单不能为空")
    private Long workOrderId;

    @Schema(description = "生产任务 ID", example = "20")
    private Long taskId;

    @Schema(description = "工位 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @NotNull(message = "工位不能为空")
    private Long workstationId;

    @Schema(description = "工序 ID", example = "40")
    private Long processId;

    // ========== 物料 ==========

    @Schema(description = "产品物料 ID", example = "50")
    private Long itemId;

    // ========== 数量 ==========

    @Schema(description = "检测数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "检测数量不能为空")
    @DecimalMin(value = "0", message = "检测数量不能小于 0")
    private BigDecimal checkQuantity;

    @Schema(description = "合格品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "95")
    @NotNull(message = "合格品数量不能为空")
    @DecimalMin(value = "0", message = "合格品数量不能小于 0")
    private BigDecimal qualifiedQuantity;

    @Schema(description = "不合格品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "不合格品数量不能为空")
    @DecimalMin(value = "0", message = "不合格品数量不能小于 0")
    private BigDecimal unqualifiedQuantity;

    @Schema(description = "工废数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "工废数量不能为空")
    @DecimalMin(value = "0", message = "工废数量不能小于 0")
    private BigDecimal laborScrapQuantity;

    @Schema(description = "料废数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "料废数量不能为空")
    @DecimalMin(value = "0", message = "料废数量不能小于 0")
    private BigDecimal materialScrapQuantity;

    @Schema(description = "其他废品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "其他废品数量不能为空")
    @DecimalMin(value = "0", message = "其他废品数量不能小于 0")
    private BigDecimal otherScrapQuantity;

    // ========== 检验 ==========

    @Schema(description = "检测结果", example = "1")
    private Integer checkResult;

    @Schema(description = "检测日期")
    private LocalDateTime inspectDate;

    @Schema(description = "检测人员用户 ID", example = "1")
    private Long inspectorUserId;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @AssertTrue(message = "检测数量必须等于合格品数量与不合格品数量之和")
    @JsonIgnore
    public boolean isQuantityValid() {
        if (ObjUtil.hasNull(checkQuantity, qualifiedQuantity, unqualifiedQuantity)) {
            return true; // @NotNull 会处理 null
        }
        return checkQuantity.compareTo(qualifiedQuantity.add(unqualifiedQuantity)) == 0;
    }

    @AssertTrue(message = "工废、料废、其他废品数量之和不能超过不合格品数量")
    @JsonIgnore
    public boolean isScrapQuantityValid() {
        if (ObjUtil.hasNull(unqualifiedQuantity, laborScrapQuantity, materialScrapQuantity, otherScrapQuantity)) {
            return true; // @NotNull 会处理 null
        }
        BigDecimal scrapSum = laborScrapQuantity.add(materialScrapQuantity).add(otherScrapQuantity);
        return scrapSum.compareTo(unqualifiedQuantity) <= 0;
    }

}
