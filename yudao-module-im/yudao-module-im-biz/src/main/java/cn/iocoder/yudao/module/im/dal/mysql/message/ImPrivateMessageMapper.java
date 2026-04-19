package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
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
                        .or().eq("receiver_id", userId))
                .gt("id", minId)
                .orderByAsc("id");
        wrapper.limitN(size);
        return selectList(wrapper);
    }

    default ImPrivateMessageDO selectBySenderIdAndClientMessageId(Long senderId, String clientMessageId) {
        return selectOne(new LambdaQueryWrapperX<ImPrivateMessageDO>()
                .eq(ImPrivateMessageDO::getSenderId, senderId)
                .eq(ImPrivateMessageDO::getClientMessageId, clientMessageId));
    }

    /**
     * 批量更新私聊消息已读状态
     * 将发送方 = friendId、接收方 = userId 且状态为 UNREAD 的消息更新为 READ
     */
    default int updateStatusToRead(Long userId, Long friendId) {
        return update(new ImPrivateMessageDO().setStatus(1), // READ
                new LambdaQueryWrapperX<ImPrivateMessageDO>()
                        .eq(ImPrivateMessageDO::getSenderId, friendId)
                        .eq(ImPrivateMessageDO::getReceiverId, userId)
                        .eq(ImPrivateMessageDO::getStatus, 0)); // UNREAD
    }

}
