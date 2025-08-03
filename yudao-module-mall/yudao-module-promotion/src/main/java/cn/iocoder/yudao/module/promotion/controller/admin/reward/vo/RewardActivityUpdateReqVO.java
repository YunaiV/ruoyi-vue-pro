package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 满减送活动更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RewardActivityUpdateReqVO extends RewardActivityBaseVO {

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "活动编号不能为空")
    private Long id;

}
