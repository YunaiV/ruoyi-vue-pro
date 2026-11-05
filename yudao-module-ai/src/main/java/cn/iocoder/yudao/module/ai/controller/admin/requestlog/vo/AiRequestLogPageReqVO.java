package cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 请求日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiRequestLogPageReqVO extends PageParam {

    @Schema(description = "请求编号（本地 UUID）", example = "0b4f6c0e-7d3c-4f0e-9a3c-1f2a3b4c5d6e")
    private String requestId;

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "业务类型", example = "1")
    @InEnum(value = AiRequestLogBizTypeEnum.class, message = "业务类型必须是 {value}")
    private Integer bizType;

    @Schema(description = "平台", example = "TongYi")
    private String platform;

    @Schema(description = "模型编号", example = "2048")
    private Long modelId;

    @Schema(description = "模型标识，模糊匹配", example = "qwen-plus")
    private String model;

    @Schema(description = "状态", example = "20")
    @InEnum(value = AiRequestLogStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] beginTime;

}
