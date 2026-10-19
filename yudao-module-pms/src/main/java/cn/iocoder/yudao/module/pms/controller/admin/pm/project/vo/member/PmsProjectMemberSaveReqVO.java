package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.member;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectMemberLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 项目成员保存 Request VO")
@Data
public class PmsProjectMemberSaveReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "项目成员列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "项目成员不能为空")
    @Valid
    private List<Member> members;

    @Schema(description = "管理后台 - PMS 项目成员保存项")
    @Data
    public static class Member {

        @Schema(description = "后台用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "后台用户编号不能为空")
        private Long userId;

        @Schema(description = "成员权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        @NotNull(message = "成员权限级别不能为空")
        @InEnum(PmsProjectMemberLevelEnum.class)
        private Integer level;

    }

}
