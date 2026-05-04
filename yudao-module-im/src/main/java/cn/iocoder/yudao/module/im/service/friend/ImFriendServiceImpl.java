package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendStateEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.friend.*;
import jakarta.annotation.Resource;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.FRIEND_STATE;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_NOT_BLOCKED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_NOT_FRIEND;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.FRIEND_ADD_TIP_MESSAGE;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.FRIEND_DELETE_TIP_MESSAGE;

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
    public ImFriendStateEnum getFriendState(Long userId, Long friendUserId) {
        // 1.1 我侧记录：我方删了，都算非好友
        ImFriendDO mine = friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (mine == null || !CommonStatusEnum.isEnable(mine.getStatus())) {
            return ImFriendStateEnum.NONE;
        }
        // 1.2 对方侧记录：对方删了 = 不是好友
        ImFriendDO peer = friendMapper.selectByUserIdAndFriendUserId(friendUserId, userId);
        if (peer == null || !CommonStatusEnum.isEnable(peer.getStatus())) {
            return ImFriendStateEnum.NONE;
        }
        // 2. 仅当双方都是 ENABLE 状态，才算好友关系；此时对方拉黑我，则是 BLOCKED
        return BooleanUtil.isTrue(peer.getBlocked()) ? ImFriendStateEnum.BLOCKED : ImFriendStateEnum.FRIEND;
    }

    @Override
    public List<ImFriendDO> getFriendList(Long userId) {
        return friendMapper.selectListByUserId(userId);
    }

    @Override
    public List<ImFriendDO> getEnableFriendList(Long userId) {
        return friendMapper.selectListByUserIdAndStatus(userId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ImFriendDO> getActiveFriendList(Long userId, Collection<Long> friendUserIds) {
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
        if (reqVO.getDisplayName() == null && reqVO.getMuted() == null && reqVO.getPinned() == null) {
            return;
        }
        // 1.2 校验好友关系存在
        ImFriendDO friend = friendMapper.selectByUserIdAndFriendUserId(userId, reqVO.getFriendUserId());
        if (friend == null) {
            throw exception(FRIEND_NOT_FRIEND);
        }

        // 2. 更新好友属性（备注 / 免打扰 / 联系人置顶）
        friendMapper.updateById(new ImFriendDO().setId(friend.getId())
                .setMuted(reqVO.getMuted())
                .setDisplayName(reqVO.getDisplayName())
                .setPinned(reqVO.getPinned()));

        // 3. 推 FRIEND_UPDATE 给 A 多端：所有单边属性变更合并为单条通知，避免多通知顺序竞争
        FriendUpdateNotification payload = (FriendUpdateNotification) new FriendUpdateNotification()
                .setDisplayName(reqVO.getDisplayName()).setMuted(reqVO.getMuted()).setPinned(reqVO.getPinned())
                .setOperatorUserId(userId).setFriendUserId(reqVO.getFriendUserId());
        websocketService.sendPrivateMessageAsync(userId, ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_UPDATE.getType(), userId, userId, payload));
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

        // 2.1 推 TIP 系统消息（双方私聊会话里看到「你们已成为好友」）；TIP 走会话入库，FRIEND_ADD 走事件通知
        privateMessageService.sendTipPrivateMessage(fromUserId, toUserId, FRIEND_ADD_TIP_MESSAGE);

        // 2.2.1 推 FRIEND_ADD 给 fromUser 多端
        FriendAddNotification toFrom = (FriendAddNotification) new FriendAddNotification()
                .setOperatorUserId(toUserId).setFriendUserId(toUserId);
        websocketService.sendPrivateMessageAsync(fromUserId, ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_ADD.getType(), toUserId, fromUserId, toFrom));
        // 2.2.2 推 FRIEND_ADD 给 toUser 多端
        FriendAddNotification toTo = (FriendAddNotification) new FriendAddNotification()
                .setOperatorUserId(fromUserId).setFriendUserId(fromUserId);
        websocketService.sendPrivateMessageAsync(toUserId, ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_ADD.getType(), fromUserId, toUserId, toTo));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#userId + '_' + #friendUserId"),
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#friendUserId + '_' + #userId")
    })
    @Transactional(rollbackFor = Exception.class)
    public void silentReAddFriend(Long userId, Long friendUserId, String displayName, Integer addSource) {
        // 1. 单边重新启用我侧好友关系
        //    不推 TIP 系统消息：sendPrivateMessage 是 (sender, receiver) 单条入库 + 双向可见，无法仅 userId 单边可见；对方不应感知 silent 重启，TIP 整体省略
        addFriend0(userId, friendUserId, displayName, addSource);

        // 2. 仅推 FRIEND_ADD 给 userId 多端（不通知对方，保持「对方一直把我当好友」的错觉）
        //    operatorUserId 填 friendUserId（对方）：让 userId 多端 UI 呈现「对方加了我」的视觉效果，与 silent 语义对齐
        FriendAddNotification payload = (FriendAddNotification) new FriendAddNotification()
                .setOperatorUserId(friendUserId).setFriendUserId(friendUserId);
        websocketService.sendPrivateMessageAsync(userId, ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_ADD.getType(), friendUserId, userId, payload));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#userId + '_' + #friendUserId"),
            @CacheEvict(cacheNames = FRIEND_STATE, key = "#friendUserId + '_' + #userId")
    })
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendUserId, Boolean clear) {
        // 1. 单边软删：仅 userId 视角的关系置 DISABLE；friendUserId 视角不动
        deleteFriend0(userId, friendUserId);

        // 2. 推 TIP「你已删除好友」走单边语义（persistent=false）：
        //    不入库 + 仅推 userId 多端，对方完全不感知；clear=true 时前端会清对话连带这条 TIP 一起清
        privateMessageService.sendTipPrivateMessage(userId, friendUserId, FRIEND_DELETE_TIP_MESSAGE, false);

        // 3. 推 FRIEND_DELETE 给 userId 多端做同步（friendUserId 不感知）；clear 透传让多端清理动作一致
        FriendDeleteNotification payload = ((FriendDeleteNotification) new FriendDeleteNotification()
                .setOperatorUserId(userId).setFriendUserId(friendUserId)).setClear(clear);
        websocketService.sendPrivateMessageAsync(userId, ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_DELETE.getType(), userId, userId, payload));
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
        websocketService.sendPrivateMessageAsync(userId, ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_BLOCK.getType(), userId, userId, payload));
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
        websocketService.sendPrivateMessageAsync(userId, ImPrivateMessageDTO.ofFriendNotification(
                ImMessageTypeEnum.FRIEND_UNBLOCK.getType(), userId, userId, payload));
    }

    /**
     * 单向绑定好友关系（内部方法，被 {@link #becomeFriends} / {@link #silentReAddFriend} 调用）：
     * - 情况一：已存在记录（含 ENABLE / DISABLE）→ 复用并恢复 ENABLE，muted / pinned / blocked 一并重置为 false
     * - 情况二：不存在记录 → 直接插入新记录
     * <p>
     * 并发安全：agree 路径由 {@code friend_request.handle_result} 的乐观锁单边推进；
     *         极端并发下若插入唯一键冲突，让 DuplicateKeyException 向外抛出，外层事务回滚。
     * <p>
     * FRIEND_STATE 缓存失效由调用方的 @Caching 注解统一处理，本方法不主动 evict
     */
    public void addFriend0(Long userId, Long friendUserId, String displayName, Integer addSource) {
        ImFriendDO exists = friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        // 情况一：复用旧记录 → 恢复 ENABLE + 重置 muted / pinned / blocked 与首次新增对齐
        if (exists != null) {
            ImFriendDO update = new ImFriendDO().setId(exists.getId())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()).setAddTime(LocalDateTime.now())
                    .setMuted(false).setPinned(false).setBlocked(false);
            if (displayName != null) {
                update.setDisplayName(displayName);
            }
            if (addSource != null) {
                update.setAddSource(addSource);
            }
            friendMapper.updateById(update);
            return;
        }
        // 情况二：不存在记录 → 直接插入新记录
        ImFriendDO friend = ImFriendDO.builder().userId(userId).friendUserId(friendUserId)
                .muted(false).pinned(false).blocked(false)
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
    public void deleteFriend0(Long userId, Long friendUserId) {
        ImFriendDO exists = friendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (exists == null || CommonStatusEnum.isDisable(exists.getStatus())) {
            return;
        }
        friendMapper.updateById(new ImFriendDO().setId(exists.getId())
                .setStatus(CommonStatusEnum.DISABLE.getStatus()).setDeleteTime(LocalDateTime.now()));
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImFriendDO> getFriendPage(ImFriendManagerPageReqVO reqVO) {
        return friendMapper.selectPage(reqVO);
    }

}
