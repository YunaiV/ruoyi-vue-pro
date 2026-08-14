package cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountuser;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsAccountUserLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - FMS 账套用户更新 Request VO")
@Data
public class FmsAccountUserUpdateReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "账套成员数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账套成员数组不能为空")
    @Size(max = 100, message = "单个账套最多授权 100 名成员")
    @Valid
    private List<Member> members;

    @Schema(description = "管理后台 - FMS 账套成员")
    @Data
    public static class Member {

        @Schema(description = "后台用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "成员用户编号不能为空")
        private Long userId;

        @Schema(description = "成员权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        @NotNull(message = "成员权限级别不能为空")
        @InEnum(FmsAccountUserLevelEnum.class)
        private Integer level;

    }

}
