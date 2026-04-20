package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 群聊消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupMessageMapper extends BaseMapperX<ImGroupMessageDO> {

    default List<ImGroupMessageDO> selectListByMinId(List<Long> groupIds, Long minId, Integer size) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.in("group_id", groupIds)
                .gt("id", minId)
                .orderByAsc("id");
        wrapper.limitN(size);
        return selectList(wrapper);
    }

    default ImGroupMessageDO selectBySenderIdAndClientMessageId(Long senderId, String clientMessageId) {
        return selectOne(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eq(ImGroupMessageDO::getSenderId, senderId)
                .eq(ImGroupMessageDO::getClientMessageId, clientMessageId));
    }

    // TODO @芋艿：暂时没 review
    /**
     * 获取群内最新消息 id
     */
    default Long selectMaxIdByGroupId(Long groupId) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.eq("group_id", groupId)
                .orderByDesc("id");
        wrapper.limitN(1);
        ImGroupMessageDO msg = selectOne(wrapper);
        return msg == null ? null : msg.getId();
    }

    // TODO @芋艿：暂时没 review
    /**
     * 获取群内需要回执的消息（状态为 PENDING）
     */
    default List<ImGroupMessageDO> selectPendingReceiptMessages(Long groupId) {
        return selectList(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eq(ImGroupMessageDO::getGroupId, groupId)
                .eq(ImGroupMessageDO::getReceiptStatus, 1)); // PENDING
    }

}
