package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
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

    /**
     * 根据群编号和 minId 增量拉取群聊消息
     *
     * @param groupIds 用户所在的群编号列表
     * @param minId    最小消息 id（不含）
     * @param size     拉取数量
     * @return 消息列表
     */
    default List<ImGroupMessageDO> selectListByMinId(List<Long> groupIds, Long minId, Integer size) {
        return selectList(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .in(ImGroupMessageDO::getGroupId, groupIds)
                .gt(ImGroupMessageDO::getId, minId)
                .orderByAsc(ImGroupMessageDO::getId)
                .last("LIMIT " + size));
    }

    /**
     * 根据 senderId 和 clientMessageId 查询（幂等校验）
     */
    default ImGroupMessageDO selectBySenderIdAndClientMessageId(Long senderId, String clientMessageId) {
        return selectOne(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eq(ImGroupMessageDO::getSenderId, senderId)
                .eq(ImGroupMessageDO::getClientMessageId, clientMessageId));
    }

    /**
     * 获取群内最新消息 id
     */
    default Long selectMaxIdByGroupId(Long groupId) {
        ImGroupMessageDO msg = selectOne(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eq(ImGroupMessageDO::getGroupId, groupId)
                .orderByDesc(ImGroupMessageDO::getId)
                .last("LIMIT 1"));
        return msg == null ? null : msg.getId();
    }

    /**
     * 获取群内需要回执的消息（状态为 PENDING）
     */
    default List<ImGroupMessageDO> selectPendingReceiptMessages(Long groupId) {
        return selectList(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eq(ImGroupMessageDO::getGroupId, groupId)
                .eq(ImGroupMessageDO::getReceiptStatus, 1)); // PENDING
    }

}
