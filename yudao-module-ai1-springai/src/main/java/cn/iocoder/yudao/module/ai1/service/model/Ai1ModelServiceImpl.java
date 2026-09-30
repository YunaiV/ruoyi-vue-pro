package cn.iocoder.yudao.module.ai1.service.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelImportReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.model.Ai1ModelMapper;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.harness.model.Ai1ProviderTool;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.ai1.util.Ai1Utils.parseHeaders;
import static cn.iocoder.yudao.module.ai1.util.Ai1Utils.resolveSpringPlaceholders;

/**
 * AI1 模型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1ModelServiceImpl implements Ai1ModelService {

    @Resource
    private Ai1ModelMapper modelMapper;

    @Resource
    private Ai1ProviderService providerService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1AgentService agentService;

    @Resource
    private Ai1LlmModelFactory llmModelFactory;

    @Resource
    private Ai1ProviderTool providerTool;

    @Override
    public Long createModel(Ai1ModelSaveReqVO createReqVO) {
        // 1. 校验供应商存在、模型标识唯一
        providerService.validateProviderExists(createReqVO.getProviderId());
        validateModelUnique(null, createReqVO.getProviderId(), createReqVO.getModel());

        // 2. 插入
        Ai1ModelDO model = BeanUtils.toBean(createReqVO, Ai1ModelDO.class);
        modelMapper.insert(model);
        return model.getId();
    }

    @Override
    public void updateModel(Ai1ModelSaveReqVO updateReqVO) {
        // 1. 校验存在、供应商存在、模型标识唯一
        validateModelExists(updateReqVO.getId());
        providerService.validateProviderExists(updateReqVO.getProviderId());
        validateModelUnique(updateReqVO.getId(), updateReqVO.getProviderId(), updateReqVO.getModel());

        // 2. 更新
        Ai1ModelDO updateObj = BeanUtils.toBean(updateReqVO, Ai1ModelDO.class);
        modelMapper.updateById(updateObj);

        // 3. 失效本节点已缓存的模型实例
        llmModelFactory.evictByModelId(updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long id) {
        deleteModelListByIds(Collections.singletonList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModelListByIds(List<Long> ids) {
        // 1. 校验存在；被 Agent 使用时禁止删除
        ids.forEach(this::validateModelExists);
        if (agentService.getAgentCountByModelIds(ids) > 0) {
            throw exception(MODEL_USED_BY_AGENT);
        }

        // 2. 删除
        modelMapper.deleteByIds(ids);

        // 3. 失效本节点已缓存的模型实例
        ids.forEach(llmModelFactory::evictByModelId);
    }

    @Override
    public Ai1ModelDO getModel(Long id) {
        return modelMapper.selectById(id);
    }

    @Override
    public Ai1ModelDO validateModelExists(Long id) {
        Ai1ModelDO model = modelMapper.selectById(id);
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        return model;
    }

    @Override
    public PageResult<Ai1ModelDO> getModelPage(Ai1ModelPageReqVO pageReqVO) {
        return modelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Ai1ModelDO> getModelListByProviderId(Long providerId) {
        return modelMapper.selectListByProviderId(providerId);
    }

    @Override
    public List<Ai1ModelDO> getModelListByProviderIdAndTypeAndStatus(Long providerId, Integer type, Integer status) {
        return modelMapper.selectListByProviderIdAndTypeAndStatus(providerId, type, status);
    }

    @Override
    public List<Ai1ModelDO> getModelList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return modelMapper.selectByIds(ids);
    }

    @Override
    public Long getModelCountByProviderIds(Collection<Long> providerIds) {
        if (CollUtil.isEmpty(providerIds)) {
            return 0L;
        }
        return modelMapper.selectCountByProviderIds(providerIds);
    }

    @Override
    public List<String> getRemoteModelList(Long providerId) {
        // 1. 校验供应商存在
        Ai1ProviderDO provider = providerService.validateProviderExists(providerId);

        // 2. 拉取远程模型：调用前解析 ${ENV} 占位符，库中的原文不改
        return providerTool.listModels(resolveSpringPlaceholders(provider.getBaseUrl()),
                resolveSpringPlaceholders(provider.getApiKey()), parseHeaders(resolveSpringPlaceholders(provider.getHeaders())));
    }

    @Override
    public Ai1ModelRespBO getModelRespBO(Long providerId, Long modelId) {
        // 1.1 校验供应商存在且开启
        Ai1ProviderDO provider = providerService.validateProviderExists(providerId);
        if (CommonStatusEnum.isDisable(provider.getStatus())) {
            throw exception(PROVIDER_DISABLE, provider.getName());
        }
        // 1.2 校验模型存在、开启，且归属于该供应商
        Ai1ModelDO model = validateModelExists(modelId);
        if (ObjUtil.notEqual(model.getProviderId(), providerId)) {
            throw exception(MODEL_NOT_BELONG_PROVIDER);
        }
        if (CommonStatusEnum.isDisable(model.getStatus())) {
            throw exception(MODEL_DISABLE, model.getName());
        }

        // 2. 组装调用参数：解析 ${ENV} 占位符，库中的原文不改，响应也不返回解析结果
        return new Ai1ModelRespBO().setProviderId(provider.getId())
                .setModelId(model.getId()).setModel(model.getModel()).setModelType(model.getType())
                .setBaseUrl(resolveSpringPlaceholders(provider.getBaseUrl()))
                .setApiKey(resolveSpringPlaceholders(provider.getApiKey()))
                .setHeaders(parseHeaders(resolveSpringPlaceholders(provider.getHeaders())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer importRemoteModelList(Ai1ModelImportReqVO importReqVO) {
        // 1.1 校验供应商存在
        providerService.validateProviderExists(importReqVO.getProviderId());
        // 1.2 过滤空白与已存在的模型标识（同时对入参去重，保持入参顺序）
        List<Ai1ModelDO> existModelList = modelMapper.selectListByProviderId(importReqVO.getProviderId());
        Set<String> existModels = convertSet(existModelList, Ai1ModelDO::getModel);
        Set<String> importModels = convertSetBySupplier(importReqVO.getModels(), StrUtil::trim, LinkedHashSet::new);
        importModels.removeIf(model -> StrUtil.isBlank(model) || existModels.contains(model));
        if (CollUtil.isEmpty(importModels)) {
            throw exception(MODEL_IMPORT_ALL_EXISTS);
        }

        // 2. 批量插入：展示名称默认同模型标识，类型默认会话，状态默认开启
        List<Ai1ModelDO> models = convertList(importModels, model -> Ai1ModelDO.builder()
                .providerId(importReqVO.getProviderId()).name(StrUtil.maxLength(model, 47)).model(model)
                .type(Ai1ModelTypeEnum.CHAT.getType()).status(CommonStatusEnum.ENABLE.getStatus()).build());
        modelMapper.insertBatch(models);
        return models.size();
    }

    /**
     * 校验同一供应商下模型标识唯一
     */
    private void validateModelUnique(Long id, Long providerId, String model) {
        Ai1ModelDO existModel = modelMapper.selectByProviderIdAndModel(providerId, model);
        if (existModel != null && ObjUtil.notEqual(existModel.getId(), id)) {
            throw exception(MODEL_DUPLICATE, model);
        }
    }

}
