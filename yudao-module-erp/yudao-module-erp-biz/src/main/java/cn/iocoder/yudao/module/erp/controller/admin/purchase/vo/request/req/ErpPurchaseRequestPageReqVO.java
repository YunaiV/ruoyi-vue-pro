package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

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

    @Schema(description = "申请人id")
    private Long applicantId;

    @Schema(description = "申请部门id")
    private Long applicationDeptId;

    @Schema(description = "单据日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] requestTime;

    @Schema(description = "状态")
    private Integer status;

//    @Schema(description = "关闭状态")
//    private Integer offStatus;
//
//    @Schema(description = "订购状态")
//    private Integer orderStatus;

    @Schema(description = "审核者id")
    private Long auditorId;

    @Schema(description = "审核时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] auditTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    //supplierId 供应商编号
    @Schema(description = "供应商编号")
    private String supplierId;
}