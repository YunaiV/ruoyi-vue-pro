package cn.iocoder.yudao.module.system.controller.admin.sms.vo.template;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 短信模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsTemplatePageReqVO extends PageParam {

    @Schema(description = "短信签名", example = "1")
    private Integer type;

    @Schema(description = "开启状态", example = "1")
    private Integer status;

    @Schema(description = "模板编码，模糊匹配", example = "test_01")
    private String code;

    @Schema(description = "模板内容，模糊匹配", example = "你好，{name}。你长的太{like}啦！")
    private String content;

    @Schema(description = "短信 API 的模板编号，模糊匹配", example = "4383920")
    private String apiTemplateId;

    @Schema(description = "短信渠道编号", example = "10")
    private Long channelId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
