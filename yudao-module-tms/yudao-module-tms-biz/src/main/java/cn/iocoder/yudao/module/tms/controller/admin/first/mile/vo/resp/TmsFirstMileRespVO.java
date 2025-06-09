package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo.TmsFirstMileItemRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingRespVO;
import cn.iocoder.yudao.module.tms.enums.TmsDictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 头程单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TmsFirstMileRespVO extends BaseVO {
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "单据日期")
    @ExcelProperty("单据日期")
    private LocalDateTime billTime;

    @Schema(description = "物流商ID")
    @ExcelProperty("物流商ID")
    private String carrierId;

    @Schema(description = "结算日期")
    @ExcelProperty("结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "应付款余额")
    @ExcelProperty("应付款余额")
    private BigDecimal balance;

    @Schema(description = "审核人")
    private Long auditorId;

    @Schema(description = "审核人名称")
    @ExcelProperty("审核人名称")
    private String auditorName;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核状态")
    @ExcelProperty(value = "审核状态", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.AUDIT_STATUS)
    private Integer auditStatus;

    @Schema(description = "目的仓ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toWarehouseId;

    @Schema(description = "目的仓名称")
    @ExcelProperty("目的仓名称")
    private String toWarehouseName;

    @Schema(description = "柜型（字典）")
    @ExcelProperty(value = "柜型", converter = DictConvert.class)
    @DictFormat(TmsDictTypeConstants.TMS_CABINET_TYPE)
    private Integer cabinetType;

    @Schema(description = "装柜日期")
    @ExcelProperty("装柜日期")
    private LocalDateTime packTime;

    @Schema(description = "预计到货日期")
    @ExcelProperty("预计到货日期")
    private LocalDateTime arrivePlanTime;

    @Schema(description = "货柜体积（m³）")
    @ExcelProperty("货柜体积（m³）")
    private BigDecimal totalVolume;

    @Schema(description = "货柜毛重（kg）")
    @ExcelProperty("货柜毛重（kg）")
    private BigDecimal totalWeight;

    @Schema(description = "货柜净重（kg）")
    @ExcelProperty("货柜净重（kg）")
    private BigDecimal netWeight;

    @Schema(description = "货柜货值（按最近采购价）")
    @ExcelProperty("货柜货值（按最近采购价）")
    private BigDecimal totalValue;

    @Schema(description = "货柜件数")
    @ExcelProperty("货柜件数")
    private Integer totalQty;

    @Schema(description = "总箱数")
    @ExcelProperty("总箱数")
    private Integer totalBoxQty;

    @Schema(description = "发货状态")
    @ExcelProperty("发货状态")
    private Integer outboundStatus;

    @Schema(description = "出库时间")
    @ExcelProperty("出库时间")
    private LocalDateTime outboundTime;

    @Schema(description = "入库状态")
    @ExcelProperty("入库状态")
    private Integer inboundStatus;

    @Schema(description = "入库时间")
    @ExcelProperty("入库时间")
    private LocalDateTime inboundTime;

    @Schema(description = "出口公司ID")
    private Long exportCompanyId;

    @Schema(description = "出口公司简称")
    @ExcelProperty("出口公司简称")
    private String exportCompanyShortName;

    @Schema(description = "中转公司ID")
    private Long transitCompanyId;

    @Schema(description = "中转公司简称")
    @ExcelProperty("中转公司简称")
    private String transitCompanyShortName;

    @Schema(description = "版本号")
    private Integer revision;

    @Schema(description = "头程单明细")
    private List<TmsFirstMileItemRespVO> firstMileItems;

    @Schema(description = "费用明细")
    private List<TmsFeeRespVO> fees;

    @Schema(description = "最新跟踪信息")
    private TmsVesselTrackingRespVO vesselTracking;
}