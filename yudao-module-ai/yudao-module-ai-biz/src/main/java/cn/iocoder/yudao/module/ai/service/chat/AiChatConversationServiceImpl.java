package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.chat.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_MODEL_ERROR;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

/**
 * AI 聊天对话 Service 实现类
 *
 * @author fansili
 */
@Service
@Validated
@Slf4j
public class AiChatConversationServiceImpl implements AiChatConversationService {

    @Resource
    private AiChatConversationMapper chatConversationMapper;

    @Resource
    private AiChatModelService chatModalService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private AiKnowledgeService knowledgeService;

    @Override
    public Long createChatConversationMy(AiChatConversationCreateMyReqVO createReqVO, Long userId) {
        // 1.1 获得 AiChatRoleDO 聊天角色
        AiChatRoleDO role = createReqVO.getRoleId() != null ? chatRoleService.validateChatRole(createReqVO.getRoleId()) : null;
        // 1.2 获得 AiChatModelDO 聊天模型
        AiChatModelDO model = role != null && role.getModelId() != null ? chatModalService.validateChatModel(role.getModelId())
                : chatModalService.getRequiredDefaultChatModel();
        Assert.notNull(model, "必须找到默认模型");
        validateChatModel(model);

        // 1.3 校验知识库
        if (Objects.nonNull(createReqVO.getKnowledgeId())) {
            knowledgeService.validateKnowledgeExists(createReqVO.getKnowledgeId());
        }

        // 2. 创建 AiChatConversationDO 聊天对话
        AiChatConversationDO conversation = new AiChatConversationDO().setUserId(userId).setPinned(false)
                .setModelId(model.getId()).setModel(model.getModel()).setKnowledgeId(createReqVO.getKnowledgeId())
                .setTemperature(model.getTemperature()).setMaxTokens(model.getMaxTokens()).setMaxContexts(model.getMaxContexts());
        if (role != null) {
            conversation.setTitle(role.getName()).setRoleId(role.getId()).setSystemMessage(role.getSystemMessage());
        } else {
            conversation.setTitle(AiChatConversationDO.TITLE_DEFAULT);
        }
        chatConversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public void updateChatConversationMy(AiChatConversationUpdateMyReqVO updateReqVO, Long userId) {
        // 1.1 校验对话是否存在
        AiChatConversationDO conversation = validateChatConversationExists(updateReqVO.getId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        // 1.2 校验模型是否存在（修改模型的情况）
        AiChatModelDO model = null;
        if (updateReqVO.getModelId() != null) {
            model = chatModalService.validateChatModel(updateReqVO.getModelId());
        }

        // 1.3 校验知识库是否存在
        if (updateReqVO.getKnowledgeId() != null) {
            knowledgeService.validateKnowledgeExists(updateReqVO.getKnowledgeId());
        }

        // 2. 更新对话信息
        AiChatConversationDO updateObj = BeanUtils.toBean(updateReqVO, AiChatConversationDO.class);
        if (Boolean.TRUE.equals(updateReqVO.getPinned())) {
            updateObj.setPinnedTime(LocalDateTime.now());
        }
        if (model != null) {
            updateObj.setModel(model.getModel());
        }
        chatConversationMapper.updateById(updateObj);
    }

    @Override
    public List<AiChatConversationDO> getChatConversationListByUserId(Long userId) {
        return chatConversationMapper.selectListByUserId(userId);
    }

    @Override
    public AiChatConversationDO getChatConversation(Long id) {
        return chatConversationMapper.selectById(id);
    }

    @Override
    public void deleteChatConversationMy(Long id, Long userId) {
        // 1. 校验对话是否存在
        AiChatConversationDO conversation = validateChatConversationExists(id);
        if (conversation == null || ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        // 2. 执行删除
        chatConversationMapper.deleteById(id);
    }

    @Override
    public void deleteChatConversationByAdmin(Long id) {
        // 1. 校验对话是否存在
        AiChatConversationDO conversation = validateChatConversationExists(id);
        if (conversation == null) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        // 2. 执行删除
        chatConversationMapper.deleteById(id);
    }

    private void validateChatModel(AiChatModelDO model) {
        if (ObjectUtil.isAllNotEmpty(model.getTemperature(), model.getMaxTokens(), model.getMaxContexts())) {
            return;
        }
        throw exception(CHAT_CONVERSATION_MODEL_ERROR);
    }

    public AiChatConversationDO validateChatConversationExists(Long id) {
        AiChatConversationDO conversation = chatConversationMapper.selectById(id);
        if (conversation == null) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        return conversation;
    }

    @Override
    public void deleteChatConversationMyByUnpinned(Long userId) {
        List<AiChatConversationDO> list = chatConversationMapper.selectListByUserIdAndPinned(userId, false);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        chatConversationMapper.deleteBatchIds(convertList(list, AiChatConversationDO::getId));
    }

    @Override
    public PageResult<AiChatConversationDO> getChatConversationPage(AiChatConversationPageReqVO pageReqVO) {
        return chatConversationMapper.selectChatConversationPage(pageReqVO);
    }

}
