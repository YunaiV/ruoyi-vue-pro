package cn.iocoder.yudao.module.ai1.service.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.agent.Ai1AgentMapper;
import cn.iocoder.yudao.module.ai1.enums.provider.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.core.skill.Ai1SkillToolFactory;
import cn.iocoder.yudao.module.ai1.service.chat.Ai1ChatConversationService;
import cn.iocoder.yudao.module.ai1.service.provider.Ai1ProviderService;
import cn.iocoder.yudao.module.ai1.service.provider.bo.Ai1ProviderRuntime;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.AGENT_DISABLE;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.AGENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.MODEL_TYPE_NOT_CHAT;

/**
 * AI1 Agent Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1AgentServiceImpl implements Ai1AgentService {

    @Resource
    private Ai1AgentMapper agentMapper;

    @Resource
    private Ai1ProviderService providerService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1ChatConversationService chatConversationService;

    @Resource
    private Ai1SkillToolFactory skillToolFactory;

    @Override
    public Long createAgent(Ai1AgentSaveReqVO createReqVO) {
        // 1. 校验对话模型
        validateChatModel(createReqVO.getProviderId(), createReqVO.getModelId());

        // TODO @AI：“：初始为关闭，访问 UUID 在首次开启时生成”去掉这个注释；
        // 2. 插入：初始为关闭，访问 UUID 在首次开启时生成
        Ai1AgentDO agent = buildAgent(createReqVO).setStatus(CommonStatusEnum.DISABLE.getStatus());
        agentMapper.insert(agent);
        return agent.getId();
    }

    @Override
    public void updateAgent(Ai1AgentSaveReqVO updateReqVO) {
        // 1. 校验存在、对话模型
        validateAgentExists(updateReqVO.getId());
        validateChatModel(updateReqVO.getProviderId(), updateReqVO.getModelId());

        // 2. 更新
        agentMapper.updateById(buildAgent(updateReqVO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAgent(Long id) {
        deleteAgentListByIds(Collections.singletonList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAgentListByIds(List<Long> ids) {
        // 1. 校验存在
        List<Ai1AgentDO> agents = convertList(ids, this::validateAgentExists);

        // 2. 删除 Agent，同一事务内级联删除对话与消息
        agentMapper.deleteByIds(ids);
        chatConversationService.deleteChatConversationListByAgentIds(ids);

        // 3. 清理 SKILL 物化沙箱
        // TODO @AI：集群下，怎么处理噢？
        agents.forEach(agent -> skillToolFactory.evict(agent.getTenantId(), agent.getId()));
    }

    @Override
    public Ai1AgentDO getAgent(Long id) {
        return agentMapper.selectById(id);
    }

    @Override
    public Ai1AgentDO validateAgentExists(Long id) {
        Ai1AgentDO agent = agentMapper.selectById(id);
        if (agent == null) {
            throw exception(AGENT_NOT_EXISTS);
        }
        return agent;
    }

    @Override
    public Ai1AgentDO validateAgentEnabled(Long id) {
        Ai1AgentDO agent = validateAgentExists(id);
        if (CommonStatusEnum.isDisable(agent.getStatus())) {
            throw exception(AGENT_DISABLE, agent.getName());
        }
        return agent;
    }

    @Override
    public PageResult<Ai1AgentDO> getAgentPage(Ai1AgentPageReqVO pageReqVO) {
        return agentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Ai1AgentDO> getAgentListByStatus(Integer status) {
        return agentMapper.selectListByStatusOrderById(status);
    }

    @Override
    public List<Ai1AgentDO> getAgentList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return agentMapper.selectByIds(ids);
    }

    @Override
    public Long getAgentCountByModelIds(Collection<Long> modelIds) {
        if (CollUtil.isEmpty(modelIds)) {
            return 0L;
        }
        return agentMapper.selectCountByModelIds(modelIds);
    }

    @Override
    public void updateAgentStatus(Long id, Integer status) {
        // 1. 校验存在
        Ai1AgentDO agent = validateAgentExists(id);

        // 2. 更新：首次开启时生成访问 UUID，之后复用，保证已分享的地址不失效
        // TODO @AI：uuid 不用处理噢
        Ai1AgentDO updateObj = new Ai1AgentDO().setId(id).setStatus(status);
        if (CommonStatusEnum.isEnable(status) && StrUtil.isBlank(agent.getUuid())) {
            updateObj.setUuid(IdUtil.fastSimpleUUID());
        }
        agentMapper.updateById(updateObj);
    }

    /**
     * 校验对话模型：Provider、模型存在且开启，模型归属于该 Provider，且为对话模型
     */
    private void validateChatModel(Long providerId, Long modelId) {
        Ai1ProviderRuntime runtime = providerService.getProviderRuntime(providerId, modelId);
        if (!Ai1ModelTypeEnum.isChat(runtime.getModelType())) {
            throw exception(MODEL_TYPE_NOT_CHAT);
        }
    }

    // TODO @AI：不用这样处理；前端会保证 [] 的；
    /**
     * 构建 Agent：绑定集合为空时写入空列表，保证修改时可以清空绑定
     */
    private static Ai1AgentDO buildAgent(Ai1AgentSaveReqVO reqVO) {
        Ai1AgentDO agent = BeanUtils.toBean(reqVO, Ai1AgentDO.class);
        agent.setKnowledgeBaseIds(reqVO.getKnowledgeBaseIds() != null ? reqVO.getKnowledgeBaseIds() : new ArrayList<>());
        agent.setMcpIds(reqVO.getMcpIds() != null ? reqVO.getMcpIds() : new ArrayList<>());
        agent.setSkillIds(reqVO.getSkillIds() != null ? reqVO.getSkillIds() : new ArrayList<>());
        return agent;
    }

}
