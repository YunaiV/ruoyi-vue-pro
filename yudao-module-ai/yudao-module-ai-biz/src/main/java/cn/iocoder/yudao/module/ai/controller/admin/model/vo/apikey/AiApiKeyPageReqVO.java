package cn.iocoder.yudao.module.ai.controller.admin.model.vo.apikey;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI API 密钥分页 Request VO")
@Data
public class AiApiKeyPageReqVO extends PageParam {

    @Schema(description = "名称", example = "文心一言")
    private String name;

    @Schema(description = "平台", example = "OpenAI")
    private String platform;

    @Schema(description = "状态", example = "1")
    private Integer status;

}