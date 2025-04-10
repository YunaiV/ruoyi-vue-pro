package cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 付款条款分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SrmPaymentTermPageReqVO extends PageParam {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "人民币采购条款（中文）")
    private String paymentTermZh;

    @Schema(description = "外币采购条款（中文）")
    private String paymentTermZhForeign;

    @Schema(description = "外币采购条款（英文）")
    private String paymentTermEnForeign;

    @Schema(description = "备注", example = "随便")
    private String remark;

}