package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemCommentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsWorkItemCommentMapper extends BaseMapperX<PmsWorkItemCommentDO> {

    default List<PmsWorkItemCommentDO> selectListByWorkItemId(Long workItemId) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemCommentDO>()
                .eq(PmsWorkItemCommentDO::getWorkItemId, workItemId)
                .orderByAsc(PmsWorkItemCommentDO::getCreateTime)
                .orderByAsc(PmsWorkItemCommentDO::getId));
    }

    default void deleteByMainId(Long mainId) {
        delete(PmsWorkItemCommentDO::getMainId, mainId);
    }

    default void deleteByWorkItemId(Long workItemId) {
        delete(PmsWorkItemCommentDO::getWorkItemId, workItemId);
    }

    default void deleteByWorkItemIds(Collection<Long> workItemIds) {
        delete(new LambdaQueryWrapperX<PmsWorkItemCommentDO>()
                .in(PmsWorkItemCommentDO::getWorkItemId, workItemIds));
    }

}
