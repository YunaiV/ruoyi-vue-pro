package cn.iocoder.yudao.module.im.service.friend;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.FRIEND;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

/**
 * IM 好友关系 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImFriendServiceImpl implements ImFriendService {

    @Resource
    private ImFriendMapper imFriendMapper;

    @Resource
    private AdminUserApi adminUserApi;

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
        ImFriendDO updateObj = new ImFriendDO();
        updateObj.setId(friend.getId());
        updateObj.setMuted(reqVO.getMuted());
        imFriendMapper.updateById(updateObj);
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

        // 双向绑定
        // DONE @AI：改成 createOrUpdateFriend 之类的接口，内部自动处理"已删除的好友再加回来"的情况；或者直接在数据库层面通过唯一索引 + 物理更新来解决。
        // 说明：bindFriend 内部已处理 DISABLE → ENABLE 的恢复逻辑
        bindFriend(userId, friendUserId);
        bindFriend(friendUserId, userId);

        // TODO @AI：推送异步化：1）好友的打招呼；2）通知好友发生变化的消息；

        // TODO @AI：实现如下：
        // TODO @芋艿：【对齐】这里缺少了好友添加成功的系统提示消息，
        //  需要通过 `ImPrivateMessageService` 插入一条 TIP_TEXT 类型的消息并走 WebSocket 推送给双方。
        //  涉及 WebSocket 推送器的事件类型扩展（当前 `ImWsEventType` 无 FRIEND_NEW），待统一后补上。
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendUserId) {
        // 双向标记为 DISABLE + 记录 deleteTime
        unbindFriend(userId, friendUserId);
        unbindFriend(friendUserId, userId);

        // TODO @芋艿：【对齐】这里缺少了好友删除的系统提示 + FRIEND_DEL WebSocket 推送，
        //  待 WS 事件类型扩展后补上。
    }

    /**
     * 单向绑定好友关系：
     * - 首次：新增记录（status=ENABLE，addTime=now）
     * - 已存在且 ENABLE：无需重复操作
     * - 已存在但 DISABLE：恢复关系（status=ENABLE，刷新 addTime，清空 deleteTime）
     */
    @CacheEvict(cacheNames = FRIEND, key = "#userId + '_' + #friendUserId")
    public void bindFriend(Long userId, Long friendUserId) {
        ImFriendDO exists = imFriendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (exists != null) {
            if (CommonStatusEnum.isEnable(exists.getStatus())) {
                return; // 已是好友，跳过
            }
            // 恢复已删除的好友关系
            ImFriendDO updateObj = new ImFriendDO();
            updateObj.setId(exists.getId());
            updateObj.setStatus(CommonStatusEnum.ENABLE.getStatus());
            updateObj.setAddTime(LocalDateTime.now());
            updateObj.setDeleteTime(null);
            imFriendMapper.updateById(updateObj);
            return;
        }
        // 新增
        ImFriendDO friend = ImFriendDO.builder()
                .userId(userId)
                .friendUserId(friendUserId)
                .muted(false)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .addTime(LocalDateTime.now())
                .build();
        imFriendMapper.insert(friend);
    }

    /**
     * 单向解除好友关系（status 设为 DISABLE，记录 deleteTime）
     */
    @CacheEvict(cacheNames = FRIEND, key = "#userId + '_' + #friendUserId")
    public void unbindFriend(Long userId, Long friendUserId) {
        ImFriendDO exists = imFriendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        if (exists == null || CommonStatusEnum.isDisable(exists.getStatus())) {
            return;
        }
        ImFriendDO updateObj = new ImFriendDO();
        updateObj.setId(exists.getId());
        updateObj.setStatus(CommonStatusEnum.DISABLE.getStatus());
        updateObj.setDeleteTime(LocalDateTime.now());
        imFriendMapper.updateById(updateObj);
    }

}
