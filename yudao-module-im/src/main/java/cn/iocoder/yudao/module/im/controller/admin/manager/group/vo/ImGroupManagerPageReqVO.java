package cn.iocoder.yudao.module.im.controller.admin.manager.group.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IM 群聊分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImGroupManagerPageReqVO extends PageParam {

    @Schema(description = "群名称，模糊匹配", example = "技术交流群")
    private String name;

    @Schema(description = "群主用户编号", example = "1024")
    private Long ownerUserId;

    @Schema(description = "群状态，参见 CommonStatusEnum 枚举类（0 正常 / 1 已解散）", example = "0")
    private Integer status;

    @Schema(description = "是否封禁", example = "false")
    private Boolean banned;

    // TODO DONE @AI：这个不太对，参考别的模块
    @Schema(description = "创建时间", example = "[2026-04-01 00:00:00, 2026-04-30 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
