package cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 积分签到规则分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SignInConfigPageReqVO extends PageParam {

    @Schema(description = "签到第x天", example = "7")
    private Integer day;

}
