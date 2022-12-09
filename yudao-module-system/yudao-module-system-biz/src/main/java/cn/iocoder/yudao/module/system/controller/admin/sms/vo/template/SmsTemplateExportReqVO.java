package cn.iocoder.yudao.module.system.controller.admin.sms.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 短信模板 Excel 导出 Request VO", description = "参数和 SmsTemplatePageReqVO 是一致的")
@Data
public class SmsTemplateExportReqVO {

    @Schema(title = "短信签名", example = "1")
    private Integer type;

    @Schema(title = "开启状态", example = "1")
    private Integer status;

    @Schema(title = "模板编码", example = "test_01", description = "模糊匹配")
    private String code;

    @Schema(title = "模板内容", example = "你好，{name}。你长的太{like}啦！", description = "模糊匹配")
    private String content;

    @Schema(title = "短信 API 的模板编号", example = "4383920", description = "模糊匹配")
    private String apiTemplateId;

    @Schema(title = "短信渠道编号", example = "10")
    private Long channelId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "创建时间")
    private LocalDateTime[] createTime;

}
