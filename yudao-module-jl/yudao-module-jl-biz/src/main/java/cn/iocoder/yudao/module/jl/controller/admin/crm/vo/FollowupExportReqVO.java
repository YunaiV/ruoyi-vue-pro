package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 销售线索跟进，可以是跟进客户，也可以是跟进线索 Excel 导出 Request VO，参数和 FollowupPageReqVO 是一致的")
@Data
public class FollowupExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "客户id", example = "24011")
    private Long customerId;

    @Schema(description = "跟进实体的 id，项目、线索、款项，客户等", example = "29426")
    private Long refId;

    @Schema(description = "跟进类型：日常联系、销售线索、催款等")
    private Integer type;

}
