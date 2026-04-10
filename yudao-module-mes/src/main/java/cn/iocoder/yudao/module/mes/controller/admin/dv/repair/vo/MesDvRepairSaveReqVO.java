package cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 维修工单新增/修改 Request VO")
@Data
public class MesDvRepairSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "维修工单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "REP2024001")
    @NotBlank(message = "维修工单编码不能为空")
    private String code;

    @Schema(description = "维修工单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "注塑机液压系统维修")
    @NotBlank(message = "维修工单名称不能为空")
    private String name;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备不能为空")
    private Long machineryId;

    @Schema(description = "报修日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "报修日期不能为空")
    private LocalDateTime requireDate;

    @Schema(description = "维修完成日期")
    private LocalDateTime finishDate;

    @Schema(description = "验收日期")
    private LocalDateTime confirmDate;

    @Schema(description = "维修结果", example = "1")
    private Integer result;

    @Schema(description = "维修人用户编号", example = "1")
    private Long acceptedUserId;

    @Schema(description = "验收人用户编号", example = "1")
    private Long confirmUserId;

    @Schema(description = "来源单据类型", example = "1")
    private Integer sourceDocType;

    @Schema(description = "来源单据编号", example = "1")
    private Long sourceDocId;

    @Schema(description = "来源单据编码", example = "DOC001")
    private String sourceDocCode;

    @Schema(description = "备注", example = "测试备注")
    private String remark;

}
