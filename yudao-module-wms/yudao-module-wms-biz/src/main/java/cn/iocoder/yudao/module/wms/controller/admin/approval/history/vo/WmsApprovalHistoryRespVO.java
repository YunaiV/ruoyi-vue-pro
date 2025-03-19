package cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : bill_id,tenant_id,status_after,creator,update_time,create_time,status_type,bill_type,comment,id,status_before,updater
 */
@Schema(description = "管理后台 - 审批历史 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsApprovalHistoryRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25351")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "来源单据类型 ; BillType : 0-入库单 , 1-出库单", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("来源单据类型")
    private Integer billType;

    @Schema(description = "业务单据ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29844")
    @ExcelProperty("业务单据ID")
    private Long billId;

    @Schema(description = "状态类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态类型")
    private String statusType;

    @Schema(description = "审批前的状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("审批前的状态")
    private Integer statusBefore;

    @Schema(description = "审批后状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("审批后状态")
    private Integer statusAfter;

    @Schema(description = "审批意见")
    @ExcelProperty("审批意见")
    private String comment;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

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
}
