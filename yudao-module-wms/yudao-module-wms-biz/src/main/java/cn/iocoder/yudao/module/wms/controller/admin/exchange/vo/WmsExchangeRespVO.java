package cn.iocoder.yudao.module.wms.controller.admin.exchange.vo;

import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,update_time,code,create_time,remark,id,audit_status,type,warehouse_id,updater
 */
@Schema(description = "管理后台 - 换货单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsExchangeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20006")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号")
    @ExcelProperty("单据号")
    private String code;

    @Schema(description = "WMS换货单类型 ; WmsExchangeType : 1-良品转次品 , 2-次品转良品", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("WMS换货单类型")
    private Integer type;

    @Schema(description = "调出仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27440")
    @ExcelProperty("调出仓库ID")
    private Long warehouseId;

    @Schema(description = "WMS换货单审批状态 ; WmsExchangeAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("WMS换货单审批状态")
    private Integer auditStatus;

    @Schema(description = "特别说明", example = "你猜")
    @ExcelProperty("特别说明")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

    @Schema(description = "详情清单", example = "")
    @ExcelProperty("详情清单")
    private List<WmsExchangeItemRespVO> itemList;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "仓库", example = "")
    private WmsWarehouseSimpleRespVO warehouse;
}
