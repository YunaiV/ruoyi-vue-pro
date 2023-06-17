package cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 用户签到积分创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SignInRecordCreateReqVO extends SignInRecordBaseVO {

}
