package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PmsWorkItemBoardMapper extends BaseMapperX<PmsWorkItemBoardDO> {

    default List<PmsWorkItemBoardDO> selectListByProjectIdAndWorkItemType(Long projectId, Integer workItemType) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemBoardDO>()
                .eq(PmsWorkItemBoardDO::getProjectId, projectId)
                .eq(PmsWorkItemBoardDO::getWorkItemType, workItemType)
                .orderByAsc(PmsWorkItemBoardDO::getSort)
                .orderByAsc(PmsWorkItemBoardDO::getId));
    }

    default void deleteByProjectIdAndWorkItemType(Long projectId, Integer workItemType) {
        delete(new LambdaQueryWrapperX<PmsWorkItemBoardDO>()
                .eq(PmsWorkItemBoardDO::getProjectId, projectId)
                .eq(PmsWorkItemBoardDO::getWorkItemType, workItemType));
    }

    default void deleteByProjectId(Long projectId) {
        delete(PmsWorkItemBoardDO::getProjectId, projectId);
    }

}
