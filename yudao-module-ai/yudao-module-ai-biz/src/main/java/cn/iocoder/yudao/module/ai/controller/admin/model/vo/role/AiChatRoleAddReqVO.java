package cn.iocoder.yudao.module.ai.controller.admin.model.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat 角色列表
 *
 * @fansili
 * @since v1.0
 */
@Data
@Accessors(chain = true)
public class AiChatRoleAddReqVO {

    @NotNull
    @Schema(description = "角色名，角色的显示名称")
    private String name;

    @Schema(description = "头像")
    private String avatar;

    @NotNull
    @Schema(description = "分类，角色所属的类别，如娱乐、创作等")
    private String category;

    @NotNull
    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "角色欢迎语")
    private String welcomeMessage;

    @NotNull
    @Schema(description = "角色设定（消息）")
    private String systemMessage;

    @NotNull
    @Schema(description = "模型编号")
    private Long modelId;

    @NotNull
    @Schema(description = "开启状态 0、open 1、close")
    private Boolean publicStatus;

    @NotNull
    @Schema(description = "排序")
    private Integer sort;

}
