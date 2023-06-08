package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 满减送活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RewardActivityPageReqVO extends PageParam {

    @Schema(description = "活动标题", example = "满啦满啦")
    private String name;

    @Schema(description = "活动状态", example = "1")
    private Integer status;

}
