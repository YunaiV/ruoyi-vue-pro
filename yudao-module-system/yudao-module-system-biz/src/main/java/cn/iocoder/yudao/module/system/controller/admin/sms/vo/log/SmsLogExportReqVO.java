package cn.iocoder.yudao.module.system.controller.admin.sms.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 短信日志 Excel 导出 Request VO", description = "参数和 SmsLogPageReqVO 是一致的")
@Data
public class SmsLogExportReqVO {

    @Schema(title = "短信渠道编号", example = "10")
    private Long channelId;

    @Schema(title = "模板编号", example = "20")
    private Long templateId;

    @Schema(title = "手机号", example = "15601691300")
    private String mobile;

    @Schema(title = "发送状态", example = "1")
    private Integer sendStatus;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "开始发送时间")
    private LocalDateTime[] sendTime;

    @Schema(title = "接收状态", example = "0")
    private Integer receiveStatus;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "开始接收时间")
    private LocalDateTime[] receiveTime;

}
