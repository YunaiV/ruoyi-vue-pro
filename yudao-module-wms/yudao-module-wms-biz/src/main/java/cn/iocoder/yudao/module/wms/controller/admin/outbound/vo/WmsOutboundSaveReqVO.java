package cn.iocoder.yudao.module.wms.controller.admin.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 出库单新增/修改 Request VO")
@Data
public class WmsOutboundSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "7690")
    private Long id;

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "仓库ID", example = "16056")
    private Long warehouseId;

    @Schema(description = "类型", example = "1")
    private Integer type;

    @Schema(description = "状态", example = "1")
    private String status;

    @Schema(description = "审核状态", example = "2")
    private Integer auditStatus;

    @Schema(description = "来源单据ID", example = "32195")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    private String sourceBillNo;

    @Schema(description = "来源单据类型", example = "2")
    private Integer sourceBillType;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

}