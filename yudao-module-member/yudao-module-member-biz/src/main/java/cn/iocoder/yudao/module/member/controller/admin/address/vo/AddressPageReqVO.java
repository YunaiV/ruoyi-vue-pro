package cn.iocoder.yudao.module.member.controller.admin.address.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户收件地址分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AddressPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "20369")
    private Long userId;

    @Schema(description = "收件人名称", example = "张三")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "地区编码", example = "15716")
    private Long areaId;

    @Schema(description = "收件详细地址")
    private String detailAddress;

    @Schema(description = "是否默认", example = "2")
    private Boolean defaultStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
