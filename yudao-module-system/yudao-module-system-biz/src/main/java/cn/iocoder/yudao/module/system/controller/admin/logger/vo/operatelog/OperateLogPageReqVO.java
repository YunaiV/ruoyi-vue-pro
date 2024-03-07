package cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 操作日志分页列表 Request VO")
@Data
public class OperateLogPageReqVO extends PageParam {

    @Schema(description = "操作模块，模拟匹配", example = "订单")
    private String module;

    @Schema(description = "用户昵称，模拟匹配", example = "芋道")
    private String userNickname;

    @Schema(description = "操作分类，参见 OperateLogTypeEnum 枚举类", example = "1")
    private Integer type;

    @Schema(description = "操作状态", example = "true")
    private Boolean success;

    @Schema(description = "开始时间", example = "[2022-07-01 00:00:00,2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

}
