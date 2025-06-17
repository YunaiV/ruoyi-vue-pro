package cn.iocoder.yudao.module.tms.controller.admin.transfer.vo;

import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsWarehourseRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 调拨单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TmsTransferRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "乐观锁")
    @ExcelProperty("乐观锁")
    private Integer revision;

    @Schema(description = "创建人ID")
    @ExcelProperty("创建人ID")
    private String creator;

    @Schema(description = "创建人姓名")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人ID")
    @ExcelProperty("更新人ID")
    private String updater;

    @ExcelProperty("更新人姓名")
    @Schema(description = "更新人姓名")
    private String updaterName;

    @Schema(description = "更新时间")
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "调拨单编码")
    @ExcelProperty("调拨单编码")
    private String code;

    @Schema(description = "发出仓库ID")
    @ExcelProperty("发出仓库ID")
    private Long fromWarehouseId;

    @Schema(description = "发出仓库")
    private TmsWarehourseRespVO fromWarehouse;

    @Schema(description = "目的仓库ID")
    @ExcelProperty("目的仓库ID")
    private Long toWarehouseId;

    @Schema(description = "目的仓库")
    private TmsWarehourseRespVO toWarehouse;

    @Schema(description = "审核人ID")
    @ExcelProperty("审核人ID")
    private Long auditorId;

    @Schema(description = "审核人姓名")
    @ExcelProperty("审核人姓名")
    private String auditorName;

    @Schema(description = "审核状态")
    @ExcelProperty("审核状态")
    private Integer auditStatus;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核意见")
    @ExcelProperty("审核意见")
    private String auditAdvice;

    @Schema(description = "出库状态(wms出库状态)")
    @ExcelProperty("出库状态")
    private Integer outboundStatus;

    @Schema(description = "出库时间")
    @ExcelProperty("出库时间")
    private LocalDateTime outboundTime;

    @Schema(description = "入库状态(wms入库状态)")
    @ExcelProperty("入库状态")
    private Integer inboundStatus;

    @Schema(description = "入库时间")
    @ExcelProperty("入库时间")
    private LocalDateTime inboundTime;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "跟踪号")
    @ExcelProperty("跟踪号")
    private String traceNo;

    @Schema(description = "总货值")
    @ExcelProperty("总货值")
    private BigDecimal totalValue;

    @Schema(description = "总净重")
    @ExcelProperty("总净重")
    private BigDecimal netWeight;

    @Schema(description = "总毛重")
    @ExcelProperty("总毛重")
    private BigDecimal totalWeight;

    @Schema(description = "总体积")
    @ExcelProperty("总体积")
    private BigDecimal totalVolume;

    @Schema(description = "总件数")
    @ExcelProperty("总件数")
    private Integer totalQty;

    @Schema(description = "出库单ID")
    @ExcelProperty("出库单ID")
    private Long outboundId;

    @Schema(description = "出库单编码")
    @ExcelProperty("出库单编码")
    private String outboundCode;

    @Schema(description = "入库单ID")
    @ExcelProperty("入库单ID")
    private Long inboundId;

    @Schema(description = "入库单编码")
    @ExcelProperty("入库单编码")
    private String inboundCode;

    @Schema(description = "调拨单明细")
    private List<TmsTransferItemRespVO> items;
}