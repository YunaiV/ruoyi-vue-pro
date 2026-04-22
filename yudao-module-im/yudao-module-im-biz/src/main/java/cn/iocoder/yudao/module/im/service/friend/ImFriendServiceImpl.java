package cn.iocoder.yudao.module.im.service.friend;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.mysql.friend.ImFriendMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.hutool.extra.spring.SpringUtil;
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

        // 2. 双向绑定
        getSelf().addFriend0(userId, friendUserId);
        getSelf().addFriend0(friendUserId, userId);

        // TODO @芋艿：好友添加成功后：1）插入 TIP_TEXT 系统提示消息并推送双方；2）通知双方好友列表变更。
        //  依赖 WebSocket 事件类型扩展（当前无 FRIEND_NEW），待统一后补上。
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendUserId) {
        // 双向标记为 DISABLE + 记录 deleteTime
        getSelf().deleteFriend0(userId, friendUserId);
        getSelf().deleteFriend0(friendUserId, userId);

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
    public void addFriend0(Long userId, Long friendUserId) {
        ImFriendDO exists = imFriendMapper.selectByUserIdAndFriendUserId(userId, friendUserId);
        // 情况一：恢复已删除的好友关系
        if (exists != null) {
            if (CommonStatusEnum.isEnable(exists.getStatus())) {
                return;
            }
            imFriendMapper.updateById(new ImFriendDO().setId(exists.getId())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()).setAddTime(LocalDateTime.now()));
            return;
        }
        // 情况二：新增好有关系
        ImFriendDO friend = ImFriendDO.builder().userId(userId).friendUserId(friendUserId)
                .muted(false).status(CommonStatusEnum.ENABLE.getStatus()).addTime(LocalDateTime.now()).build();
        imFriendMapper.insert(friend);
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
