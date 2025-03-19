package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP采购申请单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPurchaseRequestPageReqVO extends PageParam {

    @Schema(description = "单据编号")
    private String no;

    @Schema(description = "申请人")
    private String applicant;

    @Schema(description = "申请部门")
    private String applicationDept;

    @Schema(description = "单据日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] requestTime;

    @Schema(description = "审核状态(0:待审核，1:审核通过，2:审核未通过)", example = "2")
    private Integer status;

    @Schema(description = "关闭状态（0已关闭，1已开启）", example = "1")
    private Integer offStatus;

    @Schema(description = "订购状态（0部分订购，1全部订购）", example = "1")
    private Integer orderStatus;

    @Schema(description = "审核者")
    private String auditor;

    @Schema(description = "审核时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] auditTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}