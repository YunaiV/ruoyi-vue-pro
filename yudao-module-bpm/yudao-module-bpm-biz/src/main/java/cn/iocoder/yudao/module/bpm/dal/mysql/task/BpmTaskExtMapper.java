package cn.iocoder.yudao.module.bpm.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
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

    default BpmTaskExtDO selectByTaskId(String taskId) {
        return selectOne(BpmTaskExtDO::getTaskId, taskId);
    }

    default List<BpmTaskExtDO> selectListByProcessInstanceId(String processInstanceId) {
        return selectList(BpmTaskExtDO::getProcessInstanceId, processInstanceId);
    }

    /**
     * 任务驳回
     *
     * @param taskId  任务列表
     * @param reason 驳回理由
     *
     * @return 返回驳回结果，是否成功
     */
    Boolean backByTaskId(@Param("taskId") String taskId, @Param("reason") String reason);

    /**
     * 逻辑删除任务
     *
     * @param taskIdList 任务id列表
     *
     * @return 返回是否成功
     */
    Boolean delByTaskIds(@Param("taskIdList") List<String> taskIdList);
}
