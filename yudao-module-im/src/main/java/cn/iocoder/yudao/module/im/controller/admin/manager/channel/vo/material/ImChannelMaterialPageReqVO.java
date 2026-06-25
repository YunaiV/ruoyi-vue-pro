package cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IM 频道素材分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImChannelMaterialPageReqVO extends PageParam {

    @Schema(description = "频道编号", example = "1")
    private Long channelId;

    @Schema(description = "内容类型", example = "1")
    private Integer type; // 参见 ImChannelMaterialTypeEnum 枚举类

    @Schema(description = "标题", example = "活动")
    private String title;

    @Schema(description = "创建时间", example = "[2026-04-01 00:00:00, 2026-04-30 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
