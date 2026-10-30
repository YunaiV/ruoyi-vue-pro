package cn.iocoder.yudao.module.ai1.dal.mysql.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message.Ai1SessionMessagePageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * AI1 会话消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1SessionMessageMapper extends BaseMapperX<Ai1SessionMessageDO> {

    default PageResult<Ai1SessionMessageDO> selectPage(Ai1SessionMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1SessionMessageDO>()
                .eq(Ai1SessionMessageDO::getSessionId, reqVO.getSessionId())
                .orderByDesc(Ai1SessionMessageDO::getId));
    }

    default List<Ai1SessionMessageDO> selectListBySessionId(Long sessionId) {
        return selectList(new LambdaQueryWrapperX<Ai1SessionMessageDO>()
                .eq(Ai1SessionMessageDO::getSessionId, sessionId)
                .orderByAsc(Ai1SessionMessageDO::getId));
    }

    /**
     * 只查询编号，不读取正文、思考过程
     */
    default List<Long> selectIdListBySessionIdAndRoleAndStatus(Long sessionId, String role, Integer status) {
        return convertList(selectList(new LambdaQueryWrapperX<Ai1SessionMessageDO>()
                .select(Ai1SessionMessageDO::getId)
                .eq(Ai1SessionMessageDO::getSessionId, sessionId)
                .eq(Ai1SessionMessageDO::getRole, role)
                .eq(Ai1SessionMessageDO::getStatus, status)
                .orderByAsc(Ai1SessionMessageDO::getId)), Ai1SessionMessageDO::getId);
    }

    /**
     * 按编号倒序，取指定消息之前最近的 limit 条
     */
    default List<Ai1SessionMessageDO> selectListBySessionIdAndIdLessThan(Long sessionId, Long id, Integer limit) {
        return selectListLimitN(new LambdaQueryWrapperX<Ai1SessionMessageDO>()
                .eq(Ai1SessionMessageDO::getSessionId, sessionId)
                .lt(Ai1SessionMessageDO::getId, id)
                .orderByDesc(Ai1SessionMessageDO::getId), limit);
    }

    /**
     * 获得多个会话中，编号大于 id 的第一条指定状态的消息
     */
    default Ai1SessionMessageDO selectFirstBySessionIdsAndStatusAndIdGt(Collection<Long> sessionIds, Integer status, Long id) {
        return selectFirstOne(new LambdaQueryWrapperX<Ai1SessionMessageDO>()
                .in(Ai1SessionMessageDO::getSessionId, sessionIds)
                .eq(Ai1SessionMessageDO::getStatus, status)
                .gt(Ai1SessionMessageDO::getId, id)
                .orderByAsc(Ai1SessionMessageDO::getId));
    }

    /**
     * 获得多个会话中，编号在 (minId, maxId) 之间的指定状态消息，按编号升序取前 limit 条；maxId 为空时不限上界
     */
    default List<Ai1SessionMessageDO> selectListBySessionIdsAndStatusAndIdBetween(Collection<Long> sessionIds, Integer status,
                                                                                    Long minId, Long maxId, Integer limit) {
        return selectListLimitN(new LambdaQueryWrapperX<Ai1SessionMessageDO>()
                .in(Ai1SessionMessageDO::getSessionId, sessionIds)
                .eq(Ai1SessionMessageDO::getStatus, status)
                .gt(Ai1SessionMessageDO::getId, minId)
                .lt(maxId != null, Ai1SessionMessageDO::getId, maxId)
                .orderByAsc(Ai1SessionMessageDO::getId), limit);
    }

    default Long selectCountBySessionId(Long sessionId) {
        return selectCount(Ai1SessionMessageDO::getSessionId, sessionId);
    }

    default int deleteBySessionIds(Collection<Long> sessionIds) {
        return delete(new LambdaQueryWrapperX<Ai1SessionMessageDO>().in(Ai1SessionMessageDO::getSessionId, sessionIds));
    }

}
