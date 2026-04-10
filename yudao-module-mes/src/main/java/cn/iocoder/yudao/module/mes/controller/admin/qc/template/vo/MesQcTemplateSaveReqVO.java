package cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - MES 质检方案新增/修改 Request VO")
@Data
public class MesQcTemplateSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "QCT001")
    @NotEmpty(message = "方案编号不能为空")
    private String code;

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "成品出货检验方案")
    @NotEmpty(message = "方案名称不能为空")
    private String name;

    @Schema(description = "检测种类（1-IQC 2-IPQC 3-OQC 4-RQC，字典 mes_qc_type）", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 3]")
    @NotNull(message = "检测种类不能为空")
    @Size(min = 1, message = "检测种类至少选择一项")
    private List<Integer> types;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
