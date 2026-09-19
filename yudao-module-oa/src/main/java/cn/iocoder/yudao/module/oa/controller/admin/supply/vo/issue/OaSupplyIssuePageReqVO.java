package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.oa.enums.supply.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 领用发放分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaSupplyIssuePageReqVO extends PageParam {

    @Schema(description = "物品名称", example = "A4 打印纸")
    private String itemName;

    @Schema(description = "申请人", example = "张三")
    private String creatorName;

    @Schema(description = "管理类型", example = "1")
    private Integer manageType;

    @Schema(description = "使用类型", example = "1")
    private Integer useType;

    @Schema(description = "明细状态", example = "0")
    private Integer status;

    @Schema(description = "申请时间", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
