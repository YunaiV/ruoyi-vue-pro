package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 撤销群管理员 Request VO")
@Data
public class ImGroupAdminRemoveReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13279")
    @NotNull(message = "群编号不能为空")
    private Long id;

    @Schema(description = "目标用户编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[101, 102]")
    @NotEmpty(message = "目标用户编号列表不能为空")
    private List<Long> userIds;

}
