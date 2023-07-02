package cn.iocoder.yudao.module.member.controller.admin.signin.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 积分签到规则 Excel 导出 Request VO，参数和 SignInConfigPageReqVO 是一致的")
@Data
public class MemberSignInConfigExportReqVO {

    @Schema(description = "签到第x天", example = "7")
    private Integer day;

}
