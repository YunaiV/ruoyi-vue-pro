package cn.iocoder.yudao.module.pay.controller.admin.transfer.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 转账单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayTransferPageReqVO extends PageParam {

    @Schema(description = "转账单号")
    private String no;

    @Schema(description = "应用编号", example = "12831")
    private Long appId;

    @Schema(description = "渠道编码", example = "wx_app")
    private String channelCode;

    @Schema(description = "商户转账单编号", example = "17481")
    private String merchantTransferId;

    @Schema(description = "类型", example = "2")
    private Integer type;

    @Schema(description = "转账状态", example = "2")
    private Integer status;

    @Schema(description = "收款人姓名", example = "王五")
    private String userName;

    @Schema(description = "渠道转账单号")
    private String channelTransferNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
