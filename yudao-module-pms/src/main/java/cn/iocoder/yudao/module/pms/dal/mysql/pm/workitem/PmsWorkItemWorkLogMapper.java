package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemWorkLogDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsWorkItemWorkLogMapper extends BaseMapperX<PmsWorkItemWorkLogDO> {

    default List<PmsWorkItemWorkLogDO> selectListByWorkItemId(Long workItemId) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemWorkLogDO>()
                .eq(PmsWorkItemWorkLogDO::getWorkItemId, workItemId)
                .orderByDesc(PmsWorkItemWorkLogDO::getCreateTime)
                .orderByDesc(PmsWorkItemWorkLogDO::getId));
    }

    default List<PmsWorkItemWorkLogDO> selectListByWorkItemIds(Collection<Long> workItemIds) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemWorkLogDO>()
                .in(PmsWorkItemWorkLogDO::getWorkItemId, workItemIds)
                .orderByAsc(PmsWorkItemWorkLogDO::getCreateTime)
                .orderByAsc(PmsWorkItemWorkLogDO::getId));
    }

    default List<PmsWorkItemWorkLogDO> selectListByProjectIdAndCreateTimeBetween(
            Long projectId, LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemWorkLogDO>()
                .eq(PmsWorkItemWorkLogDO::getProjectId, projectId)
                .between(PmsWorkItemWorkLogDO::getCreateTime, beginTime, endTime)
                .orderByAsc(PmsWorkItemWorkLogDO::getCreateTime)
                .orderByAsc(PmsWorkItemWorkLogDO::getId));
    }

    default int updateForEdit(PmsWorkItemWorkLogDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<PmsWorkItemWorkLogDO>()
                .set(updateObj.getDescription() == null, PmsWorkItemWorkLogDO::getDescription, null)
                .eq(PmsWorkItemWorkLogDO::getId, updateObj.getId()));
    }

    default void deleteByProjectId(Long projectId) {
        delete(PmsWorkItemWorkLogDO::getProjectId, projectId);
    }

    default void deleteByWorkItemId(Long workItemId) {
        delete(PmsWorkItemWorkLogDO::getWorkItemId, workItemId);
    }

}
