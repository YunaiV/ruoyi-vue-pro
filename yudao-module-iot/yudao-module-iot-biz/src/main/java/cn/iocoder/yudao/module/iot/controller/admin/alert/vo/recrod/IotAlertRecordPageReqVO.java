package cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 告警记录分页 Request VO")
@Data
public class IotAlertRecordPageReqVO extends PageParam {

    @Schema(description = "告警配置编号", example = "29320")
    private Long configId;

    @Schema(description = "告警级别", example = "1")
    private Integer level;

    @Schema(description = "产品编号", example = "2050")
    private Long productId;

    @Schema(description = "设备编号", example = "21727")
    private String deviceId;

    @Schema(description = "是否处理", example = "true")
    private Boolean processStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}