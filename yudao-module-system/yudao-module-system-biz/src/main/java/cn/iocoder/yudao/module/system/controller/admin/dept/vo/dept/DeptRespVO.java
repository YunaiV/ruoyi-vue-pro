package cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 部门信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptRespVO extends DeptBaseVO {

    @Schema(title = "部门编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "状态", required = true, example = "1", description = "参见 CommonStatusEnum 枚举类")
    private Integer status;

    @Schema(title = "创建时间", required = true, example = "时间戳格式")
    private LocalDateTime createTime;

}
