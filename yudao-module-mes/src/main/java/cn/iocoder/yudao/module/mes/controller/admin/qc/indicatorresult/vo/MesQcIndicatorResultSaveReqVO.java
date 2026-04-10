package cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - MES 检验结果创建/更新 Request VO")
@Data
public class MesQcIndicatorResultSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "样品编号", example = "SPL-001")
    private String code;

    @Schema(description = "关联质检单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "关联质检单ID不能为空")
    private Long qcId;

    @Schema(description = "质检类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检类型不能为空")
    @InEnum(MesQcTypeEnum.class)
    private Integer qcType;

    @Schema(description = "物资SN", example = "SN-001")
    private String sn;

    @Schema(description = "备注", example = "无")
    private String remark;

    @Schema(description = "检验结果明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验结果明细列表不能为空")
    private List<Item> items;

    @Schema(description = "检验结果明细项")
    @Data
    public static class Item {

        @Schema(description = "编号（更新时传入已有明细的 ID）", example = "1024")
        private Long id;

        @Schema(description = "检测指标ID", example = "1")
        private Long indicatorId;

        @Schema(description = "检测值（统一存为字符串）", example = "3.14")
        private String value;

        @Schema(description = "备注", example = "无")
        private String remark;

    }

}
