package cn.iocoder.yudao.module.bpm.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceResultEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

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

    // TODO @海：BpmProcessInstanceResultEnum.CAN_SUB_SIGN_STATUS_LIST) 应该作为条件，mapper 不要有业务
    default List<BpmTaskExtDO> selectProcessListByTaskIds(Collection<String> taskIds) {
        return selectList(new LambdaQueryWrapperX<BpmTaskExtDO>()
                .in(BpmTaskExtDO::getTaskId, taskIds)
                .in(BpmTaskExtDO::getResult, BpmProcessInstanceResultEnum.CAN_SUB_SIGN_STATUS_LIST));
    }

    default BpmTaskExtDO selectByTaskId(String taskId) {
        return selectOne(BpmTaskExtDO::getTaskId, taskId);
    }

    default void updateBatchByTaskIdList(List<String> taskIdList, BpmTaskExtDO updateObj) {
        update(updateObj, new LambdaQueryWrapper<BpmTaskExtDO>().in(BpmTaskExtDO::getTaskId, taskIdList));
    }

}
