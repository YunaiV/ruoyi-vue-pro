package cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 班组成员 Response VO")
@Data
public class MesCalTeamMemberRespVO {

    @Schema(description = "班组成员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "班组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "201")
    private Long teamId;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "管理员")
    private String nickname;

    @Schema(description = "电话", example = "13800138000")
    private String telephone;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
