package cn.iocoder.yudao.module.ai.vo;

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
public class ChatRoleUpdateVisibilityReq extends PageParam {

    @NotNull
    @Schema(description = "编号")
    private Long id;

    @NotNull
    @Schema(description = "发布状态，0表示仅自己可见，1表示公开，2表示禁用")
    private String visibility;
}
