package cn.iocoder.yudao.module.ai.service.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRolePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRoleSaveMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRoleSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.model.AiChatRoleMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 聊天角色 Service 实现类
 *
 * @author fansili
 */
@Service
@Slf4j
public class AiChatRoleServiceImpl implements AiChatRoleService {

    @Resource
    private AiChatRoleMapper chatRoleMapper;

    @Override
    public Long createChatRole(AiChatRoleSaveReqVO createReqVO) {
        AiChatRoleDO chatRole = BeanUtils.toBean(createReqVO, AiChatRoleDO.class);
        chatRoleMapper.insert(chatRole);
        return chatRole.getId();
    }

    @Override
    public Long createChatRoleMy(AiChatRoleSaveMyReqVO createReqVO, Long userId) {
        AiChatRoleDO chatRole = BeanUtils.toBean(createReqVO, AiChatRoleDO.class).setUserId(userId)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setPublicStatus(false);
        chatRoleMapper.insert(chatRole);
        return chatRole.getId();
    }

    @Override
    public void updateChatRole(AiChatRoleSaveReqVO updateReqVO) {
        // 校验存在
        validateChatRoleExists(updateReqVO.getId());
        // 更新
        AiChatRoleDO updateObj = BeanUtils.toBean(updateReqVO, AiChatRoleDO.class);
        chatRoleMapper.updateById(updateObj);
    }

    @Override
    public void updateChatRoleMy(AiChatRoleSaveMyReqVO updateReqVO, Long userId) {
        // 校验存在
        AiChatRoleDO chatRole = validateChatRoleExists(updateReqVO.getId());
        if (ObjectUtil.notEqual(chatRole.getUserId(), userId)) {
            throw exception(CHAT_ROLE_NOT_EXISTS);
        }

        // 更新
        AiChatRoleDO updateObj = BeanUtils.toBean(updateReqVO, AiChatRoleDO.class);
        chatRoleMapper.updateById(updateObj);
    }

    @Override
    public void deleteChatRole(Long id) {
        // 校验存在
        validateChatRoleExists(id);
        // 删除
        chatRoleMapper.deleteById(id);
    }

    @Override
    public void deleteChatRoleMy(Long id, Long userId) {
        // 校验存在
        AiChatRoleDO chatRole = validateChatRoleExists(id);
        if (ObjectUtil.notEqual(chatRole.getUserId(), userId)) {
            throw exception(CHAT_ROLE_NOT_EXISTS);
        }
        // 删除
        chatRoleMapper.deleteById(id);
    }

    private AiChatRoleDO validateChatRoleExists(Long id) {
        AiChatRoleDO chatRole = chatRoleMapper.selectById(id);
        if (chatRole == null) {
            throw exception(CHAT_ROLE_NOT_EXISTS);
        }
        return chatRole;
    }

    @Override
    public AiChatRoleDO getChatRole(Long id) {
        return chatRoleMapper.selectById(id);
    }

    @Override
    public List<AiChatRoleDO> getChatRoleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return chatRoleMapper.selectBatchIds(ids);
    }

    @Override
    public AiChatRoleDO validateChatRole(Long id) {
        AiChatRoleDO chatRole = validateChatRoleExists(id);
        if (CommonStatusEnum.isDisable(chatRole.getStatus())) {
            throw exception(CHAT_ROLE_DISABLE, chatRole.getName());
        }
        return chatRole;
    }

    @Override
    public PageResult<AiChatRoleDO> getChatRolePage(AiChatRolePageReqVO pageReqVO) {
        return chatRoleMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<AiChatRoleDO> getChatRoleMyPage(AiChatRolePageReqVO pageReqVO, Long userId) {
        return chatRoleMapper.selectPageByMy(pageReqVO, userId);
    }

    @Override
    public List<String> getChatRoleCategoryList() {
        List<AiChatRoleDO> list = chatRoleMapper.selectListGroupByCategory(CommonStatusEnum.ENABLE.getStatus());
        return convertList(list, AiChatRoleDO::getCategory, role -> role != null && StrUtil.isNotBlank(role.getCategory()));
    }

    @Override
    public List<AiChatRoleDO> getChatRoleListByName(String name) {
        return chatRoleMapper.selectListByName(name);
    }

}

