package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 满减送活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RewardActivityRespVO extends RewardActivityBaseVO {

    @Schema(title = "活动编号", required = true, example = "1024")
    private Integer id;

    @Schema(title = "活动状态", required = true, example = "1")
    private Integer status;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
