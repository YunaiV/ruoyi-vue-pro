package cn.iocoder.yudao.module.pms.dal.mysql.pm.project;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsProjectGroupMapper extends BaseMapperX<PmsProjectGroupDO> {

    default PmsProjectGroupDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(new LambdaQueryWrapperX<PmsProjectGroupDO>()
                .eq(PmsProjectGroupDO::getId, id)
                .eq(PmsProjectGroupDO::getUserId, userId));
    }

    default PmsProjectGroupDO selectByUserIdAndType(Long userId, Integer type) {
        return selectOne(new LambdaQueryWrapperX<PmsProjectGroupDO>()
                .eq(PmsProjectGroupDO::getUserId, userId)
                .eq(PmsProjectGroupDO::getType, type));
    }

    default PmsProjectGroupDO selectByUserIdAndName(Long userId, String name, Long excludeId) {
        return selectOne(new LambdaQueryWrapperX<PmsProjectGroupDO>()
                .eq(PmsProjectGroupDO::getUserId, userId)
                .eq(PmsProjectGroupDO::getName, name)
                .neIfPresent(PmsProjectGroupDO::getId, excludeId));
    }

    default List<PmsProjectGroupDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PmsProjectGroupDO>()
                .eq(PmsProjectGroupDO::getUserId, userId)
                .orderByAsc(PmsProjectGroupDO::getSort)
                .orderByAsc(PmsProjectGroupDO::getId));
    }

    default List<PmsProjectGroupDO> selectListByIdsAndUserId(Collection<Long> ids, Long userId) {
        return selectList(new LambdaQueryWrapperX<PmsProjectGroupDO>()
                .in(PmsProjectGroupDO::getId, ids)
                .eq(PmsProjectGroupDO::getUserId, userId));
    }

    default PmsProjectGroupDO selectLastByUserId(Long userId) {
        return selectLastOne(new LambdaQueryWrapperX<PmsProjectGroupDO>()
                .eq(PmsProjectGroupDO::getUserId, userId)
                .orderByAsc(PmsProjectGroupDO::getSort)
                .orderByAsc(PmsProjectGroupDO::getId));
    }

}
