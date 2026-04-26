package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
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
        QueryWrapperX<ImPrivateMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.and(w -> w.eq("sender_id", userId)
                        .or()
                        .eq("receiver_id", userId))
                .gt("id", minId)
                .gt("send_time", minSendTime)
                .orderByAsc("id");
        wrapper.limitN(size);
        return selectList(wrapper);
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
        QueryWrapperX<ImPrivateMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.and(w -> w.eq("sender_id", userId).eq("receiver_id", receiverId)
                        .or()
                        .eq("sender_id", receiverId).eq("receiver_id", userId))
                .ne("status", ImMessageStatusEnum.RECALL.getStatus())
                .lt(maxId != null, "id", maxId)
                .orderByDesc("id");
        wrapper.limitN(limit);
        return selectList(wrapper);
    }

    default ImPrivateMessageDO selectBySenderIdAndClientMessageId(Long senderId, String clientMessageId) {
        return selectOne(new LambdaQueryWrapperX<ImPrivateMessageDO>()
                .eq(ImPrivateMessageDO::getSenderId, senderId)
                .eq(ImPrivateMessageDO::getClientMessageId, clientMessageId));
    }

    /**
     * 标记 (senderId → receiverId) 这条会话上、id <= maxMessageId 且 status = whereStatus 的消息为新状态
     * <p>
     * 用于「已读」语义：前端上报"我已读到 maxMessageId"，由这一条 SQL 在 status=UNREAD 上幂等翻转，
     * 避免"先 select 未读、再按 id in 更新"两步带来的竞态（select 后到达的消息被误标）。
     *
     * @param senderId      发送方用户编号（对方）
     * @param receiverId    接收方用户编号（当前用户）
     * @param maxMessageId  已读位置（含）
     * @param whereStatus   匹配的当前状态
     * @param updateObj     更新对象（仅设要变更的字段）
     * @return 实际更新行数
     */
    default int updateBySenderIdAndReceiverIdAndIdLeAndStatus(Long senderId, Long receiverId, Long maxMessageId,
                                                              Integer whereStatus, ImPrivateMessageDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<ImPrivateMessageDO>()
                .eq(ImPrivateMessageDO::getSenderId, senderId)
                .eq(ImPrivateMessageDO::getReceiverId, receiverId)
                .le(ImPrivateMessageDO::getId, maxMessageId)
                .eq(ImPrivateMessageDO::getStatus, whereStatus));
    }

}
