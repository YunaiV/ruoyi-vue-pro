package cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.member;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 班组成员分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesCalTeamMemberPageReqVO extends PageParam {

    @Schema(description = "班组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "201")
    @NotNull(message = "班组编号不能为空")
    private Long teamId;

}
