package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo.TmsFirstMileItemPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingPageReqVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 头程单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TmsFirstMilePageReqVO extends PageParam {

    @Schema(description = "嵌套用 - 头程单查询条件")
    private mainQueryVO mainQueryVO;

    @Schema(description = "嵌套用 - 头程单明细查询条件")
    private TmsFirstMileItemPageReqVO itemPageReqVO;

    @Schema(description = "嵌套用 - 跟踪信息筛选 VO")
    private TmsVesselTrackingFirstMileQueryVO trackingQueryVO;

    @Data
    @Schema(description = "嵌套用 - 头程单查询条件")
    public static class mainQueryVO {
        @Schema(description = "id")
        private Long id;

        @Schema(description = "销售公司ID")
        private Long salesCompanyId;

        @Schema(description = "创建时间")
        private LocalDateTime[] createTime;

        @Schema(description = "编码")
        private String code;

        @Schema(description = "单据日期")
        private LocalDateTime[] billTime;

        @Schema(description = "物流商ID")
        private String carrierId;

        @Schema(description = "结算日期")
        private LocalDateTime[] settlementDate;

        @Schema(description = "应付款余额")
        private BigDecimal balance;

        @Schema(description = "审核人")
        private Long auditorId;

        @Schema(description = "审核时间")
        private LocalDateTime[] auditTime;

        @Schema(description = "审核状态")
        private Integer auditStatus;

        @Schema(description = "目的仓ID")
        private Long toWarehouseId;

        @Schema(description = "柜型（字典）")
        private Integer cabinetType;

        @Schema(description = "装柜日期")
        private LocalDateTime[] packTime;

        @Schema(description = "预计到货日期")
        private LocalDateTime[] arrivePlanTime;

        @Schema(description = "货柜体积（m³）")
        private BigDecimal[] totalVolume;

        @Schema(description = "货柜毛重（kg）")
        private BigDecimal[] totalWeight;

        @Schema(description = "货柜净重（kg）")
        private BigDecimal[] netWeight;

        @Schema(description = "货柜货值（按最近采购价）")
        private BigDecimal[] totalValue;

        @Schema(description = "货柜件数")
        private Integer[] totalQty;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "发货状态")
        private Integer outboundStatus;

        @Schema(description = "出库时间")
        private LocalDateTime[] outboundTime;

        @Schema(description = "入库状态")
        private Integer inboundStatus;

        @Schema(description = "入库时间")
        private LocalDateTime[] inboundTime;
    }

    @Schema(description = "嵌套用 - 跟踪信息筛选 VO")
    @Data
    public static class TmsVesselTrackingFirstMileQueryVO extends TmsVesselTrackingPageReqVO {
        @Schema(hidden = true)
        @JsonIgnore
        private Long id;

        @Schema(hidden = true)
        @JsonIgnore
        private Integer upstreamType;

        @Schema(hidden = true)
        @JsonIgnore
        private Long upstreamId;
    }

}
