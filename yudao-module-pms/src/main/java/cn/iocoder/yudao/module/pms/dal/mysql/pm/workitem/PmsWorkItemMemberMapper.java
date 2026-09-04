package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsWorkItemMemberMapper extends BaseMapperX<PmsWorkItemMemberDO> {

    default List<PmsWorkItemMemberDO> selectListByWorkItemIds(Collection<Long> workItemIds) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemMemberDO>()
                .in(PmsWorkItemMemberDO::getWorkItemId, workItemIds)
                .orderByAsc(PmsWorkItemMemberDO::getId));
    }

    default void deleteByWorkItemId(Long workItemId) {
        delete(PmsWorkItemMemberDO::getWorkItemId, workItemId);
    }

    default void deleteByProjectId(Long projectId) {
        delete(PmsWorkItemMemberDO::getProjectId, projectId);
    }

}
