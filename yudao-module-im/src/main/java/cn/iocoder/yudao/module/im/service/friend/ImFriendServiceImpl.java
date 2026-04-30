package cn.iocoder.yudao.module.im.service.friend;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendManagerRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
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
    @Lazy
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
    public List<ImFriendDO> getActiveFriendList(Long userId, Collection<Long> friendUserIds) {
        return imFriendMapper.selectListByUserIdAndFriendUserIdsAndStatus(userId, friendUserIds,
                CommonStatusEnum.ENABLE.getStatus());
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

        // 2. 更新好友属性
        imFriendMapper.updateById(new ImFriendDO().setId(friend.getId())
                .setMuted(reqVO.getMuted()).setDisplayName(reqVO.getDisplayName()));

        // 3. 推送好友更新通知（多端同步）
        imWebSocketService.sendPrivateMessageAsync(userId,
                ImPrivateMessageDTO.ofFriendUpdate(userId, reqVO.getFriendUserId()));
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
     * 并发安全：依靠 im_friend 表的唯一索引 uk_im_friend_user_friend(user_id, friend_user_id) 保证幂等，
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
            // 并发场景：另一个请求已先一步插入，且其插入的必然是 ENABLE 状态（DISABLE 场景在上方分支已处理），直接忽略
            log.warn("[addFriend0][userId({}) friendUserId({}) 并发插入冲突，忽略]", userId, friendUserId);
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

    // ==================== 管理后台 ====================

    // TODO @AI：拿到 controller 拼接数据，简化 service；
    @Override
    public PageResult<ImFriendManagerRespVO> getFriendPage(ImFriendManagerPageReqVO reqVO) {
        // 1. 分页查询
        PageResult<ImFriendDO> pageResult = imFriendMapper.selectPage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }

        // 2. 一次性批量查询用户 + 好友的昵称
        Set<Long> userIds = new HashSet<>();
        userIds.addAll(convertSet(pageResult.getList(), ImFriendDO::getUserId));
        userIds.addAll(convertSet(pageResult.getList(), ImFriendDO::getFriendUserId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);

        // 3. 转换为 VO，填充昵称
        return BeanUtils.toBean(pageResult, ImFriendManagerRespVO.class, vo -> {
            MapUtils.findAndThen(userMap, vo.getUserId(), user -> vo.setUserNickname(user.getNickname()));
            MapUtils.findAndThen(userMap, vo.getFriendUserId(), user -> vo.setFriendNickname(user.getNickname()));
        });
    }

}
