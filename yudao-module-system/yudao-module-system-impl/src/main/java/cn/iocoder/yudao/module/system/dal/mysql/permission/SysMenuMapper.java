package cn.iocoder.yudao.module.system.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.enums.SqlConstants;
import cn.iocoder.yudao.module.system.controller.permission.vo.menu.SysMenuListReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.SysMenuDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapperX<SysMenuDO> {

    default SysMenuDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(new LambdaQueryWrapper<SysMenuDO>().eq(SysMenuDO::getParentId, parentId)
                .eq(SysMenuDO::getName, name));
    }

    default Integer selectCountByParentId(Long parentId) {
        return selectCount(SysMenuDO::getParentId, parentId);
    }

    default List<SysMenuDO> selectList(SysMenuListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SysMenuDO>().likeIfPresent(SysMenuDO::getName, reqVO.getName())
                .eqIfPresent(SysMenuDO::getStatus, reqVO.getStatus()));
    }

    default boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return selectOne(new LambdaQueryWrapper<SysMenuDO>().select(SysMenuDO::getId)
                .gt(SysMenuDO::getUpdateTime, maxUpdateTime).last(SqlConstants.LIMIT1)) != null;
    }

}
