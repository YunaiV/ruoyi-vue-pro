package cn.iocoder.yudao.module.ai.controller.admin.model.vo.role;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat 角色 - 修改可见性
 *
 * @fansili
 * @since v1.0
 */
@Data
@Accessors(chain = true)
public class AiChatRoleUpdatePublicStatusReqVO extends PageParam {


    @NotNull(message = "角色编号不能为空")
    @Schema(description = "角色编号")
    private Long id;

    @NotNull(message = "开启状态不能为空")
    @Schema(description = "开启状态 open、close")
    private Boolean publicStatus;
}
