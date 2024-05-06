package cn.iocoder.yudao.module.ai.controller.admin.model.vo.role;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat 角色 - 更新
 *
 * @fansili
 * @since v1.0
 */
@Data
@Accessors(chain = true)
public class AiChatRoleUpdateReq extends PageParam {

    @NotNull
    @Schema(description = "角色名，角色的显示名称")
    private String name;

    @NotNull
    @Schema(description = "角色介绍，详细描述角色的功能或用途")
    private String introduce;

    @NotNull
    @Schema(description = "分类，角色所属的类别，如娱乐、创作等")
    private String classify;

    @NotNull
    @Schema(description = "开启状态 open、close")
    private String enable;
}
