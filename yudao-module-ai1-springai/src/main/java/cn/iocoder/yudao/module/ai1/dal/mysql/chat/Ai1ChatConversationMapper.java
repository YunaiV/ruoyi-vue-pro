package cn.iocoder.yudao.module.ai1.dal.mysql.chat;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatConversationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI1 对话 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1ChatConversationMapper extends BaseMapperX<Ai1ChatConversationDO> {

    default PageResult<Ai1ChatConversationDO> selectPage(Ai1ChatConversationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1ChatConversationDO>()
                .eq(Ai1ChatConversationDO::getAgentId, reqVO.getAgentId())
                .likeIfPresent(Ai1ChatConversationDO::getTitle, reqVO.getTitle())
                .eqIfPresent(Ai1ChatConversationDO::getUserId, reqVO.getUserId())
                .orderByDesc(Ai1ChatConversationDO::getId));
    }

    default List<Ai1ChatConversationDO> selectListByAgentIdAndUserId(Long agentId, Long userId) {
        return selectList(new LambdaQueryWrapperX<Ai1ChatConversationDO>()
                .eq(Ai1ChatConversationDO::getAgentId, agentId)
                .eq(Ai1ChatConversationDO::getUserId, userId)
                .orderByDesc(Ai1ChatConversationDO::getId));
    }

    default List<Ai1ChatConversationDO> selectListByAgentIds(Collection<Long> agentIds) {
        return selectList(new LambdaQueryWrapperX<Ai1ChatConversationDO>().in(Ai1ChatConversationDO::getAgentId, agentIds));
    }

}
