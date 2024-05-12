package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.AiCommonConstants;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.role.AiChatRoleRespVO;
import cn.iocoder.yudao.module.ai.convert.AiChatConversationConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatModelMapper;
import cn.iocoder.yudao.module.ai.service.AiChatConversationService;
import cn.iocoder.yudao.module.ai.service.AiChatRoleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * chat 对话
 *
 * @fansili
 * @since v1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class AiChatConversationServiceImpl implements AiChatConversationService {

    private final AiChatModelMapper aiChatModalMapper;
    private final AiChatModelService aiChatModalService;
    private final AiChatRoleService aiChatRoleService;
    private final AiChatConversationMapper aiChatConversationMapper;

    @Override
    public Long createConversation(AiChatConversationCreateReqVO req) {
        // 获取用户id
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 默认使用 sort 排序第一个模型
        AiChatModelDO aiChatModalDO = aiChatModalMapper.selectFirstModal();
        // 查询角色
        AiChatRoleRespVO chatRoleRes = null;
        if (req.getRoleId() != null) {
            chatRoleRes = aiChatRoleService.getChatRole(req.getRoleId());
        }
        Long chatRoleId = chatRoleRes != null ? chatRoleRes.getId() : null;
        // 创建新的 Conversation
        AiChatConversationDO insertConversation = saveConversation(AiCommonConstants.CONVERSATION_DEFAULT_TITLE,
                loginUserId, chatRoleId, aiChatModalDO.getId(), aiChatModalDO.getModel()
        );
        // 返回对话id
        return insertConversation.getId();
    }

    private @NotNull AiChatConversationDO saveConversation(String title,
                                                           Long userId,
                                                           Long roleId,
                                                           Long modalId,
                                                           String model) {
        AiChatConversationDO insertConversation = new AiChatConversationDO();
        insertConversation.setId(null);
        insertConversation.setUserId(userId);
        insertConversation.setTitle(title);
        insertConversation.setPinned(false);

        insertConversation.setRoleId(roleId);
        insertConversation.setModelId(modalId);
        insertConversation.setModel(model);

        insertConversation.setTemperature(null);
        insertConversation.setMaxTokens(null);
        insertConversation.setMaxContexts(null);
        aiChatConversationMapper.insert(insertConversation);
        return insertConversation;
    }

    @Override
    public Boolean updateConversation(AiChatConversationUpdateReqVO updateReqVO) {
        // 校验对话是否存在
        validateExists(updateReqVO.getId());
        // 获取模型信息并验证
        aiChatModalService.validateChatModel(updateReqVO.getModelId());
        // 更新对话信息
        AiChatConversationDO updateAiChatConversationDO
                = AiChatConversationConvert.INSTANCE.convertAiChatConversationDO(updateReqVO);
        return aiChatConversationMapper.updateById(updateAiChatConversationDO) > 0;
    }

    @Override
    public List<AiChatConversationRespVO> listConversation() {
        // 获取用户id
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询前100对话
        List<AiChatConversationDO> top100Conversation
                = aiChatConversationMapper.selectTop100Conversation(loginUserId, null);
        return AiChatConversationConvert.INSTANCE.covnertChatConversationResList(top100Conversation);
    }

    @Override
    public AiChatConversationRespVO getConversationOfValidate(Long id) {
        AiChatConversationDO aiChatConversationDO = validateExists(id);
        return AiChatConversationConvert.INSTANCE.covnertChatConversationRes(aiChatConversationDO);
    }

    @Override
    public Boolean deleteConversation(Long id) {
        return aiChatConversationMapper.deleteById(id) > 0;
    }

    public @NotNull AiChatConversationDO validateExists(Long id) {
        AiChatConversationDO aiChatConversationDO = aiChatConversationMapper.selectById(id);
        if (aiChatConversationDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CONVERSATION_NOT_EXISTS);
        }
        return aiChatConversationDO;
    }
}
