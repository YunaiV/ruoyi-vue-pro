package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.group.ImGroupMessageManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * IM 群聊消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupMessageMapper extends BaseMapperX<ImGroupMessageDO> {

    /**
     * 根据 minId + 时间窗口增量拉取群聊消息
     *
     * @param userId      当前用户编号
     * @param groupIds    候选群编号集合（当前在群 ∪ 窗口内退群），不能为空
     * @param minId       最小消息 id（不含）
     * @param minSendTime 最早发送时间（不含），限制离线消息时间窗口
     * @param size        拉取数量
     * @return 消息列表（按 id 升序）
     */
    default List<ImGroupMessageDO> selectListByMinId(Long userId, Collection<Long> groupIds, Long minId,
                                                     LocalDateTime minSendTime, Integer size) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.in("group_id", groupIds)
                .gt("id", minId)
                .gt("send_time", minSendTime)
                .apply(MyBatisUtils.findInSet("receiver_user_ids"), userId)
                .orderByAsc("id");
        wrapper.limitN(size);
        return selectList(wrapper);
    }

    /**
     * 查询群聊历史消息（游标拉取）
     *
     * @param userId 当前用户编号
     * @param groupId 群编号
     * @param maxId  起始消息 id（不含），为空则从最新开始
     * @param limit  拉取数量
     * @return 消息列表（按 id 倒序）
     */
    default List<ImGroupMessageDO> selectHistoryListByUser(Long userId, Long groupId, Long maxId, Integer limit) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.eq("group_id", groupId)
                .lt(maxId != null, "id", maxId)
                .apply(MyBatisUtils.findInSet("receiver_user_ids"), userId)
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
    default List<ImGroupMessageDO> selectListByGroupIdAndPendingReceipt(Long groupId, Long minId, Long maxId) {
        return selectList(new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eq(ImGroupMessageDO::getGroupId, groupId)
                .eq(ImGroupMessageDO::getReceiptStatus, ImMessageReceiptStatusEnum.PENDING.getStatus())
                .gt(minId != null, ImGroupMessageDO::getId, minId)
                .le(ImGroupMessageDO::getId, maxId)
                .ne(ImGroupMessageDO::getStatus, ImMessageStatusEnum.RECALL.getStatus()));
    }

    default PageResult<ImGroupMessageDO> selectPage(ImGroupMessageManagerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImGroupMessageDO>()
                .eqIfPresent(ImGroupMessageDO::getGroupId, reqVO.getGroupId())
                .eqIfPresent(ImGroupMessageDO::getSenderId, reqVO.getSenderId())
                .eqIfPresent(ImGroupMessageDO::getType, reqVO.getType())
                .likeIfPresent(ImGroupMessageDO::getContent, reqVO.getContent())
                .eqIfPresent(ImGroupMessageDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ImGroupMessageDO::getSendTime, reqVO.getSendTime())
                .orderByDesc(ImGroupMessageDO::getId));
    }

}
