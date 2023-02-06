package cn.iocoder.yudao.module.system.controller.admin.permission.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "管理后台 - 角色信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleRespVO extends RoleBaseVO {

    @Schema(description = "角色编号", required = true, example = "1")
    private Long id;

    @Schema(description = "数据范围,参见 DataScopeEnum 枚举类", required = true, example = "1")
    private Integer dataScope;

    @Schema(description = "数据范围(指定部门数组)", example = "1")
    private Set<Long> dataScopeDeptIds;

    @Schema(description = "状态,参见 CommonStatusEnum 枚举类", required = true, example = "1")
    private Integer status;

    @Schema(description = "角色类型,参见 RoleTypeEnum 枚举类", required = true, example = "1")
    private Integer type;

    @Schema(description = "创建时间", required = true, example = "时间戳格式")
    private LocalDateTime createTime;

}
