package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 销售线索分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesleadPageReqVO extends PageParam {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "销售线索来源")
    private String source;

    @Schema(description = "关键需求")
    private String requirement;

    @Schema(description = "预算(元)")
    private Long budget;

    @Schema(description = "报价")
    private Long quotation;

    @Schema(description = "报价状态", allowableValues = "'0', '1', '2'", example = "0: 全部。1：待报价。2：已报价。")
    private String quotationStatus;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "客户id", example = "11635")
    private Long customerId;

    @Schema(description = "项目id", example = "8951")
    private Long projectId;

    @Schema(description = "业务类型", example = "2")
    private String businessType;

    @Schema(description = "丢单的说明")
    private String lostNote;

    @Schema(description = "绑定的销售报价人员", example = "26885")
    private Long managerId;

}
