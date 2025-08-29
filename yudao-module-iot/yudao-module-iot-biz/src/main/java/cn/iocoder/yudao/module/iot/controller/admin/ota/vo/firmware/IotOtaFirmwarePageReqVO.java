package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT OTA 固件分页 Request VO")
@Data
public class IotOtaFirmwarePageReqVO extends PageParam {

    @Schema(description = "固件名称", example = "智能开关固件")
    private String name;

    @Schema(description = "产品标识", example = "1024")
    private String productId;

    @Schema(description = "创建时间", example = "[2022-07-01 00:00:00, 2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
