package cn.iocoder.yudao.module.member.controller.admin.signin.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 积分签到规则创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberSignInConfigCreateReqVO extends MemberSignInConfigBaseVO {

}
