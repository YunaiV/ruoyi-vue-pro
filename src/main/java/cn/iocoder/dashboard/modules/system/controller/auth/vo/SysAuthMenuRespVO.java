package cn.iocoder.dashboard.modules.system.controller.auth.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("登陆用户的菜单信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysAuthMenuRespVO {

    /**
     * 菜单编号
     */
    private Long menuId;
    /**
     * 父菜单编号
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 路由地址
     */
    private String path;
    /**
     * 组件地址
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 子路由
     */
    private List<SysAuthMenuRespVO> children;

}
