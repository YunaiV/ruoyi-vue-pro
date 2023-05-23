package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 快递运费模板 Excel 导出 Request VO，参数和 DeliveryExpressTemplatePageReqVO 是一致的")
@Data
public class DeliveryExpressTemplateExportReqVO {

    @Schema(description = "模板名称", example = "王五")
    private String name;

    @Schema(description = "配送计费方式 1:按件 2:按重量 3:按体积")
    private Integer chargeMode;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
