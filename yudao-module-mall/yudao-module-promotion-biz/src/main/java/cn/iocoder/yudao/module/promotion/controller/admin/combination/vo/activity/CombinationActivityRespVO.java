package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 拼团活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CombinationActivityRespVO extends CombinationActivityBaseVO {

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22901")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "购买人数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "购买人数不能为空")
    private Integer userSize;

    @Schema(description = "开团组数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开团组数不能为空")
    private Integer totalNum;

    @Schema(description = "成团组数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成团组数不能为空")
    private Integer successNum;

    @Schema(description = "虚拟成团", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "虚拟成团不能为空")
    private Integer virtualGroup;

    @Schema(description = "活动状态：0开启 1关闭", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "活动状态：0开启 1关闭不能为空")
    private Integer status;

}
