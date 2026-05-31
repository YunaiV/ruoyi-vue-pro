package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.group.ImGroupMessageManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IM 群聊消息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupMessageMapper extends BaseMapperX<ImGroupMessageDO> {

    /**
     * 根据 minId + 时间窗口增量拉取群聊消息（在群成员使用）
     *
     * @param groupIds    用户当前仍在群内的群编号列表
     * @param minId       最小消息 id（不含）
     * @param minSendTime 最早发送时间（不含），限制离线消息时间窗口
     * @param size        拉取数量
     * @return 消息列表（按 id 升序）
     */
    default List<ImGroupMessageDO> selectListByMinId(List<Long> groupIds, Long minId,
                                                     LocalDateTime minSendTime, Integer size) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.in("group_id", groupIds)
                .gt("id", minId)
                .gt("send_time", minSendTime)
                .orderByAsc("id");
        wrapper.limitN(size);
        return selectList(wrapper);
    }

    /**
     * 查询"退群前"的离线消息（退群成员使用）
     * <p>
     * 语义：用户已退出某群，但仍需把 {@code minId} 之后、{@code minSendTime} 之后、不晚于退群时间的消息补齐到本地，便于前端看到完整上下文。
     * <p>
     * 撤回消息（status=RECALL）保留返回，与在群成员的 {@link #selectListByMinId} 行为一致，由前端按撤回信号渲染「消息已撤回」气泡。
     *
     * @param groupId     群编号
     * @param minId       最小消息 id（不含）
     * @param minSendTime 最早发送时间（不含）
     * @param quitTime    退群时间（含），仅返回退群当时已存在的消息
     * @param size        拉取数量
     * @return 消息列表（按 id 升序）
     */
    default List<ImGroupMessageDO> selectListByGroupIdAndMinIdAndQuitTimeBefore(Long groupId, Long minId,
                                                                                LocalDateTime minSendTime,
                                                                                LocalDateTime quitTime,
                                                                                Integer size) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.eq("group_id", groupId)
                .gt("id", minId)
                .gt("send_time", minSendTime)
                .le("send_time", quitTime)
                .orderByAsc("id");
        wrapper.limitN(size);
        return selectList(wrapper);
    }

    /**
     * 查询群聊历史消息（游标拉取）
     *
     * @param groupId  群编号
     * @param maxId    起始消息 id（不含），为空则从最新开始
     * @param limit    拉取数量
     * @param joinTime 入群时间，仅返回入群之后的消息
     * @return 消息列表（按 id 倒序）
     */
    default List<ImGroupMessageDO> selectHistoryList(Long groupId, Long maxId, Integer limit, LocalDateTime joinTime) {
        QueryWrapperX<ImGroupMessageDO> wrapper = new QueryWrapperX<>();
        wrapper.eq("group_id", groupId)
                .lt(maxId != null, "id", maxId)
                .ge(joinTime != null, "send_time", joinTime)
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
                .eq(ImGroupMessageDO::getReceiptStatus, ImGroupMessageReceiptStatusEnum.PENDING.getStatus())
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
