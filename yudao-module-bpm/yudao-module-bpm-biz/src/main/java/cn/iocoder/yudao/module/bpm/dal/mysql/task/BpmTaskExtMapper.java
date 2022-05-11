package cn.iocoder.yudao.module.bpm.dal.mysql.task;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface BpmTaskExtMapper extends BaseMapperX<BpmTaskExtDO> {

    default void updateByTaskId(BpmTaskExtDO entity) {
        update(entity, new LambdaQueryWrapper<BpmTaskExtDO>().eq(BpmTaskExtDO::getTaskId, entity.getTaskId()));
    }

    default List<BpmTaskExtDO> selectListByTaskIds(Collection<String> taskIds) {
        return selectList(BpmTaskExtDO::getTaskId, taskIds);
    }

    /**
     * 查询任务
     *
     * @param procInstId 流程id
     *
     * @return 返回任务列表
     */
    @TenantIgnore
    List<BpmTaskExtDO> listByProcInstId(@Param("procInstId") String procInstId);

    default List<BpmTaskExtDO> selectListByProcessInstanceId(String processInstanceId) {
        return selectList("process_instance_id", processInstanceId);
    }

    /**
     * 删除非当前相同taskDefKey非进行中的任务
     *
     * @param entity 任务信息
     */
    void delTaskByProcInstIdAndTaskIdAndTaskDefKey(@Param("entity") BpmTaskExtDO entity);

    /**
     * 任务驳回
     *
     * @param taskId  任务列表
     * @param comment 驳回理由
     *
     * @return 返回驳回结果，是否成功
     */
    Boolean backByTaskId(@Param("taskId") String taskId, @Param("comment") String comment);

    /**
     * 逻辑删除任务
     *
     * @param taskIdList 任务id列表
     *
     * @return 返回是否成功
     */
    Boolean delByTaskIds(@Param("taskIdList") List<String> taskIdList);
}
