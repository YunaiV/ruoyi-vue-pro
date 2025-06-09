package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo.TmsFirstMileItemSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingSaveReqVO;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 头程单新增/修改 Request VO")
@Data
public class TmsFirstMileSaveReqVO {
    //id
    @Schema(description = "id")
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时，头程单id不能为空")
    @Null(groups = {Validation.OnCreate.class}, message = "创建时，头程单id需为空")
    @DiffLogField(name = "头程单ID")
    private Long id;

    /**
     * code限制开发给用户
     */
    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "编码不能为空")
    @DiffLogField(name = "编码")
    private String code;

    @Schema(description = "单据日期")
    @DiffLogField(name = "单据日期")
    private LocalDateTime billTime;

    @Schema(description = "物流商ID")
    @DiffLogField(name = "物流商ID")
    private String carrierId;

    @Schema(description = "结算日期")
    @DiffLogField(name = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "应付款余额")
    @DiffLogField(name = "应付款余额")
    private BigDecimal balance;

    @Schema(description = "目的仓ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目的仓ID不能为空")
    @DiffLogField(name = "目的仓ID")
    private Long toWarehouseId;

    @Schema(description = "柜型（字典）")
    @DiffLogField(name = "柜型")
    private Integer cabinetType;

    @Schema(description = "装柜日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "装柜日期不能为空")
    @DiffLogField(name = "装柜日期")
    private LocalDateTime packTime;

    @Schema(description = "销售公司ID")
    @DiffLogField(name = "销售公司ID")
    private Long salesCompanyId;

    @Schema(description = "预计到货日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预计到货日期不能为空")
    @DiffLogField(name = "预计到货日期")
    private LocalDateTime arrivePlanTime;


    @Schema(description = "货柜货值（按最近采购价）")
    @DiffLogField(name = "货柜货值")
    private BigDecimal totalValue;

    @Schema(description = "货柜件数")
    @DiffLogField(name = "货柜件数")
    private Integer totalQty;

    @Schema(description = "备注")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "出口公司ID")
    private Long exportCompanyId;

    @Schema(description = "中转公司ID")
    private Long transitCompanyId;

    @Schema(description = "版本号")
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时版本号不能为空")
    private Integer revision;

    @Schema(description = "头程单明细列表")
    @DiffLogField(name = "头程单明细列表")
    private List<TmsFirstMileItemSaveReqVO> firstMileItems;

    @Schema(description = "出运订单费用明细列表")
    @DiffLogField(name = "出运订单费用明细列表")
    private List<TmsFeeSaveReqVO2> fees;

    @Schema(description = "船期信息")
    @DiffLogField(name = "船期信息")
    private TmsVesselTrackingSaveReqVO2 vesselTracking;


    public void initId() {
        // 在 id 被赋值后设置 upstreamId
        if (id != null) {
            // 设置 vesselTracking 和 fees 的 upstreamId
            if (vesselTracking != null) {
                vesselTracking.setUpstreamId(id);
            }
            if (fees != null) {
                fees.forEach(fee -> fee.setUpstreamId(id));
            }
        }
    }


    @Data
    public static class TmsVesselTrackingSaveReqVO2 extends TmsVesselTrackingSaveReqVO {
        @Schema(description = "上游单据类型; 前端不填入，后端写死的")
        private final Integer upstreamType = BillType.TMS_FIRST_MILE.getValue();

        @Schema(description = "上游业务单ID，如调拨单ID(前端不填入)")
        @Null(groups = {Validation.OnCreate.class}, message = "上游业务单ID必须为空，前端创建头程时不传递")
        private Long upstreamId;
    }

    @Data
    public static class TmsFeeSaveReqVO2 extends TmsFeeSaveReqVO {
        @Schema(description = "上游单据类型; 前端不填入，后端写死的")
        private final Integer upstreamType = BillType.TMS_FIRST_MILE.getValue();

        @Schema(description = "上游业务单ID，如调拨单ID(前端不填入)")
        @Null(groups = {Validation.OnCreate.class}, message = "上游业务单ID必须为空，前端创建头程时不传递")
        private Long upstreamId;
    }
}