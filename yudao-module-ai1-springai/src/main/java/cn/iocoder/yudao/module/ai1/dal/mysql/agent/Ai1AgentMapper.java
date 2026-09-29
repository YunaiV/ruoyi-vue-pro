package cn.iocoder.yudao.module.ai1.dal.mysql.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI1 Agent Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1AgentMapper extends BaseMapperX<Ai1AgentDO> {

    default PageResult<Ai1AgentDO> selectPage(Ai1AgentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1AgentDO>()
                .likeIfPresent(Ai1AgentDO::getName, reqVO.getName())
                .eqIfPresent(Ai1AgentDO::getStatus, reqVO.getStatus())
                .orderByAsc(Ai1AgentDO::getId));
    }

    default List<Ai1AgentDO> selectListByStatusOrderById(Integer status) {
        return selectList(new LambdaQueryWrapperX<Ai1AgentDO>()
                .eq(Ai1AgentDO::getStatus, status)
                .orderByAsc(Ai1AgentDO::getId));
    }

    default Long selectCountByModelIds(Collection<Long> modelIds) {
        return selectCount(new LambdaQueryWrapperX<Ai1AgentDO>().in(Ai1AgentDO::getModelId, modelIds));
    }

}
