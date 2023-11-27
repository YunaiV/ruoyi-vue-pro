package cn.iocoder.yudao.module.crm.controller.admin.business.vo.business;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 商机 Excel 导出 Request VO，参数和 CrmBusinessPageReqVO 是一致的")
@Data
public class CrmBusinessExportReqVO {

    @Schema(description = "商机名称", example = "李四")
    private String name;

    @Schema(description = "商机状态类型编号", example = "25714")
    private Long statusTypeId;

    @Schema(description = "商机状态编号", example = "30320")
    private Long statusId;

    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] contactNextTime;

    @Schema(description = "客户编号", example = "10299")
    private Long customerId;

    @Schema(description = "预计成交日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] dealTime;

    @Schema(description = "商机金额", example = "12371")
    private BigDecimal price;

    @Schema(description = "整单折扣")
    private BigDecimal discountPercent;

    @Schema(description = "产品总金额", example = "12025")
    private BigDecimal productPrice;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "负责人的用户编号", example = "25562")
    private Long ownerUserId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "只读权限的用户编号数组")
    private String roUserIds;

    @Schema(description = "读写权限的用户编号数组")
    private String rwUserIds;

    @Schema(description = "1赢单2输单3无效", example = "1")
    private Integer endStatus;

    @Schema(description = "结束时的备注", example = "你说的对")
    private String endRemark;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] contactLastTime;

    @Schema(description = "跟进状态", example = "1")
    private Integer followUpStatus;

}
