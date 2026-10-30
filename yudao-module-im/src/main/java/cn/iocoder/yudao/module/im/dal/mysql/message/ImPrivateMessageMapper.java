package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.privates.ImPrivateMessageManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IM 私聊消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImPrivateMessageMapper extends BaseMapperX<ImPrivateMessageDO> {

    /**
     * 根据 minId + 时间窗口增量拉取私聊消息
     *
     * @param userId      当前用户编号
     * @param minId       最小消息 id（不含）
     * @param minSendTime 最早发送时间（不含），限制离线消息时间窗口
     * @param size        拉取数量
     * @return 消息列表
     */
    default List<ImPrivateMessageDO> selectListByMinId(Long userId, Long minId,
                                                       LocalDateTime minSendTime, Integer size) {
        LambdaQueryWrapperX<ImPrivateMessageDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.and(w -> w.eq(ImPrivateMessageDO::getSenderId, userId)
                        .or()
                        .eq(ImPrivateMessageDO::getReceiverId, userId))
                .gt(ImPrivateMessageDO::getId, minId)
                .gt(ImPrivateMessageDO::getSendTime, minSendTime)
                .orderByAsc(ImPrivateMessageDO::getId);
        return selectListLimitN(wrapper, size);
    }

    /**
     * 查询私聊历史消息（游标拉取）
     *
     * @param userId     当前用户编号
     * @param receiverId 对方用户编号
     * @param maxId      起始消息 id（不含），为空则从最新开始
     * @param limit      拉取数量
     * @return 消息列表（按 id 倒序）
     */
    default List<ImPrivateMessageDO> selectHistoryList(Long userId, Long receiverId, Long maxId, Integer limit) {
        LambdaQueryWrapperX<ImPrivateMessageDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.and(w -> w.eq(ImPrivateMessageDO::getSenderId, userId).eq(ImPrivateMessageDO::getReceiverId, receiverId)
                        .or()
                        .eq(ImPrivateMessageDO::getSenderId, receiverId).eq(ImPrivateMessageDO::getReceiverId, userId))
                .lt(maxId != null, ImPrivateMessageDO::getId, maxId)
                .orderByDesc(ImPrivateMessageDO::getId);
        return selectListLimitN(wrapper, limit);
    }

    default ImPrivateMessageDO selectBySenderIdAndClientMessageId(Long senderId, String clientMessageId) {
        return selectOne(new LambdaQueryWrapperX<ImPrivateMessageDO>()
                .eq(ImPrivateMessageDO::getSenderId, senderId)
                .eq(ImPrivateMessageDO::getClientMessageId, clientMessageId));
    }

    default int updateBySenderIdAndReceiverIdAndIdLeAndReceiptStatus(Long senderId, Long receiverId, Long maxMessageId,
                                                                     Integer whereReceiptStatus, ImPrivateMessageDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<ImPrivateMessageDO>()
                .eq(ImPrivateMessageDO::getSenderId, senderId)
                .eq(ImPrivateMessageDO::getReceiverId, receiverId)
                .le(ImPrivateMessageDO::getId, maxMessageId)
                .eq(ImPrivateMessageDO::getReceiptStatus, whereReceiptStatus));
    }

    default PageResult<ImPrivateMessageDO> selectPage(ImPrivateMessageManagerPageReqVO reqVO) {
        LambdaQueryWrapperX<ImPrivateMessageDO> query = new LambdaQueryWrapperX<>();
        if (reqVO.getSenderId() != null && reqVO.getReceiverId() != null) {
            query.and(w -> w.eq(ImPrivateMessageDO::getSenderId, reqVO.getSenderId())
                            .eq(ImPrivateMessageDO::getReceiverId, reqVO.getReceiverId())
                            .or()
                            .eq(ImPrivateMessageDO::getSenderId, reqVO.getReceiverId())
                            .eq(ImPrivateMessageDO::getReceiverId, reqVO.getSenderId()));
        } else {
            query.eqIfPresent(ImPrivateMessageDO::getSenderId, reqVO.getSenderId())
                    .eqIfPresent(ImPrivateMessageDO::getReceiverId, reqVO.getReceiverId());
        }
        return selectPage(reqVO, query
                .eqIfPresent(ImPrivateMessageDO::getType, reqVO.getType())
                .likeIfPresent(ImPrivateMessageDO::getContent, reqVO.getContent())
                .eqIfPresent(ImPrivateMessageDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ImPrivateMessageDO::getSendTime, reqVO.getSendTime())
                .orderByDesc(ImPrivateMessageDO::getId));
    }

}
