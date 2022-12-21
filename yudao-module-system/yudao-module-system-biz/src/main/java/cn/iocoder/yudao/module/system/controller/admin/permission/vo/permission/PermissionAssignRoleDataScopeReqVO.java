package cn.iocoder.yudao.module.system.controller.admin.permission.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

@Schema(description = "管理后台 - 赋予角色数据权限 Request VO")
@Data
public class PermissionAssignRoleDataScopeReqVO {

    @Schema(description = "角色编号", required = true, example = "1")
    @NotNull(message = "角色编号不能为空")
    private Long roleId;

    @Schema(description = "数据范围,参见 DataScopeEnum 枚举类", required = true, example = "1")
    @NotNull(message = "数据范围不能为空")
//    TODO 这里要多一个枚举校验
    private Integer dataScope;

    @Schema(description = "部门编号列表,只有范围类型为 DEPT_CUSTOM 时，该字段才需要", example = "1,3,5")
    private Set<Long> dataScopeDeptIds = Collections.emptySet(); // 兜底

}
