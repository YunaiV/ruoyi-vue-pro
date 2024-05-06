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
public class AiChatRoleUpdateVisibilityReq extends PageParam {

    @NotNull
    @Schema(description = "开启状态 open、close")
    private String enable;
}
