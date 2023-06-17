package cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 积分设置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointConfigPageReqVO extends PageParam {

    @Schema(description = "1 开启积分抵扣 0 关闭积分抵扣", example = "1")
    private Integer tradeDeductEnable;

}
