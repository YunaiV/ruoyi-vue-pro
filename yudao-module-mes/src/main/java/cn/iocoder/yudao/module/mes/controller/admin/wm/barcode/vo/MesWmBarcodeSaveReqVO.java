package cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 条码清单新增/修改 Request VO")
@Data
public class MesWmBarcodeSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "条码配置编号", example = "1")
    private Long configId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "102")
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @Schema(description = "条码内容", example = "WH-WH001")
    private String content;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "业务编号不能为空")
    private Long bizId;

    @Schema(description = "业务编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WH001")
    @NotEmpty(message = "业务编码不能为空")
    private String bizCode;

    @Schema(description = "业务名称", example = "原料仓")
    private String bizName;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
