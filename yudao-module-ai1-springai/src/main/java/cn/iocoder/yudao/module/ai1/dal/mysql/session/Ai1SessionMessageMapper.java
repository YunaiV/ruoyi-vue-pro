package cn.iocoder.yudao.module.ai1.dal.mysql.session;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI1 对话消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1SessionMessageMapper extends BaseMapperX<Ai1SessionMessageDO> {

    default List<Ai1SessionMessageDO> selectListBySessionId(Long sessionId) {
        return selectList(new LambdaQueryWrapperX<Ai1SessionMessageDO>()
                .eq(Ai1SessionMessageDO::getSessionId, sessionId)
                .orderByAsc(Ai1SessionMessageDO::getId));
    }

    /**
     * 按编号倒序，取指定消息之前最近的 limit 条
     */
    default List<Ai1SessionMessageDO> selectListBySessionIdAndIdLessThan(Long sessionId, Long id, Integer limit) {
        return selectList(new QueryWrapperX<Ai1SessionMessageDO>()
                .limitN(limit)
                .eq("session_id", sessionId)
                .lt("id", id)
                .orderByDesc("id"));
    }

    default Long selectCountBySessionId(Long sessionId) {
        return selectCount(Ai1SessionMessageDO::getSessionId, sessionId);
    }

    default int deleteBySessionIds(Collection<Long> sessionIds) {
        return delete(new LambdaQueryWrapperX<Ai1SessionMessageDO>().in(Ai1SessionMessageDO::getSessionId, sessionIds));
    }

}
