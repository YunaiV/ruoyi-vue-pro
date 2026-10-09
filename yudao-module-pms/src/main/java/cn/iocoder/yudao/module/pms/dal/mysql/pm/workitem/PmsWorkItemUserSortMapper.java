package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemUserSortDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    // 个人排序无需保留删除历史，物理删除避免重复排序时逻辑删除记录发生唯一键冲突
    @Delete("DELETE FROM pms_work_item_user_sort WHERE project_id = #{projectId} AND user_id = #{userId}")
    void deleteByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Delete("DELETE FROM pms_work_item_user_sort WHERE work_item_id = #{workItemId}")
    void deleteByWorkItemId(@Param("workItemId") Long workItemId);

    @Delete("DELETE FROM pms_work_item_user_sort WHERE project_id = #{projectId}")
    void deleteByProjectId(@Param("projectId") Long projectId);

}
