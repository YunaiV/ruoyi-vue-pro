package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(title = "管理后台 - 登录用户的菜单信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthMenuRespVO {

    @Schema(title = "菜单名称", required = true, example = "芋道")
    private Long id;

    @Schema(title = "父菜单 ID", required = true, example = "1024")
    private Long parentId;

    @Schema(title = "菜单名称", required = true, example = "芋道")
    private String name;

    @Schema(title = "路由地址", example = "post", description = "仅菜单类型为菜单或者目录时，才需要传")
    private String path;

    @Schema(title = "组件路径", example = "system/post/index", description = "仅菜单类型为菜单时，才需要传")
    private String component;

    @Schema(title = "菜单图标", example = "/menu/list", description = "仅菜单类型为菜单或者目录时，才需要传")
    private String icon;

    @Schema(title = "是否可见", required = true, example = "false")
    private Boolean visible;

    @Schema(title = "是否缓存", required = true, example = "false")
    private Boolean keepAlive;

    /**
     * 子路由
     */
    private List<AuthMenuRespVO> children;

}
