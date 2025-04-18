package cn.iocoder.yudao.module.wms.controller.admin.exchange.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 换货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsExchangePageReqVO extends PageParam {

    @Schema(description = "单据号")
    private String code;

    @Schema(description = "类型", example = "2")
    private Integer type;

    @Schema(description = "调出仓库ID", example = "27440")
    private Long warehouseId;

    @Schema(description = "状态", example = "2")
    private String auditStatus;

    @Schema(description = "特别说明", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}