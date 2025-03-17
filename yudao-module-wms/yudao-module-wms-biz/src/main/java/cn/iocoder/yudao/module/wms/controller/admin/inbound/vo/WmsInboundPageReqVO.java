package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 入库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInboundPageReqVO extends PageParam {

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "入库单类型", example = "1")
    private Integer type;

    @Schema(description = "仓库ID", example = "23620")
    private Long warehouseId;

    @Schema(description = "状态", example = "1")
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
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] planArrivalTime;

    @Schema(description = "实际到货时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] actualArrivalTime;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "初始库龄")
    private Integer initAge;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}