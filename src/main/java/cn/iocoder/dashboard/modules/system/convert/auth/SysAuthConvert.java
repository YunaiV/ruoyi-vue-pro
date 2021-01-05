package cn.iocoder.dashboard.modules.system.convert.auth;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthGetInfoRespVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthGetRouterRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.enums.permission.MenuIdEnum;
import cn.iocoder.dashboard.modules.system.enums.permission.MenuTypeEnum;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysAuthConvert {

    /** Layout组件标识 */
    public final static String LAYOUT = "Layout";

    /** ParentView组件标识 */
    public final static String PARENT_VIEW = "ParentView";

    SysAuthConvert INSTANCE = Mappers.getMapper(SysAuthConvert.class);

    @Mapping(source = "updateTime", target = "updateTime", ignore = true) // 字段相同，但是含义不同，忽略
    LoginUser convert(SysUserDO bean);

    default SysAuthGetInfoRespVO convert(SysUserDO user, List<SysRoleDO> roleList, List<SysMenuDO> menuList) {
        return SysAuthGetInfoRespVO.builder()
                .user(SysAuthGetInfoRespVO.UserVO.builder().nickname(user.getNickname()).avatar(user.getAvatar()).build())
                .roles(CollectionUtils.convertSet(roleList, SysRoleDO::getRoleKey))
                .permissions(CollectionUtils.convertSet(menuList, SysMenuDO::getPerms))
                .build();
    }

    default SysAuthGetRouterRespVO convertTreeNode(SysMenuDO menu) {
        SysAuthGetRouterRespVO.SysAuthGetRouterRespVOBuilder respVOBuilder = SysAuthGetRouterRespVO.builder();
        respVOBuilder.menuId(menu.getMenuId()).parentId(menu.getParentId())
                .hidden("1".equals(menu.getVisible())) // TODO 芋艿：需要处理
                .name(getRouteName(menu)).path(menu.getPath()).component(getComponent(menu))
                .meta(SysAuthGetRouterRespVO.MetaVO.builder().title(menu.getMenuName()).icon(menu.getIcon())
                        .noCache("1".equals(menu.getIsCache())).build())
        ;
        return respVOBuilder.build();
    }



    // TODO 芋艿：需要预处理。存储的时候
    default String getRouteName(SysMenuDO menu) {
        String routerName = StringUtil.capitalize(menu.getPath()); // TODO 芋艿：看看怎么去除掉
        // 非外链并且是一级目录（类型为目录）
        if (isMenuFrame(menu)) {
            routerName = StringUtils.EMPTY;
        }
        return routerName;
    }

    /**
     * 是否为菜单内部跳转
     *
     * @param menu 菜单信息
     * @return 是否
     */
    // TODO 芋艿：思考下是不是可以重构下
    default boolean isMenuFrame(SysMenuDO menu) {
        return MenuIdEnum.ROOT.getId().equals(menu.getParentId())
                && MenuTypeEnum.MENU.getType().equals(menu.getMenuType())
                && CommonStatusEnum.ENABLE.getStatus().equals(menu.getIsFrame()); // TODO 芋艿：思考是不是用这个好
    }

//    /** // TODO 芋艿：后面重构下
//     * 获取路由地址
//     *
//     * @param menu 菜单信息
//     * @return 路由地址
//     */
//    default String getRouterPath(SysMenuDO menu) {
//        String routerPath = menu.getPath();
//        // 非外链并且是一级目录（类型为目录）
//        if (MenuIdEnum.ROOT.getId().equals(menu.getParentId())
//                && MenuTypeEnum.MENU.getType().equals(menu.getMenuType())
//                && CommonStatusEnum.DISABLE.getStatus().equals(menu.getIsFrame())) {
//            routerPath = "/" + menu.getPath();
//        }
//        // 非外链并且是一级目录（类型为菜单）
//        else if (isMenuFrame(menu)) {
//            routerPath = "/";
//        }
//        return routerPath;
//    }

    /**
     * 获取组件信息
     *
     * @param menu 菜单信息
     * @return 组件信息
     */
    default String getComponent(SysMenuDO menu) {
        String component = LAYOUT;
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu)) {
            component = menu.getComponent();
        } else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu)) {
            component = PARENT_VIEW;
        }
        return component;
    }

    /**
     * 是否为parent_view组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    default boolean isParentView(SysMenuDO menu) {
        return !MenuIdEnum.ROOT.getId().equals(menu.getParentId())
                && MenuTypeEnum.DIR.getType().equals(menu.getMenuType());
    }

}
