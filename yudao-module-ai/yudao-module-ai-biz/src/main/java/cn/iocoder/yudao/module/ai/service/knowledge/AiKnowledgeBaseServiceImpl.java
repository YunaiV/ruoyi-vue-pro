package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeBaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeBaseMapper;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.KNOWLEDGE_NOT_EXISTS;

/**
 * AI 知识库-基础信息 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiKnowledgeBaseServiceImpl implements AiKnowledgeBaseService {

    @Resource
    private AiChatModelService chatModalService;

    @Resource
    private AiKnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public Long createKnowledgeMy(AiKnowledgeCreateMyReqVO createReqVO, Long userId) {
        // TODO @xin：貌似直接调用 chatModalService.validateChatModel(id) 完事，不用搞个方法
        // 1. 校验模型配置
        AiChatModelDO model = validateChatModel(createReqVO.getModelId());

        // 2. 插入知识库
        // TODO @xin：不用 DO 结尾
        AiKnowledgeBaseDO knowledgeBaseDO = BeanUtils.toBean(createReqVO, AiKnowledgeBaseDO.class)
                .setModel(model.getModel()).setUserId(userId).setStatus(CommonStatusEnum.ENABLE.getStatus());
        knowledgeBaseMapper.insert(knowledgeBaseDO);
        return knowledgeBaseDO.getId();
    }

    @Override
    public void updateKnowledgeMy(AiKnowledgeUpdateMyReqVO updateReqVO, Long userId) {
        // 1.1 校验知识库存在
        AiKnowledgeBaseDO knowledgeBaseDO = validateKnowledgeExists(updateReqVO.getId());
        if (ObjUtil.notEqual(knowledgeBaseDO.getUserId(), userId)) {
            throw exception(KNOWLEDGE_NOT_EXISTS);
        }
        // 1.2 校验模型配置
        AiChatModelDO model = validateChatModel(updateReqVO.getModelId());

        // 2. 更新知识库
        AiKnowledgeBaseDO updateDO = BeanUtils.toBean(updateReqVO, AiKnowledgeBaseDO.class);
        updateDO.setModel(model.getModel());
        knowledgeBaseMapper.updateById(updateDO);
    }

    private AiChatModelDO validateChatModel(Long id) {
        AiChatModelDO model = chatModalService.validateChatModel(id);
        Assert.notNull(model, "未找到对应嵌入模型");
        return model;
    }

    public AiKnowledgeBaseDO validateKnowledgeExists(Long id) {
        AiKnowledgeBaseDO knowledgeBase = knowledgeBaseMapper.selectById(id);
        if (knowledgeBase == null) {
            throw exception(KNOWLEDGE_NOT_EXISTS);
        }
        return knowledgeBase;
    }

}
