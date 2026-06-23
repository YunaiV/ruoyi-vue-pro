package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendStateEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImPrivateMessageSendDTO;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.notification.friend.*;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.FRIEND_STATE;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_BLOCKED_BY_PEER;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_NOT_BLOCKED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_NOT_FRIEND;

/**
 * IM 好友关系 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class ImFriendServiceImpl implements ImFriendService {

    @Resource
    private ImFriendMapper friendMapper;

    @Resource
    private ImWebSocketService websocketService;
    @Resource
    @Lazy
    private ImPrivateMessageService privateMessageService;

    @Override
    @Cacheable(cacheNames = FRIEND_STATE, key = "#userId + '_' + #friendUserId", unless = "#result == null")
    public Integer getFriendState(Long userId, Long friendUserId) {
        // 1.1 我侧记录：我方删了，都算非好友
        ImFriendDO mine = friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (mine == null || !CommonStatusEnum.isEnable(mine.getStatus())) {
            return ImFriendStateEnum.NONE.getState();
        }
        // 1.2 对方侧记录：对方删了 = 不是好友
        ImFriendDO peer = friendMapper.selectByUserIdAndFriendUserId(friendUserId, userId);
        if (peer == null || !CommonStatusEnum.isEnable(peer.getStatus())) {
            return ImFriendStateEnum.NONE.getState();
        }
        // 2. 仅当双方都是 ENABLE 状态，才算好友关系；此时对方拉黑我，则是 BLOCKED
        return BooleanUtil.isTrue(peer.getBlocked()) ? ImFriendStateEnum.BLOCKED.getState() : ImFriendStateEnum.FRIEND.getState();
    }

    @Override
    public void validateFriend(Long userId, Long peerUserId) {
        // 好友 ／ 黑名单校验：和私聊消息发送同一套语义；NONE 已删 ／ 未加，BLOCKED 被对方拉黑
        Integer state = getSelf().getFriendState(userId, peerUserId);
        if (ImFriendStateEnum.isNone(state)) {
            throw exception(FRIEND_NOT_FRIEND);
        }
        if (ImFriendStateEnum.isBlocked(state)) {
            throw exception(FRIEND_BLOCKED_BY_PEER);
        }
    }

    private ImFriendService getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    public List<ImFriendDO> getFriendList(Long userId) {
        return friendMapper.selectListByUserId(userId);
    }

    @Override
    public List<ImFriendDO> pullFriendList(Long userId, Long lastUpdateTime, Long lastId, Integer limit) {
        return friendMapper.selectPullListByUserId(userId, lastUpdateTime, lastId, limit);
    }

    @Override
    public List<ImFriendDO> getEnableFriendList(Long userId) {
        return friendMapper.selectListByUserIdAndStatus(userId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ImFriendDO> getMutualEnableFriendList(Long userId) {
        // 1. 查询本端启用好友
        List<ImFriendDO> friends = getEnableFriendList(userId);
        if (CollUtil.isEmpty(friends)) {
            return Collections.emptyList();
        }

        // 2. 查询对端启用好友关系
        Set<Long> friendUserIds = convertSet(friends, ImFriendDO::getFriendUserId);
        List<ImFriendDO> mutualFriends = friendMapper.selectListByUserIdsAndFriendUserIdAndStatus(friendUserIds, userId,
                CommonStatusEnum.ENABLE.getStatus());
        Set<Long> mutualUserIds = convertSet(mutualFriends, ImFriendDO::getUserId);

        // 3. 过滤双向启用好友
        return filterList(friends, friend -> mutualUserIds.contains(friend.getFriendUserId()));
    }

    @Override
    public List<ImFriendDO> getActiveFriendList(Long userId, Collection<Long> friendUserIds) {
        if (CollUtil.isEmpty(friendUserIds)) {
            return Collections.emptyList();
        }
        return friendMapper.selectListByUserIdAndFriendUserIdsAndStatus(userId, friendUserIds,
                CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public ImFriendDO getFriend(Long userId, Long friendUserId) {
        return friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
    }

    @Override
    public void updateFriend(Long userId, ImFriendUpdateReqVO reqVO) {
        // 1.1 校验：至少改一个字段（无字段变更，直接结束）
        if (reqVO.getDisplayName() == null && reqVO.getSilent() == null && reqVO.getPinned() == null) {
            return;
        }
        // 1.2 校验好友关系启用
        ImFriendDO friend = friendMapper.selectByUserIdAndFriendUserId(userId, reqVO.getFriendUserId());
        if (friend == null || !CommonStatusEnum.isEnable(friend.getStatus())) {
            throw exception(FRIEND_NOT_FRIEND);
        }

        // 2. 更新好友属性（备注 / 免打扰 / 联系人置顶）
        friendMapper.updateById(new ImFriendDO().setId(friend.getId())
                .setSilent(reqVO.getSilent()).setDisplayName(reqVO.getDisplayName()).setPinned(reqVO.getPinned()));

        // 3. 推 FRIEND_UPDATE 给 A 多端：所有单边属性变更合并为单条通知，避免多通知顺序竞争
        FriendUpdateNotification payload = (FriendUpdateNotification) new FriendUpdateNotification()
                .setDisplayName(reqVO.getDisplayName()).setSilent(reqVO.getSilent()).setPinned(reqVO.getPinned())
                .setOperatorUserId(userId).setFriendUserId(reqVO.getFriendUserId());
        websocketService.sendNotificationAsync(userId, ImConversationTypeEnum.NONE.getType(),
                ImContentTypeEnum.FRIEND_UPDATE.getType(), payload);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#request.fromUserId + '_' + #request.toUserId"),
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#request.toUserId + '_' + #request.fromUserId")
    })
    @Transactional(rollbackFor = Exception.class)
    public void becomeFriends(ImFriendRequestDO request) {
        Long fromUserId = request.getFromUserId();
        Long toUserId = request.getToUserId();
        // 1. 双向建立关系：A 侧带申请的 displayName / addSource；B 侧 displayName 为空、addSource 同来源
        //    FRIEND_STATE 双向失效由方法上的 @Caching 注解处理；framework 已开 transactionAware 自动延迟到 afterCommit
        addFriend0(fromUserId, toUserId, request.getDisplayName(), request.getAddSource());
        addFriend0(toUserId, fromUserId, null, request.getAddSource());

        // 2. 发送 FRIEND_ADD 入库（双方拉历史都能看到「你们已成为好友」会话气泡）+ 双向 WebSocket 自动覆盖双方多端
        //    operatorUserId=fromUserId 标记申请发起方；前端按 (currentUserId === operatorUserId) 区分视角，文案固定不依赖此字段
        FriendAddNotification payload = (FriendAddNotification) new FriendAddNotification()
                .setOperatorUserId(fromUserId).setFriendUserId(toUserId);
        privateMessageService.sendPrivateMessage(fromUserId, new ImPrivateMessageSendDTO()
                .setReceiverId(toUserId).setType(ImContentTypeEnum.FRIEND_ADD.getType()).setContent(payload));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#userId + '_' + #friendUserId"),
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#friendUserId + '_' + #userId")
    })
    @Transactional(rollbackFor = Exception.class)
    public void silentReAddFriend(Long userId, Long friendUserId, String displayName, Integer addSource) {
        // 1. 单边重新启用我侧好友关系
        addFriend0(userId, friendUserId, displayName, addSource);

        // 2. 走 sendPrivateMessage + persistent=false：不入库 + 仅推 userId 多端（对方完全不感知，保持「对方一直把我当好友」错觉）
        //    operatorUserId 填 friendUserId（对方）：让 userId 多端 UI 呈现「对方加了我」视角，与 silent 语义对齐
        //    前端按 type=FRIEND_ADD 渲染会话气泡（瞬时，不入库刷新即消失）
        FriendAddNotification payload = (FriendAddNotification) new FriendAddNotification()
                .setOperatorUserId(friendUserId).setFriendUserId(friendUserId);
        privateMessageService.sendPrivateMessage(userId, new ImPrivateMessageSendDTO()
                .setReceiverId(friendUserId).setType(ImContentTypeEnum.FRIEND_ADD.getType())
                .setContent(payload).setPersistent(false));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#userId + '_' + #friendUserId"),
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#friendUserId + '_' + #userId")
    })
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendUserId, Boolean clear) {
        // 1. 单边软删：仅 userId 视角的关系置 DISABLE；friendUserId 视角不动
        if (!deleteFriend0(userId, friendUserId)) {
            return;
        }

        // 2. 走 sendPrivateMessage + persistent=false：不入库 + 仅推 userId 多端（friendUserId 不感知）；clear 透传让多端清理动作一致
        //    clear=false 时前端按 type=FRIEND_DELETE 渲染「你已删除好友」会话气泡（瞬时）；
        //    clear=true 时前端按 clear 字段直接清会话，跳过气泡渲染
        FriendDeleteNotification payload = ((FriendDeleteNotification) new FriendDeleteNotification()
                .setOperatorUserId(userId).setFriendUserId(friendUserId)).setClear(clear);
        privateMessageService.sendPrivateMessage(userId, new ImPrivateMessageSendDTO()
                .setReceiverId(friendUserId).setType(ImContentTypeEnum.FRIEND_DELETE.getType())
                .setContent(payload).setPersistent(false));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#userId + '_' + #friendUserId"),
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#friendUserId + '_' + #userId")
    })
    public void blockFriend(Long userId, Long friendUserId) {
        // 1.1 校验是好友
        ImFriendDO friend = friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (friend == null || !CommonStatusEnum.isEnable(friend.getStatus())) {
            throw exception(FRIEND_NOT_FRIEND);
        }
        // 1.2 已拉黑直接返回，幂等
        if (BooleanUtil.isTrue(friend.getBlocked())) {
            return;
        }

        // 2. 单边更新
        friendMapper.updateById(new ImFriendDO().setId(friend.getId()).setBlocked(true));

        // 3. 推 FRIEND_BLOCK 给 A 多端
        FriendBlockNotification payload = (FriendBlockNotification) new FriendBlockNotification()
                .setOperatorUserId(userId).setFriendUserId(friendUserId);
        websocketService.sendNotificationAsync(userId, ImConversationTypeEnum.NONE.getType(),
                ImContentTypeEnum.FRIEND_BLOCK.getType(), payload);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#userId + '_' + #friendUserId"),
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#friendUserId + '_' + #userId")
    })
    public void unblockFriend(Long userId, Long friendUserId) {
        // 1.1 校验是好友
        ImFriendDO friend = friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (friend == null || !CommonStatusEnum.isEnable(friend.getStatus())) {
            throw exception(FRIEND_NOT_FRIEND);
        }
        // 1.2 未拉黑则报错
        if (!BooleanUtil.isTrue(friend.getBlocked())) {
            throw exception(FRIEND_NOT_BLOCKED);
        }

        // 2. 单边更新
        friendMapper.updateById(new ImFriendDO().setId(friend.getId()).setBlocked(false));

        // 3. 推 FRIEND_UNBLOCK 给 A 多端
        FriendUnblockNotification payload = (FriendUnblockNotification) new FriendUnblockNotification()
                .setOperatorUserId(userId).setFriendUserId(friendUserId);
        websocketService.sendNotificationAsync(userId, ImConversationTypeEnum.NONE.getType(),
                ImContentTypeEnum.FRIEND_UNBLOCK.getType(), payload);
    }

    /**
     * 单向绑定好友关系（内部方法，被 {@link #becomeFriends} / {@link #silentReAddFriend} 调用）：
     * - 情况一：已存在 ENABLE 记录 → 已是好友，幂等跳过；不重置 silent / pinned / blocked，避免历史未处理申请再被同意时清掉用户的拉黑 / 置顶 / 免打扰设置
     * - 情况二：已存在 DISABLE 记录 → 复用并恢复 ENABLE，silent / pinned / blocked 一并重置为 false，对齐"重新加好友"语义
     * - 情况三：不存在记录 → 直接插入新记录
     * <p>
     * 并发安全：agree 路径由 {@code friend_request.handle_result} 的乐观锁单边推进；
     *         极端并发下若插入唯一键冲突，让 DuplicateKeyException 向外抛出，外层事务回滚。
     * <p>
     * FRIEND_STATE 缓存失效由调用方的 @Caching 注解统一处理，本方法不主动 evict
     */
    public void addFriend0(Long userId, Long friendUserId, String displayName, Integer addSource) {
        ImFriendDO exists = friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        // 情况一：已是 ENABLE 好友，幂等跳过；防御历史未处理申请被二次同意时把 blocked / silent / pinned 清回 false
        if (exists != null && CommonStatusEnum.isEnable(exists.getStatus())) {
            return;
        }
        // 情况二：复用 DISABLE 旧记录 → 恢复 ENABLE + 重置 silent / pinned / blocked，对齐"重新加好友"语义
        if (exists != null) {
            LocalDateTime now = LocalDateTime.now();
            friendMapper.updateReAddFields(exists.getId(), CommonStatusEnum.ENABLE.getStatus(), now, now,
                    false, false, false, displayName, addSource);
            return;
        }
        // 情况三：不存在记录 → 直接插入新记录
        ImFriendDO friend = ImFriendDO.builder().userId(userId).friendUserId(friendUserId)
                .silent(false).pinned(false).blocked(false)
                .displayName(displayName).addSource(addSource)
                .status(CommonStatusEnum.ENABLE.getStatus()).addTime(LocalDateTime.now()).build();
        friendMapper.insert(friend);
    }

    /**
     * 单向解除好友关系（status 设为 DISABLE，记录 deleteTime）
     * <p>
     * blocked 不主动重置：删好友期间保留拉黑状态；如果未来再 addFriend0，由 addFriend0 统一重置
     * <p>
     * FRIEND_STATE 缓存失效由调用方的 @Caching 注解统一处理，本方法不主动 evict
     */
    public boolean deleteFriend0(Long userId, Long friendUserId) {
        ImFriendDO exists = friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (exists == null || CommonStatusEnum.isDisable(exists.getStatus())) {
            return false;
        }
        friendMapper.updateById(new ImFriendDO().setId(exists.getId())
                .setStatus(CommonStatusEnum.DISABLE.getStatus()).setDeleteTime(LocalDateTime.now()));
        return true;
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImFriendDO> getFriendPage(ImFriendManagerPageReqVO reqVO) {
        return friendMapper.selectPage(reqVO);
    }

}
