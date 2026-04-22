package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.friend.FriendUpdateMessage;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.FRIEND;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FRIEND_ADD_SELF;
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
    private ImFriendMapper imFriendMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private ImWebSocketService imWebSocketService;
    @Resource
    private ImPrivateMessageService privateMessageService;

    @Override
    @Cacheable(cacheNames = FRIEND, key = "#userId + '_' + #friendUserId", unless = "#result == null")
    public boolean isFriend(Long userId, Long friendUserId) {
        ImFriendDO friend = imFriendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        return friend != null && CommonStatusEnum.isEnable(friend.getStatus());
    }

    @Override
    public List<ImFriendDO> getFriendList(Long userId) {
        return imFriendMapper.selectListByUserId(userId);
    }

    @Override
    public ImFriendDO getFriend(Long userId, Long friendUserId) {
        return imFriendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
    }

    @Override
    public void updateFriend(Long userId, ImFriendUpdateReqVO reqVO) {
        // 1. 校验好友关系存在
        ImFriendDO friend = imFriendMapper.selectByUserIdAndFriendUserId(userId, reqVO.getFriendUserId());
        if (friend == null) {
            throw exception(FRIEND_NOT_FRIEND);
        }

        // 2. 更新好友属性（目前仅免打扰）
        imFriendMapper.updateById(new ImFriendDO().setId(friend.getId()).setMuted(reqVO.getMuted()));

        // 3. 推送好友更新通知（多端同步）
        FriendUpdateMessage websocketContent = new FriendUpdateMessage()
                .setFriendUserId(reqVO.getFriendUserId()).setMuted(reqVO.getMuted());
        imWebSocketService.sendPrivateMessageAsync(userId,
                ImPrivateMessageDTO.ofFriendUpdate(userId, reqVO.getFriendUserId(), websocketContent));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFriend(Long userId, Long friendUserId) {
        // 1.1 校验：不允许添加自己为好友
        if (Objects.equals(userId, friendUserId)) {
            throw exception(FRIEND_ADD_SELF);
        }
        // 1.2 校验对方存在（禁用/不存在的账号不允许添加）
        adminUserApi.validateUser(friendUserId);

        // 2. 双向绑定
        getSelf().addFriend0(userId, friendUserId);
        getSelf().addFriend0(friendUserId, userId);

        // 3.1 插入 TIP_TEXT 系统提示消息并推送双方
        privateMessageService.sendTipPrivateMessage(userId, friendUserId, FRIEND_ADD_TIP_MESSAGE);
        // 3.2 推送好友添加通知给双方
        imWebSocketService.sendPrivateMessageAsync(userId,
                ImPrivateMessageDTO.ofFriendAdd(friendUserId, userId));
        imWebSocketService.sendPrivateMessageAsync(friendUserId,
                ImPrivateMessageDTO.ofFriendAdd(userId, friendUserId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendUserId) {
        // 1. 双向标记为 DISABLE + 记录 deleteTime
        getSelf().deleteFriend0(userId, friendUserId);
        getSelf().deleteFriend0(friendUserId, userId);

        // 2.1 插入 TIP_TEXT 系统提示消息并推送双方
        privateMessageService.sendTipPrivateMessage(userId, friendUserId, FRIEND_DELETE_TIP_MESSAGE);
        // 2.2 推送好友删除通知给双方
        imWebSocketService.sendPrivateMessageAsync(userId,
                ImPrivateMessageDTO.ofFriendDelete(userId, friendUserId));
        imWebSocketService.sendPrivateMessageAsync(friendUserId,
                ImPrivateMessageDTO.ofFriendDelete(friendUserId, userId));
    }

    /**
     * 单向绑定好友关系：
     * - 首次：新增记录（status=ENABLE，addTime=now）
     * - 已存在且 ENABLE：无需重复操作
     * - 已存在但 DISABLE：恢复关系（status=ENABLE，刷新 addTime，清空 deleteTime）
     * <p>
     * 并发安全：依靠 im_friend 表的唯一索引 uk_user_friend(user_id, friend_user_id) 保证幂等，
     * 当并发 insert 触发 {@link DuplicateKeyException} 时降级为 select + update。
     */
    @CacheEvict(cacheNames = FRIEND, key = "#userId + '_' + #friendUserId")
    public void addFriend0(Long userId, Long friendUserId) {
        ImFriendDO exists = imFriendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        // 情况一：已存在记录 → 恢复或跳过
        if (exists != null) {
            if (CommonStatusEnum.isEnable(exists.getStatus())) {
                return;
            }
            imFriendMapper.updateById(new ImFriendDO().setId(exists.getId())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()).setAddTime(LocalDateTime.now()));
            return;
        }
        // 情况二：新增好友关系
        ImFriendDO friend = ImFriendDO.builder().userId(userId).friendUserId(friendUserId)
                .muted(false).status(CommonStatusEnum.ENABLE.getStatus()).addTime(LocalDateTime.now()).build();
        try {
            imFriendMapper.insert(friend);
        } catch (DuplicateKeyException e) {
            // 并发场景：另一个请求已先一步插入，降级走已存在逻辑
            log.warn("[addFriend0][userId({}) friendUserId({}) 并发插入冲突，降级处理]", userId, friendUserId);
            addFriend0(userId, friendUserId);
        }
    }

    /**
     * 单向解除好友关系（status 设为 DISABLE，记录 deleteTime）
     */
    @CacheEvict(cacheNames = FRIEND, key = "#userId + '_' + #friendUserId")
    public void deleteFriend0(Long userId, Long friendUserId) {
        ImFriendDO exists = imFriendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (exists == null || CommonStatusEnum.isDisable(exists.getStatus())) {
            return;
        }
        imFriendMapper.updateById(new ImFriendDO().setId(exists.getId())
                .setStatus(CommonStatusEnum.DISABLE.getStatus()).setDeleteTime(LocalDateTime.now()));
    }

    private ImFriendServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
