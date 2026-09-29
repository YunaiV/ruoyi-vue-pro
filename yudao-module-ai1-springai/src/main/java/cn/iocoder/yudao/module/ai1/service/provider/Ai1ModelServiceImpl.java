package cn.iocoder.yudao.module.ai1.service.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model.Ai1ModelImportReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model.Ai1ModelPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model.Ai1ModelSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.provider.Ai1ModelMapper;
import cn.iocoder.yudao.module.ai1.enums.provider.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.core.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;

/**
 * AI1 Provider 模型 Service 实现类
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

    @Override
    public Long createModel(Ai1ModelSaveReqVO createReqVO) {
        // 1. 校验 Provider 存在、模型标识唯一
        providerService.validateProviderExists(createReqVO.getProviderId());
        validateModelUnique(null, createReqVO.getProviderId(), createReqVO.getModel());

        // 2. 插入
        Ai1ModelDO model = BeanUtils.toBean(createReqVO, Ai1ModelDO.class);
        modelMapper.insert(model);
        return model.getId();
    }

    @Override
    public void updateModel(Ai1ModelSaveReqVO updateReqVO) {
        // 1. 校验存在、Provider 存在、模型标识唯一
        validateModelExists(updateReqVO.getId());
        providerService.validateProviderExists(updateReqVO.getProviderId());
        validateModelUnique(updateReqVO.getId(), updateReqVO.getProviderId(), updateReqVO.getModel());

        // TODO @AI：2.1 和 2.2 需要分开写注释；
        // 2. 更新，并失效已缓存的模型实例
        Ai1ModelDO updateObj = BeanUtils.toBean(updateReqVO, Ai1ModelDO.class);
        modelMapper.updateById(updateObj);
        // TODO @AI：这个缓存，如果多实例，怎么解决过期噢？
        llmModelFactory.evictByModelId(updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // TODO @AI：检查下别的，不然会报错；
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

        // 2. 删除，并失效已缓存的模型实例
        modelMapper.deleteByIds(ids);

        // TODO @AI：这个缓存，如果多实例，怎么解决过期噢？
        // TODO @AI：注释上，2. 3. 需要分开下噢
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
    @Transactional(rollbackFor = Exception.class)
    public Integer importRemoteModelList(Ai1ModelImportReqVO importReqVO) {
        // 1. 校验 Provider 存在
        providerService.validateProviderExists(importReqVO.getProviderId());
        // TODO @AI：1.1 1.2；校验序号要用同一个；其它地方也检查下；
        // 2. 过滤空白与已存在的模型标识（同时对入参去重）
        // TODO @AI：先查询出变量，后 convert，保持习惯。。。
        Set<String> existModels = convertSet(modelMapper.selectListByProviderId(importReqVO.getProviderId()), Ai1ModelDO::getModel);
        // TODO @AI：是不是也可以 convertSet 噢？
        Set<String> importModels = new LinkedHashSet<>();
        for (String model : importReqVO.getModels()) {
            if (StrUtil.isNotBlank(model) && !existModels.contains(model.trim())) {
                importModels.add(model.trim());
            }
        }
        if (CollUtil.isEmpty(importModels)) {
            throw exception(MODEL_IMPORT_ALL_EXISTS);
        }

        // 3. 批量插入：展示名称默认同模型标识，类型默认对话，状态默认开启
        // TODO @AI：convertList？
        List<Ai1ModelDO> models = new ArrayList<>(importModels.size());
        for (String model : importModels) {
            models.add(Ai1ModelDO.builder().providerId(importReqVO.getProviderId()).name(StrUtil.maxLength(model, 47))
                    .model(model).type(Ai1ModelTypeEnum.CHAT.getType()).status(CommonStatusEnum.ENABLE.getStatus()).build());
        }
        modelMapper.insertBatch(models);
        return models.size();
    }

    /**
     * 校验同一 Provider 下模型标识唯一
     */
    private void validateModelUnique(Long id, Long providerId, String model) {
        Ai1ModelDO existModel = modelMapper.selectByProviderIdAndModel(providerId, model);
        if (existModel != null && ObjUtil.notEqual(existModel.getId(), id)) {
            throw exception(MODEL_DUPLICATE, model);
        }
    }

}
