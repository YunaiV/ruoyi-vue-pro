package cn.iocoder.yudao.module.ai1.service.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.Method;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model.Ai1ModelImportReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model.Ai1ModelPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model.Ai1ModelSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.provider.Ai1ModelMapper;
import cn.iocoder.yudao.module.ai1.enums.provider.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.core.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.ai1.framework.ai.core.llm.Ai1LlmModelFactory.normalizeBaseUrl;
import static cn.iocoder.yudao.module.ai1.util.Ai1Utils.*;

/**
 * AI1 模型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
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

    // TODO @AI：想了下，还是拿回原来的 tool；
    @Override
    public List<String> getRemoteModelList(Long providerId) {
        // 1. 校验供应商存在
        Ai1ProviderDO provider = providerService.validateProviderExists(providerId);

        // 2.1 拉取远程模型：GET {baseUrl}/models；调用前解析 ${ENV} 占位符，库中的原文不改
        String baseUrl = normalizeBaseUrl(resolveSpringPlaceholders(provider.getBaseUrl()));
        String apiKey = resolveSpringPlaceholders(provider.getApiKey());
        List<Map<String, String>> headers = parseHeaders(resolveSpringPlaceholders(provider.getHeaders()));
        String body = null;
        try (HttpResponse response = buildOpenAiRequest(Method.GET, baseUrl + "/models", apiKey, headers).execute()) {
            if (response.getStatus() == HttpStatus.HTTP_OK) {
                body = response.body();
            } else {
                log.warn("[getRemoteModelList][供应商({}) 模型拉取失败，httpCode({})]", providerId, response.getStatus());
            }
        } catch (Exception e) {
            log.warn("[getRemoteModelList][供应商({}) 模型拉取失败]", providerId, e);
        }
        // 2.2 解析 data[].id：请求或解析失败时，提示检查配置
        Map<String, Object> root = JsonUtils.parseMap(body);
        if (root == null) {
            throw exception(PROVIDER_REMOTE_MODEL_LOAD_FAIL);
        }
        List<?> data = MapUtil.get(root, "data", List.class);
        List<String> models = convertList(data, item -> item instanceof Map
                ? StrUtil.blankToDefault(MapUtil.getStr((Map<?, ?>) item, "id"), null) : null);
        if (CollUtil.isEmpty(models)) {
            throw exception(PROVIDER_REMOTE_MODEL_EMPTY);
        }
        return models;
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

        // 2. 批量插入：展示名称默认同模型标识，类型默认对话，状态默认开启
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
