package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMapper;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.GROUP;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

// TODO @芋艿：群主的调整，暂时不考虑。后续在弄；
/**
 * 群 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImGroupServiceImpl implements ImGroupService {

    @Resource
    private ImGroupMapper groupMapper;

    @Resource
    @Lazy // 避免循环依赖
    private ImGroupMemberService groupMemberService;
    @Resource
    @Lazy // 避免循环依赖
    private ImGroupMessageService groupMessageService;
    @Resource
    private ImWebSocketService webSocketService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImGroupDO createGroup(ImGroupCreateReqVO createReqVO, Long userId) {
        // 1. 插入群记录
        ImGroupDO group = BeanUtils.toBean(createReqVO, ImGroupDO.class)
                .setOwnerUserId(userId).setStatus(CommonStatusEnum.ENABLE.getStatus());
        groupMapper.insert(group);

        // 2. 将群主插入为群成员
        groupMemberService.addGroupMember(group.getId(), userId);

        // 3. 推送群创建事件给群主（多端同步）
        webSocketService.sendGroupMessageAsync(userId,
                ImGroupMessageDTO.ofGroupCreate(userId, group.getId()));
        return group;
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#updateReqVO.id")
    @Transactional(rollbackFor = Exception.class)
    public ImGroupDO updateGroup(ImGroupUpdateReqVO updateReqVO, Long userId) {
        // 1. 校验群存在
        ImGroupDO group = validateGroupExists(updateReqVO.getId());

        // 2. 更新群信息（name、avatar、notice → im_group 表）
        boolean hasGroupInfoChange = !ObjUtil.isAllEmpty(
                updateReqVO.getName(), updateReqVO.getAvatar(), updateReqVO.getNotice());
        if (hasGroupInfoChange) {
            if (ObjUtil.notEqual(group.getOwnerUserId(), userId)) {
                throw exception(GROUP_NOT_OWNER);
            }
            // 2.1 更新数据库
            ImGroupDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupDO.class);
            groupMapper.updateById(updateObj);
            // 同步到内存中的 group 对象，避免再次查询
            BeanUtil.copyProperties(updateReqVO, group, CopyOptions.create().ignoreNullValue());

            // 2.2 群信息的变更，推送给所有群成员
            List<ImGroupMemberDO> members = groupMemberService.getActiveGroupMemberListByGroupId(group.getId());
            Set<Long> memberUserIds = convertSet(members, ImGroupMemberDO::getUserId);
            webSocketService.sendGroupMessageAsync(memberUserIds,
                    ImGroupMessageDTO.ofGroupUpdate(userId, group.getId()));
        }

        // 3. 更新当前用户的群成员信息（displayUserName、displayGroupName → im_group_member 表）
        // TODO @AI：ObjUtil.isAllEmpty(？
        if (updateReqVO.getDisplayUserName() != null || updateReqVO.getDisplayGroupName() != null) {
            groupMemberService.updateGroupMember(updateReqVO.getId(), userId,
                    updateReqVO.getDisplayUserName(), updateReqVO.getDisplayGroupName());
        }
        return group;
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#id")
    @Transactional(rollbackFor = Exception.class)
    public void dissolveGroup(Long id, Long userId) {
        // 1.1 校验群存在
        ImGroupDO group = validateGroupExists(id);
        // 1.2 校验当前用户是群主
        if (ObjUtil.notEqual(group.getOwnerUserId(), userId)) {
            throw exception(GROUP_NOT_OWNER);
        }
        // TODO @AI：增加一个是否为群主的 public 方法；因为别的模块也需要使用；

        // 3. 更新群状态为已解散
        // TODO @AI：链式调用；
        ImGroupDO updateObj = new ImGroupDO();
        updateObj.setId(id);
        updateObj.setStatus(CommonStatusEnum.DISABLE.getStatus());
        updateObj.setDissolvedTime(LocalDateTime.now());
        groupMapper.updateById(updateObj);

        // 4. 清理已读缓存
        groupMessageService.deleteReadMaxMessageIdMap(id);
        // TODO @AI：推送群解散的提示；
        // TODO @AI：推送群信息变化；
    }

    // TODO @AI：缓存还是加在这个方法上；不加在 getGroup 上；
    @Override
    public ImGroupDO validateGroupExists(Long id) {
        ImGroupDO group = getSelf().getGroup(id);
        if (group == null) {
            throw exception(GROUP_NOT_EXISTS);
        }
        if (Boolean.TRUE.equals(group.getBanned())) {
            throw exception(GROUP_BANNED);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(group.getStatus())) {
            throw exception(GROUP_DISSOLVED);
        }
        return group;
    }

    @Override
    @Cacheable(cacheNames = GROUP, key = "#id", unless = "#result == null")
    public ImGroupDO getGroup(Long id) {
        return groupMapper.selectById(id);
    }

    @Override
    public List<ImGroupDO> getActiveGroupList(Long userId) {
        // 1. 查用户所在的、仍有效的群成员记录（仅 ENABLE 状态）
        List<ImGroupMemberDO> members = groupMemberService.getActiveGroupMemberListByUserId(userId);
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        Set<Long> groupIds = convertSet(members, ImGroupMemberDO::getGroupId);
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyList();
        }

        // 2. 批量查询群信息（仅 ENABLE 状态）
        return groupMapper.selectListByIds(groupIds, CommonStatusEnum.ENABLE.getStatus(), null);
    }

    private ImGroupServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}