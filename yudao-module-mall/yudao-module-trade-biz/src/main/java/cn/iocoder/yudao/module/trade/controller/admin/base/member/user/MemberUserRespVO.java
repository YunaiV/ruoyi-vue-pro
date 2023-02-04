package cn.iocoder.yudao.module.trade.controller.admin.base.member.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员用户 Response VO")
@Data
public class MemberUserRespVO {

    @Schema(description = "用户 ID", required = true, example = "1")
    private Long id;

    @Schema(description = "用户昵称", required = true, example = "芋道源码")
    private String nickname;

    @Schema(description = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

}
