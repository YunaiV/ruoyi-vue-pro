package cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - MES 检验结果 Response VO")
@Data
public class MesQcIndicatorResultRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "样品编号", example = "SPL-001")
    private String code;

    @Schema(description = "关联质检单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long qcId;

    @Schema(description = "质检类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer qcType;

    @Schema(description = "产品物料ID", example = "1")
    private Long itemId;

    @Schema(description = "物资SN", example = "SN-001")
    private String sn;

    @Schema(description = "备注", example = "无")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 子表：检验结果明细 ==========

    @Schema(description = "检验结果明细列表")
    private List<Item> items;

    @Schema(description = "检验结果明细项")
    @Data
    public static class Item {

        @Schema(description = "编号", example = "1024")
        private Long id;

        @Schema(description = "关联检验结果ID", example = "1")
        private Long resultId;

        @Schema(description = "检测指标ID", example = "1")
        private Long indicatorId;

        @Schema(description = "检测值（统一存为字符串）", example = "3.14")
        private String value;

        @Schema(description = "备注", example = "无")
        private String remark;

        // ========== 关联查询字段（来自 indicator） ==========

        @Schema(description = "检测指标名称", example = "外观检查")
        private String indicatorName;

        @Schema(description = "质检值类型", example = "1")
        private Integer valueType;

        @Schema(description = "值属性", example = "IMG")
        private String valueSpecification;

    }

}
