package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 入库单新增/修改 Request VO")
@Data
public class WmsInboundSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6889")
    private Long id;

    @Schema(description = "单据号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "单据号不能为空")
    private String no;

    @Schema(description = "入库单类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "入库单类型不能为空")
    private Integer type;

    @Schema(description = "仓库ID", example = "23620")
    private Long warehouseId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态不能为空")
    private String status;

    @Schema(description = "来源单据ID", example = "24655")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    private String sourceBillNo;

    @Schema(description = "来源单据类型", example = "2")
    private Integer sourceBillType;

    @Schema(description = "参考号")
    private String referNo;

    @Schema(description = "跟踪号")
    private String traceNo;

    @Schema(description = "运输方式，1-海运；2-火车；3-空运；4、集卡")
    private Integer shippingMethod;

    @Schema(description = "预计到货时间")
    private LocalDateTime planArrivalTime;

    @Schema(description = "实际到货时间")
    private LocalDateTime actualArrivalTime;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "初始库龄")
    private Integer initAge;

}