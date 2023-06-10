package cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 积分签到规则创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SignInConfigCreateReqVO extends SignInConfigBaseVO {

}
