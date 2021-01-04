package cn.iocoder.dashboard.modules.system.convert.auth;

import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthGetInfoRespVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthGetRouterRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysAuthConvert {

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

    SysAuthGetRouterRespVO convertTreeNode(SysMenuDO menu);

}
