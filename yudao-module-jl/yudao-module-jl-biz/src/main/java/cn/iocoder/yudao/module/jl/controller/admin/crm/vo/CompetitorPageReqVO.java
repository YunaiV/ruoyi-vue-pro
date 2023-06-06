package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 友商分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CompetitorPageReqVO extends PageParam {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "公司名", example = "惟象科技")
    private String name;

    @Schema(description = "联系人", example = "dev")
    private String contactName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "友商类型", example = "请选择")
    private String type;

    @Schema(description = "优势")
    private String advantage;

    @Schema(description = "劣势")
    private String disadvantage;

    @Schema(description = "备注")
    private String mark;

}
