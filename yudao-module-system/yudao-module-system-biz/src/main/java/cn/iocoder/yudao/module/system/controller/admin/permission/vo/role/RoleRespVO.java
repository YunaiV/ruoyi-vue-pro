package cn.iocoder.yudao.module.system.controller.admin.permission.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(title = "管理后台 - 角色信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleRespVO extends RoleBaseVO {

    @Schema(title = "角色编号", required = true, example = "1")
    private Long id;

    @Schema(title = "数据范围", required = true, example = "1", description = "参见 DataScopeEnum 枚举类")
    private Integer dataScope;

    @Schema(title = "数据范围(指定部门数组)", example = "1")
    private Set<Long> dataScopeDeptIds;

    @Schema(title = "状态", required = true, example = "1", description = "参见 CommonStatusEnum 枚举类")
    private Integer status;

    @Schema(title = "角色类型", required = true, example = "1", description = "参见 RoleTypeEnum 枚举类")
    private Integer type;

    @Schema(title = "创建时间", required = true, example = "时间戳格式")
    private LocalDateTime createTime;

}
