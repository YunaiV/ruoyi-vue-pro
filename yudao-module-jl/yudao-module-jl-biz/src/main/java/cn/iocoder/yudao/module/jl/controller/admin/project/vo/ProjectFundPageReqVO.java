package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 项目款项分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectFundPageReqVO extends PageParam {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "名称", example = "王五")
    private String name;

    @Schema(description = "资金额度", example = "23415")
    private Long price;

    @Schema(description = "项目 id", example = "27211")
    private Long projectId;

    @Schema(description = "支付状态(未支付，部分支付，完全支付)", example = "2")
    private String status;

    @Schema(description = "支付时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] paidTime;

    @Schema(description = "支付的截止时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] deadline;

    @Schema(description = "支付凭证上传地址", example = "https://www.iocoder.cn")
    private String receiptUrl;

    @Schema(description = "支付凭证文件名称", example = "芋艿")
    private String receiptName;

    @Schema(description = "排序")
    private Integer sort;

}
