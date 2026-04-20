package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 私聊消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImPrivateMessageMapper extends BaseMapperX<ImPrivateMessageDO> {

    /**
     * 根据 minId 增量拉取私聊消息
     *
     * @param userId 当前用户编号
     * @param minId  最小消息 id（不含）
     * @param size   拉取数量
     * @return 消息列表
     */
    default List<ImPrivateMessageDO> selectListByMinId(Long userId, Long minId, Integer size) {
        QueryWrapperX<ImPrivateMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.and(w -> w.eq("sender_id", userId)
                        .or()
                        .eq("receiver_id", userId))
                .gt("id", minId)
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

    default List<ImPrivateMessageDO> selectListBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ImPrivateMessageDO>()
                .eq(ImPrivateMessageDO::getSenderId, senderId)
                .eq(ImPrivateMessageDO::getReceiverId, receiverId)
                .eq(ImPrivateMessageDO::getStatus, status));
    }

    default void updateByIdsAndStatus(List<Long> ids, Integer whereStatus, ImPrivateMessageDO updateObj) {
        update(updateObj, new LambdaQueryWrapperX<ImPrivateMessageDO>()
                .in(ImPrivateMessageDO::getId, ids)
                .eq(ImPrivateMessageDO::getStatus, whereStatus));
    }

}
