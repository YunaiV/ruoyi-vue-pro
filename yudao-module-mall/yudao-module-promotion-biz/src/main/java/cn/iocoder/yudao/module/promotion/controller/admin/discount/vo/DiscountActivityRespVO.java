package cn.iocoder.yudao.module.promotion.controller.admin.discount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(title = "管理后台 - 限时折扣活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DiscountActivityRespVO extends DiscountActivityBaseVO {

    @Schema(title = "活动编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "活动状态", required = true, example = "1")
    @NotNull(message = "活动状态不能为空")
    private Integer status;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
