package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.convert.AiChatConversationConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.chat.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.*;
import static cn.iocoder.yudao.module.ai.ErrorCodeConstants.CHAT_CONVERSATION_MODEL_ERROR;
import static cn.iocoder.yudao.module.ai.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

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

    @Override
    public Long createChatConversationMy(AiChatConversationCreateMyReqVO createReqVO, Long userId) {
        // 1.1 获得 AiChatRoleDO 聊天角色
        AiChatRoleDO role = createReqVO.getRoleId() != null ? chatRoleService.validateChatRole(createReqVO.getRoleId())
                : chatRoleService.getRequiredDefaultChatRole();
        Assert.notNull(role, "必须找到聊天角色");
        // 1.2 获得 AiChatModelDO 聊天模型
        AiChatModelDO model = role.getModelId() != null ? chatModalService.validateChatModel(role.getModelId())
                : chatModalService.getRequiredDefaultChatModel();
        Assert.notNull(model, "必须找到默认模型");
        validateChatModel(model);

        // 2. 创建 AiChatConversationDO 聊天对话
        AiChatConversationDO conversation = new AiChatConversationDO()
                .setUserId(userId).setTitle(AiChatConversationDO.TITLE_DEFAULT).setPinned(false)
                .setRoleId(role.getId()).setModelId(model.getId()).setModel(model.getModel())
                .setTemperature(model.getTemperature()).setMaxTokens(model.getMaxTokens()).setMaxContexts(model.getMaxContexts());
        chatConversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public void updateChatConversationMy(AiChatConversationUpdateMyReqVO updateReqVO, Long userId) {
        // 1.1 校验对话是否存在
        AiChatConversationDO conversation = validateExists(updateReqVO.getId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        // 1.2 校验模型是否存在
        AiChatModelDO model;
        if (updateReqVO.getModelId() != null) {
            model = chatModalService.validateChatModel(updateReqVO.getModelId());
            Assert.notNull(model, "必须找到默认模型");
        }
        // 1.3 校验温度参数、Token 数量、消息数量 TODO

        // 更新对话信息
        chatConversationMapper.updateById(BeanUtils.toBean(updateReqVO, AiChatConversationDO.class));
    }

    @Override
    public List<AiChatConversationDO> getChatConversationListByUserId(Long userId) {
        return chatConversationMapper.selectListByUserId(userId);
    }

    private void validateChatModel(AiChatModelDO model) {
        if (ObjectUtil.isAllNotEmpty(model.getTemperature(), model.getMaxTokens(), model.getMaxContexts())) {
            return;
        }
        throw exception(CHAT_CONVERSATION_MODEL_ERROR);
    }

    @Override
    public AiChatConversationRespVO getConversationOfValidate(Long id) {
        AiChatConversationDO aiChatConversationDO = validateExists(id);
        return AiChatConversationConvert.INSTANCE.covnertChatConversationRes(aiChatConversationDO);
    }

    @Override
    public Boolean deleteConversation(Long id) {
        return chatConversationMapper.deleteById(id) > 0;
    }

    public AiChatConversationDO validateExists(Long id) {
        AiChatConversationDO conversation = chatConversationMapper.selectById(id);
        if (conversation == null) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        return conversation;
    }

}
