package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Schema(description = "管理后台 - 用户组创建/修改 Request VO")
@Data
public class BpmUserGroupSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "组名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    @NotNull(message = "组名不能为空")
    private String name;

    @Schema(description = "描述", example = "芋道源码")
    private String description;

    @Schema(description = "成员编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2,3")
    @NotNull(message = "成员编号数组不能为空")
    private Set<Long> userIds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
