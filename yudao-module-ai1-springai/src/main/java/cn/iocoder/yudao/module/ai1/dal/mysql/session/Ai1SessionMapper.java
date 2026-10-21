package cn.iocoder.yudao.module.ai1.dal.mysql.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI1 会话 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1SessionMapper extends BaseMapperX<Ai1SessionDO> {

    default PageResult<Ai1SessionDO> selectPage(Ai1SessionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1SessionDO>()
                .eq(Ai1SessionDO::getAgentId, reqVO.getAgentId())
                .likeIfPresent(Ai1SessionDO::getTitle, reqVO.getTitle())
                .eqIfPresent(Ai1SessionDO::getUserId, reqVO.getUserId())
                .orderByDesc(Ai1SessionDO::getId));
    }

    default List<Ai1SessionDO> selectListByAgentIdAndUserId(Long agentId, Long userId) {
        return selectList(new LambdaQueryWrapperX<Ai1SessionDO>()
                .eq(Ai1SessionDO::getAgentId, agentId)
                .eq(Ai1SessionDO::getUserId, userId)
                .orderByDesc(Ai1SessionDO::getId));
    }

    default List<Ai1SessionDO> selectListByAgentIds(Collection<Long> agentIds) {
        return selectList(new LambdaQueryWrapperX<Ai1SessionDO>().in(Ai1SessionDO::getAgentId, agentIds));
    }

}
