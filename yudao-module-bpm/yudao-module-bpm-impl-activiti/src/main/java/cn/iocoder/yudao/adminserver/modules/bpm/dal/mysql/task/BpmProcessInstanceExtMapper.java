package cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceMyPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmProcessInstanceExtDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BpmProcessInstanceExtMapper extends BaseMapperX<BpmProcessInstanceExtDO> {

    default PageResult<BpmProcessInstanceExtDO> selectPage(Long userId, BpmProcessInstanceMyPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<BpmProcessInstanceExtDO>()
                .eqIfPresent("start_user_id", userId)
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("process_definition_id", reqVO.getProcessDefinitionId())
                .eqIfPresent("category", reqVO.getCategory())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("result", reqVO.getResult())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default BpmProcessInstanceExtDO selectByProcessInstanceId(String processDefinitionId) {
        return selectOne("process_instance_id", processDefinitionId);
    }

    default void updateByProcessInstanceId(BpmProcessInstanceExtDO updateObj) {
        update(updateObj, new QueryWrapper<BpmProcessInstanceExtDO>()
                .eq("process_instance_id", updateObj.getProcessInstanceId()));
    }

}
