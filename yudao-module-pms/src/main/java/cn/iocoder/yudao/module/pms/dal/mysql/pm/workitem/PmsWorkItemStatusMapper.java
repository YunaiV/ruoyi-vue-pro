package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemStatusDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsWorkItemStatusMapper extends BaseMapperX<PmsWorkItemStatusDO> {

    default List<PmsWorkItemStatusDO> selectListByProjectIdAndWorkItemType(Long projectId, Integer workItemType) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemStatusDO>()
                .eq(PmsWorkItemStatusDO::getProjectId, projectId)
                .eq(PmsWorkItemStatusDO::getWorkItemType, workItemType)
                .orderByAsc(PmsWorkItemStatusDO::getSort)
                .orderByAsc(PmsWorkItemStatusDO::getId));
    }

    default PmsWorkItemStatusDO selectDefaultByProjectIdAndWorkItemType(Long projectId, Integer workItemType) {
        return selectOne(new LambdaQueryWrapperX<PmsWorkItemStatusDO>()
                .eq(PmsWorkItemStatusDO::getProjectId, projectId)
                .eq(PmsWorkItemStatusDO::getWorkItemType, workItemType)
                .eq(PmsWorkItemStatusDO::getDefaultStatus, true));
    }

    default PmsWorkItemStatusDO selectByProjectIdAndWorkItemTypeAndName(Long projectId, Integer workItemType,
                                                                        String name) {
        return selectOne(new LambdaQueryWrapperX<PmsWorkItemStatusDO>()
                .eq(PmsWorkItemStatusDO::getProjectId, projectId)
                .eq(PmsWorkItemStatusDO::getWorkItemType, workItemType)
                .eq(PmsWorkItemStatusDO::getName, name));
    }

    default void updateDefaultStatusByProjectIdAndWorkItemType(Long projectId, Integer workItemType,
                                                               Long defaultStatusId) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemStatusDO>()
                .set(PmsWorkItemStatusDO::getDefaultStatus, false)
                .eq(PmsWorkItemStatusDO::getProjectId, projectId)
                .eq(PmsWorkItemStatusDO::getWorkItemType, workItemType));
        update(null, new LambdaUpdateWrapper<PmsWorkItemStatusDO>()
                .set(PmsWorkItemStatusDO::getDefaultStatus, true)
                .eq(PmsWorkItemStatusDO::getId, defaultStatusId)
                .eq(PmsWorkItemStatusDO::getProjectId, projectId)
                .eq(PmsWorkItemStatusDO::getWorkItemType, workItemType));
    }

    default void deleteByProjectId(Long projectId) {
        delete(PmsWorkItemStatusDO::getProjectId, projectId);
    }

    default void updateBoardNameByProjectIdAndWorkItemType(Long projectId, Integer workItemType, String boardName) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemStatusDO>()
                .set(PmsWorkItemStatusDO::getBoardName, boardName)
                .eq(PmsWorkItemStatusDO::getProjectId, projectId)
                .eq(PmsWorkItemStatusDO::getWorkItemType, workItemType));
    }

    default void updateBoardNameByIds(Collection<Long> ids, String boardName) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemStatusDO>()
                .set(PmsWorkItemStatusDO::getBoardName, boardName)
                .in(PmsWorkItemStatusDO::getId, ids));
    }

}
