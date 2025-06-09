package cn.iocoder.yudao.module.wms.controller.admin.outbound.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundType;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : code,company_id,remark,audit_status,outbound_time,type,upstream_type,latest_outbound_action_id,outbound_status,upstream_id,id,upstream_code,dept_id,warehouse_id
 */
@Schema(description = "管理后台 - 出库单新增/修改 Request VO")
@Data
public class WmsOutboundImportReqVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "WMS出库单类型 ; WmsOutboundType : 1-手工出库 , 2-订单出库 , 3-盘点出库", example = "1")
    @NotNull(message = "WMS出库单类型不能为空", groups = { ValidationGroup.create.class })
    @InEnum(WmsOutboundType.class)
    private Integer type;

    @Schema(description = "详情清单", example = "")
    private List<WmsOutboundItemSaveReqVO> itemList;

    @Schema(description = "WMS出库状态 ; WmsOutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    @InEnum(WmsOutboundStatus.class)
    private Integer outboundStatus;

    @Schema(description = "库存财务公司ID", example = "")
    private Long companyId;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;

    @Schema(description = "出库时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime outboundTime;

    @Schema(description = "计划出库时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("计划出库时间")
    private LocalDateTime outboundPlanTime;

    @Schema(description = "出库动作ID，与flow关联", example = "")
    private Long latestOutboundActionId;

    @Schema(description = "备注", example = "")
    private String remark;

    @Schema(description = "单据号", example = "")
    private String code;

    @Schema(description = "来源单据ID", example = "")
    @NotNull(message = "来源单据ID不能为空")
    private Long upstreamId;

    @Schema(description = "来源单据编码", example = "")
    private String upstreamCode;

    @Schema(description = "WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单 , 3-换货单", example = "")
    @InEnum(BillType.class)
    private Integer upstreamType;

    @Schema(description = "仓库ID", example = "")
    private Long warehouseId;
}
