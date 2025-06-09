package cn.iocoder.yudao.module.tms.controller.admin.transfer.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemPageReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 调拨单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TmsTransferPageReqVO extends PageParam {
    @Schema(description = "主表分页查询")
    private MainQueryVo mainQueryVo;

    @Schema(description = "明细行分页查询")
    private TmsTransferItemPageReqVO itemQueryVo;


    @Data
    public static class MainQueryVo {

        @Schema(description = "主键")
        private Long id;

        @Schema(description = "创建人ID")
        private String creator;

        @Schema(description = "创建时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] createTime;

        @Schema(description = "更新人ID")
        private String updater;

        @Schema(description = "更新时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] updateTime;

        @Schema(description = "调拨单编码")
        private String code;

        @Schema(description = "发出仓库ID")
        private Long fromWarehouseId;

        @Schema(description = "目的仓库ID")
        private Long toWarehouseId;

        @Schema(description = "审核人ID")
        private Long auditorId;

        @Schema(description = "审核状态")
        private Integer auditStatus;

        @Schema(description = "审核时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] auditTime;

        @Schema(description = "审核意见")
        private String auditAdvice;

        @Schema(description = "出库状态")
        private Integer outboundStatus;

        @Schema(description = "出库时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] outboundTime;

        @Schema(description = "入库状态")
        private Integer inboundStatus;

        @Schema(description = "入库时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] inboundTime;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "跟踪号")
        private String traceNo;

        @Schema(description = "总货值")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private BigDecimal[] totalValue;

        @Schema(description = "总净重")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private BigDecimal[] netWeight;

        @Schema(description = "总毛重")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private BigDecimal[] totalWeight;

        @Schema(description = "总体积")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private BigDecimal[] totalVolume;

        @Schema(description = "总件数")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private Integer[] totalQty;

        @Schema(description = "出库单ID")
        private Long outboundId;

        @Schema(description = "出库单编码")
        private String outboundCode;

        @Schema(description = "入库单ID")
        private Long inboundId;

        @Schema(description = "入库单编码")
        private String inboundCode;
    }
}