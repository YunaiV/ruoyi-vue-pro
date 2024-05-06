package cn.iocoder.yudao.module.ai.controller.admin.model.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat 角色
 *
 * @fansili
 * @since v1.0
 */
@Data
@Accessors(chain = true)
public class AiChatRoleRes {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "角色名字")
    private String name;

    @Schema(description = "角色介绍，详细描述角色的功能或用途")
    private String introduce;

    @Schema(description = "分类，角色所属的类别，如娱乐、创作等")
    private String classify;

    @Schema(description = "状态 open、close")
    private String enable;

    @Schema(description = "角色的使用次数统计")
    private Integer useCount;
}
