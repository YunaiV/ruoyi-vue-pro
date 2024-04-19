package cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户与呼叫中心用户绑定关系分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCallcenterUserPageReqVO extends PageParam {

    @Schema(description = "用户id", example = "2642")
    private Long userId;

    @Schema(description = "用户名称", example = "2642")
    private String nickName;

    @Schema(description = "云客用户id", example = "23969")
    private String yunkeCallcenterUserId;

    @Schema(description = "云客手机号")
    private String yunkeCallcenterPhone;

    @Schema(description = "连连用户id", example = "18461")
    private String lianlianCallcenterUserId;

    @Schema(description = "连连手机号")
    private String lianlianCallcenterPhone;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}