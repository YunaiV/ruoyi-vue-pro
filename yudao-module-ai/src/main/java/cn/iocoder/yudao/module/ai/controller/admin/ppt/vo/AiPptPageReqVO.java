package cn.iocoder.yudao.module.ai.controller.admin.ppt.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - AI PPT 分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI PPT 分页 Request VO")
@Data
public class AiPptPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "标题，模糊匹配", example = "新能源汽车市场分析")
    private String title;

    @Schema(description = "状态", example = "20")
    @InEnum(AiPptStatusEnum.class)
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
