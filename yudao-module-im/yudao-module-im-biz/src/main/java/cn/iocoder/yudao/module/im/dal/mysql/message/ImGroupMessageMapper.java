package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
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
     * 根据 minId 增量拉取群聊消息
     *
     * @param groupIds 用户所在群编号列表
     * @param minId    最小消息 id（不含）
     * @param size     拉取数量
     * @return 消息列表
     */
    default List<ImGroupMessageDO> selectListByMinId(List<Long> groupIds, Long minId, Integer size) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.in("group_id", groupIds)
                .gt("id", minId)
                .orderByAsc("id");
        wrapper.limitN(size);
        return selectList(wrapper);
    }

    /**
     * 查询群聊历史消息（游标拉取）
     *
     * @param groupId 群编号
     * @param maxId   起始消息 id（不含），为空则从最新开始
     * @param limit   拉取数量
     * @return 消息列表（按 id 倒序）
     */
    default List<ImGroupMessageDO> selectHistoryList(Long groupId, Long maxId, Integer limit) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.eq("group_id", groupId)
                .ne("status", ImMessageStatusEnum.RECALL.getStatus())
                .lt(maxId != null, "id", maxId)
                .orderByDesc("id");
        wrapper.limitN(limit);
        return selectList(wrapper);
    }

    default ImGroupMessageDO selectBySenderIdAndClientMessageId(Long senderId, String clientMessageId) {
        return selectOne(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eq(ImGroupMessageDO::getSenderId, senderId)
                .eq(ImGroupMessageDO::getClientMessageId, clientMessageId));
    }

    /**
     * 获取群内最新消息 id
     *
     * @param groupId 群编号
     * @return 最新消息 id，无消息时返回 null
     */
    default Long selectMaxIdByGroupId(Long groupId) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.eq("group_id", groupId)
                .orderByDesc("id")
                .select("id");
        wrapper.limitN(1);
        ImGroupMessageDO msg = selectOne(wrapper);
        return msg == null ? null : msg.getId();
    }

    /**
     * 查询群内指定范围内待回执的消息
     * <p>
     * 仅在用户"已读位置前进"时调用，避免全量扫描：
     * 只有位于 (minId, maxId] 范围内、且仍处于 PENDING 的回执消息可能因本次已读而状态变化。
     *
     * @param groupId 群编号
     * @param minId   起始消息 id（不含，上一次已读位置）
     * @param maxId   结束消息 id（含，本次已读位置）
     * @return 待回执消息列表
     */
    default List<ImGroupMessageDO> selectPendingReceiptMessagesInRange(Long groupId, Long minId, Long maxId) {
        return selectList(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eq(ImGroupMessageDO::getGroupId, groupId)
                .eq(ImGroupMessageDO::getReceiptStatus, ImGroupMessageReceiptStatusEnum.PENDING.getStatus())
                .gt(ImGroupMessageDO::getId, minId)
                .le(ImGroupMessageDO::getId, maxId)
                .ne(ImGroupMessageDO::getStatus, ImMessageStatusEnum.RECALL.getStatus()));
    }

}
