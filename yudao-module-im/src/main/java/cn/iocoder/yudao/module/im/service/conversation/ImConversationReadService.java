package cn.iocoder.yudao.module.im.service.conversation;

import cn.iocoder.yudao.module.im.dal.dataobject.conversation.ImConversationReadDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IM 会话读位置 Service 接口
 *
 * @author 芋道源码
 */
public interface ImConversationReadService {

    /**
     * 更新用户在某会话的最大已读位置（单调递增，乱序 / 并发上报不会回退）
     *
     * @param userId           用户编号
     * @param conversationType 会话类型
     * @param conversationId   会话编号
     * @param readMessageId    已读到的最大消息编号
     * @return 读位置是否前进；true 时调用方才需要下发已读 / 回执事件
     */
    boolean updateConversationReadPosition(Long userId, Integer conversationType, Long conversationId, Long readMessageId);

    /**
     * 获取用户在某会话的最大已读位置
     *
     * @param userId           用户编号
     * @param conversationType 会话类型
     * @param conversationId   会话编号
     * @return 最大已读消息编号；不存在则返回 null
     */
    Long getConversationReadMessageId(Long userId, Integer conversationType, Long conversationId);

    /**
     * 获取某会话内所有用户的读位置（用于群回执人数聚合）
     *
     * @param conversationType 会话类型
     * @param conversationId   会话编号
     * @return userId → 最大已读消息编号
     */
    Map<Long, Long> getUserReadMessageIdMap(Integer conversationType, Long conversationId);

    /**
     * 批量获取某用户在多个会话的读位置（用于频道批量读位置、重连后按活跃会话补偿）
     *
     * @param userId           用户编号
     * @param conversationType 会话类型
     * @param conversationIds  会话编号集合
     * @return conversationId → 最大已读消息编号
     */
    Map<Long, Long> getConversationReadMessageIdMap(Long userId, Integer conversationType, Collection<Long> conversationIds);

    /**
     * 增量拉取当前用户的会话读位置（重连 / 离线补偿：按 update_time + id 游标）
     *
     * @param userId         用户编号
     * @param lastUpdateTime 上次拉取到的最新更新时间（毫秒时间戳）；首次拉取传 null
     * @param lastId         上次拉取到的最后一条记录 id；首次拉取传 null
     * @param limit          单次拉取条数
     * @return 会话读位置列表，按更新时间、id 正序
     */
    List<ImConversationReadDO> pullConversationReadList(Long userId, Long lastUpdateTime, Long lastId, Integer limit);

}
