package cn.iocoder.yudao.module.member.controller.admin.signin.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 积分签到规则分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberSignInConfigPageReqVO extends PageParam {

    @Schema(description = "签到第x天", example = "7")
    private Integer day;

}
