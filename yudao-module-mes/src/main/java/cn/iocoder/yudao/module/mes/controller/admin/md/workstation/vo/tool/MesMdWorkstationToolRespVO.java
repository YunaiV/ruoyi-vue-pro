package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 工装夹具资源 Response VO")
@Data
public class MesMdWorkstationToolRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "工作站编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workstationId;

    @Schema(description = "工具类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long toolTypeId;

    @Schema(description = "工具类型名称", example = "钻头")
    private String toolTypeName;

    @Schema(description = "数量", example = "1")
    private Integer quantity;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
