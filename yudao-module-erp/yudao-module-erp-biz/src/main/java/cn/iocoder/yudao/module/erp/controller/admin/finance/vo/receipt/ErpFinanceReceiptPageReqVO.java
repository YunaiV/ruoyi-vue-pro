package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 收款单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceReceiptPageReqVO extends PageParam {

    @Schema(description = "收款单编号", example = "XS001")
    private String no;

    @Schema(description = "收款时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] receiptTime;

    @Schema(description = "客户编号", example = "1724")
    private Long customerId;

    @Schema(description = "创建者", example = "666")
    private String creator;

    @Schema(description = "财务人员编号", example = "888")
    private String financeUserId;

    @Schema(description = "收款账户编号", example = "31189")
    private Long accountId;

    @Schema(description = "收款状态", example = "2")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "业务编号", example = "123")
    private String bizNo;

}