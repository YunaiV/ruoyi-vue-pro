package cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo;

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

@Schema(description = "管理后台 - MES 退货检验单新增/修改 Request VO")
@Data
public class MesQcRqcSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "检验单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "RQC20250101001")
    @NotEmpty(message = "检验单编号不能为空")
    private String code;

    @Schema(description = "检验单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "物料A退货检验")
    @NotEmpty(message = "检验单名称不能为空")
    private String name;

    @Schema(description = "检验模板 ID", example = "100")
    private Long templateId;

    // ========== 来源单据 ==========

    @Schema(description = "来源单据 ID", example = "200")
    private Long sourceDocId;

    @Schema(description = "来源单据类型", example = "116")
    private Integer sourceDocType;

    @Schema(description = "来源单据行 ID", example = "300")
    private Long sourceLineId;

    // ========== 检验类型 ==========

    @Schema(description = "检验类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验类型不能为空")
    private Integer type;

    // ========== 物料 ==========

    @Schema(description = "产品物料 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "产品物料不能为空")
    private Long itemId;

    @Schema(description = "批次号", example = "BATCH001")
    private String batchCode;

    // ========== 数量 ==========

    @Schema(description = "检测数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "检测数量不能为空")
    @DecimalMin(value = "0", message = "检测数量不能小于 0")
    private BigDecimal checkQuantity;

    @Schema(description = "合格品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "90")
    @NotNull(message = "合格品数量不能为空")
    @DecimalMin(value = "0", message = "合格品数量不能小于 0")
    private BigDecimal qualifiedQuantity;

    @Schema(description = "不合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "不合格数量不能为空")
    @DecimalMin(value = "0", message = "不合格数量不能小于 0")
    private BigDecimal unqualifiedQuantity;

    // ========== 检验 ==========

    @Schema(description = "检测结果", example = "1")
    private Integer checkResult;

    @Schema(description = "检测日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "检测日期不能为空")
    private LocalDateTime inspectDate;

    @Schema(description = "检测人员用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检测人员不能为空")
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

}
