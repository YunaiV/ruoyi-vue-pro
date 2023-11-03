package cn.iocoder.yudao.module.pay.controller.admin.notify.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 回调通知分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayNotifyTaskPageReqVO extends PageParam {

    @Schema(description = "应用编号", example = "10636")
    private Long appId;

    @Schema(description = "通知类型", example = "2")
    private Integer type;

    @Schema(description = "数据编号", example = "6722")
    private Long dataId;

    @Schema(description = "通知状态", example = "1")
    private Integer status;

    @Schema(description = "商户订单编号", example = "26697")
    private String merchantOrderId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
