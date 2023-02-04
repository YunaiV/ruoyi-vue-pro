package cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 部门精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptSimpleRespVO {

    @Schema(description = "部门编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "部门名称", required = true, example = "芋道")
    private String name;

    @Schema(description = "父部门 ID", required = true, example = "1024")
    private Long parentId;

}
