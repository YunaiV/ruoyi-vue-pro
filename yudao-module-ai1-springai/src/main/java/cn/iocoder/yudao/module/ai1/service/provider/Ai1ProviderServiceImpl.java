package cn.iocoder.yudao.module.ai1.service.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.provider.Ai1ProviderPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.provider.Ai1ProviderSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.provider.Ai1ProviderMapper;
import cn.iocoder.yudao.module.ai1.framework.ai.core.config.Ai1ConfigPlaceholders;
import cn.iocoder.yudao.module.ai1.framework.ai.core.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.service.provider.bo.Ai1ProviderRuntime;
import cn.iocoder.yudao.module.ai1.tool.provider.Ai1ProviderTool;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;

/**
 * AI1 Provider Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1ProviderServiceImpl implements Ai1ProviderService {

    @Resource
    private Ai1ProviderMapper providerMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1ModelService modelService;

    @Resource
    private Ai1ProviderTool providerTool;

    @Resource
    private Environment environment;

    @Resource
    private Ai1LlmModelFactory llmModelFactory;

    @Override
    public Long createProvider(Ai1ProviderSaveReqVO createReqVO) {
        // 1. 校验附属 Header 格式
        validateHeaders(createReqVO.getHeaders());

        // 2. 插入
        Ai1ProviderDO provider = BeanUtils.toBean(createReqVO, Ai1ProviderDO.class);
        providerMapper.insert(provider);
        return provider.getId();
    }

    @Override
    public void updateProvider(Ai1ProviderSaveReqVO updateReqVO) {
        // 1. 校验存在、附属 Header 格式
        validateProviderExists(updateReqVO.getId());
        validateHeaders(updateReqVO.getHeaders());

        // 2. 更新：API 密钥为空时置为 null，由 updateById 跳过该字段，保持原密钥
        Ai1ProviderDO updateObj = BeanUtils.toBean(updateReqVO, Ai1ProviderDO.class);
        if (StrUtil.isBlank(updateObj.getApiKey())) {
            updateObj.setApiKey(null);
        }
        providerMapper.updateById(updateObj);

        // 3. 地址、密钥、Header、状态可能变化，失效已缓存的模型实例
        // TODO @AI：多节点下，怎么处理？？
        llmModelFactory.evictByProviderId(updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProvider(Long id) {
        deleteProviderListByIds(Collections.singletonList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProviderListByIds(List<Long> ids) {
        // 1. 校验存在；其下存在模型时禁止删除
        ids.forEach(this::validateProviderExists);
        if (modelService.getModelCountByProviderIds(ids) > 0) {
            throw exception(PROVIDER_HAS_MODEL);
        }

        // 2. 删除，并失效已缓存的模型实例
        providerMapper.deleteByIds(ids);

        // TODO @AI：多节点下，怎么处理？？
        // TODO @AI：注释要 2. 和 3. 分开噢；
        ids.forEach(llmModelFactory::evictByProviderId);
    }

    @Override
    public Ai1ProviderDO getProvider(Long id) {
        return providerMapper.selectById(id);
    }

    @Override
    public Ai1ProviderDO validateProviderExists(Long id) {
        Ai1ProviderDO provider = providerMapper.selectById(id);
        if (provider == null) {
            throw exception(PROVIDER_NOT_EXISTS);
        }
        return provider;
    }

    @Override
    public PageResult<Ai1ProviderDO> getProviderPage(Ai1ProviderPageReqVO pageReqVO) {
        return providerMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Ai1ProviderDO> getProviderListByStatus(Integer status) {
        return providerMapper.selectListByStatus(status);
    }

    @Override
    public List<Ai1ProviderDO> getProviderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return providerMapper.selectByIds(ids);
    }

    // TODO @AI：testProviderConnect 把，方法名
    @Override
    public Ai1ProviderTool.ConnectResult testConnect(Long id) {
        Ai1ProviderDO provider = validateProviderExists(id);
        return providerTool.testConnect(resolve(provider.getBaseUrl()), resolve(provider.getApiKey()), resolve(provider.getHeaders()));
    }

    @Override
    public List<String> getRemoteModelList(Long id) {
        // 1. 校验存在
        Ai1ProviderDO provider = validateProviderExists(id);

        // 2. 拉取远程模型
        List<String> models = providerTool.listModels(resolve(provider.getBaseUrl()), resolve(provider.getApiKey()), resolve(provider.getHeaders()));
        if (models == null) {
            throw exception(PROVIDER_REMOTE_MODEL_LOAD_FAIL);
        }
        if (CollUtil.isEmpty(models)) {
            throw exception(PROVIDER_REMOTE_MODEL_EMPTY);
        }
        return models;
    }

    @Override
    public Ai1ProviderRuntime getProviderRuntime(Long providerId, Long modelId) {
        // 1.1 校验 Provider 存在且开启
        Ai1ProviderDO provider = validateProviderExists(providerId);
        if (CommonStatusEnum.isDisable(provider.getStatus())) {
            throw exception(PROVIDER_DISABLE, provider.getName());
        }
        // 1.2 校验模型存在、开启，且归属于该 Provider
        Ai1ModelDO model = modelService.validateModelExists(modelId);
        if (ObjUtil.notEqual(model.getProviderId(), providerId)) {
            throw exception(MODEL_NOT_BELONG_PROVIDER);
        }
        if (CommonStatusEnum.isDisable(model.getStatus())) {
            throw exception(MODEL_DISABLE, model.getName());
        }

        // 2. 构建运行时快照
        return new Ai1ProviderRuntime().setProviderId(provider.getId()).setProviderName(provider.getName())
                .setModelId(model.getId()).setModel(model.getModel()).setModelType(model.getType())
                .setBaseUrl(resolve(provider.getBaseUrl())).setApiKey(resolve(provider.getApiKey()))
                .setHeaders(Ai1ProviderTool.parseHeaders(resolve(provider.getHeaders())));
    }

    // TODO @AI：不用愁方法；
    /**
     * 校验附属 Header：非空时必须是 [{"key":"...","value":"..."}] 格式的 JSON 数组
     */
    /**
     * 调用前解析 ${ENV} 占位符；库中的原文不改，响应也不返回解析结果
     */
    private String resolve(String value) {
        return Ai1ConfigPlaceholders.resolve(environment, value);
    }

    // TODO @AI：reqvo validator 下是不是就行了噢。
    private void validateHeaders(String headers) {
        if (StrUtil.isNotBlank(headers) && Ai1ProviderTool.parseHeaders(headers) == null) {
            throw exception(PROVIDER_HEADERS_INVALID);
        }
    }

}
