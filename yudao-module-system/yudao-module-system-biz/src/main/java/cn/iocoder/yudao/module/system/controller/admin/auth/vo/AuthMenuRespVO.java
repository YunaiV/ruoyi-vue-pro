package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "管理后台 - 登录用户的菜单信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthMenuRespVO {

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private Long id;

    @Schema(description = "父菜单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long parentId;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String name;

    @Schema(description = "路由地址,仅菜单类型为菜单或者目录时，才需要传", example = "post")
    private String path;

    @Schema(description = "组件路径,仅菜单类型为菜单时，才需要传", example = "system/post/index")
    private String component;

    @Schema(description = "组件名", example = "SystemUser")
    private String componentName;

    @Schema(description = "菜单图标,仅菜单类型为菜单或者目录时，才需要传", example = "/menu/list")
    private String icon;

    @Schema(description = "是否可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean visible;

    @Schema(description = "是否缓存", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean keepAlive;

    @Schema(description = "是否总是显示", example = "false")
    private Boolean alwaysShow;

    /**
     * 子路由
     */
    private List<AuthMenuRespVO> children;

}
