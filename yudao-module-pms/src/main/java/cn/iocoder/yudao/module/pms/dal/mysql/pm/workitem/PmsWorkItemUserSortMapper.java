package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemUserSortDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PmsWorkItemUserSortMapper extends BaseMapperX<PmsWorkItemUserSortDO> {

    default List<PmsWorkItemUserSortDO> selectListByProjectIdAndUserId(Long projectId, Long userId) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemUserSortDO>()
                .eq(PmsWorkItemUserSortDO::getProjectId, projectId)
                .eq(PmsWorkItemUserSortDO::getUserId, userId)
                .orderByAsc(PmsWorkItemUserSortDO::getSort)
                .orderByAsc(PmsWorkItemUserSortDO::getId));
    }

    default void deleteByProjectIdAndUserId(Long projectId, Long userId) {
        delete(new LambdaQueryWrapperX<PmsWorkItemUserSortDO>()
                .eq(PmsWorkItemUserSortDO::getProjectId, projectId)
                .eq(PmsWorkItemUserSortDO::getUserId, userId));
    }

    default void deleteByWorkItemId(Long workItemId) {
        delete(new LambdaQueryWrapperX<PmsWorkItemUserSortDO>()
                .eq(PmsWorkItemUserSortDO::getWorkItemId, workItemId));
    }

    default void deleteByProjectId(Long projectId) {
        delete(new LambdaQueryWrapperX<PmsWorkItemUserSortDO>()
                .eq(PmsWorkItemUserSortDO::getProjectId, projectId));
    }

}
