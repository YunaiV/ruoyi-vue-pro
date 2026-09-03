package cn.iocoder.yudao.module.pms.dal.mysql.pm.project;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsProjectMemberMapper extends BaseMapperX<PmsProjectMemberDO> {

    default List<PmsProjectMemberDO> selectListByUserId(Long userId) {
        return selectList(PmsProjectMemberDO::getUserId, userId);
    }

    default List<PmsProjectMemberDO> selectListByProjectId(Long projectId) {
        return selectList(PmsProjectMemberDO::getProjectId, projectId);
    }

    default PmsProjectMemberDO selectByProjectIdAndUserId(Long projectId, Long userId) {
        return selectFirstOne(PmsProjectMemberDO::getProjectId, projectId,
                PmsProjectMemberDO::getUserId, userId);
    }

    default List<PmsProjectMemberDO> selectListByUserIdAndLevels(Long userId, Collection<Integer> levels) {
        return selectList(new LambdaQueryWrapperX<PmsProjectMemberDO>()
                .eq(PmsProjectMemberDO::getUserId, userId)
                .in(PmsProjectMemberDO::getLevel, levels));
    }

    default List<PmsProjectMemberDO> selectListByProjectIds(Collection<Long> projectIds) {
        return selectList(new LambdaQueryWrapperX<PmsProjectMemberDO>()
                .in(PmsProjectMemberDO::getProjectId, projectIds));
    }

    default List<PmsProjectMemberDO> selectListByProjectIdsAndLevels(Collection<Long> projectIds,
                                                                     Collection<Integer> levels) {
        return selectList(new LambdaQueryWrapperX<PmsProjectMemberDO>()
                .in(PmsProjectMemberDO::getProjectId, projectIds)
                .in(PmsProjectMemberDO::getLevel, levels));
    }

    default void deleteByProjectIdAndUserIds(Long projectId, Collection<Long> userIds) {
        delete(new LambdaQueryWrapperX<PmsProjectMemberDO>()
                .eq(PmsProjectMemberDO::getProjectId, projectId)
                .in(PmsProjectMemberDO::getUserId, userIds));
    }

    default void deleteByProjectIdAndUserId(Long projectId, Long userId) {
        delete(new LambdaQueryWrapperX<PmsProjectMemberDO>()
                .eq(PmsProjectMemberDO::getProjectId, projectId)
                .eq(PmsProjectMemberDO::getUserId, userId));
    }

    default void deleteByProjectId(Long projectId) {
        delete(PmsProjectMemberDO::getProjectId, projectId);
    }

}
