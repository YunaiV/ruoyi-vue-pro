package cn.iocoder.yudao.module.ai1.service.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderConnectRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.model.Ai1ProviderMapper;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.harness.model.Ai1ProviderTool;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.ai1.util.Ai1Utils.normalizeHeaders;
import static cn.iocoder.yudao.module.ai1.util.Ai1Utils.resolveSpringPlaceholders;

/**
 * AI1 供应商 Service 实现类
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
    private Ai1LlmModelFactory llmModelFactory;

    @Resource
    private Ai1ProviderTool providerTool;

    @Override
    public Long createProvider(Ai1ProviderSaveReqVO createReqVO) {
        Ai1ProviderDO provider = BeanUtils.toBean(createReqVO, Ai1ProviderDO.class);
        provider.setHeaders(normalizeHeaders(createReqVO.getHeaders()));
        providerMapper.insert(provider);
        return provider.getId();
    }

    @Override
    public void updateProvider(Ai1ProviderSaveReqVO updateReqVO) {
        // 1. 校验存在
        validateProviderExists(updateReqVO.getId());

        // 2. 更新：API 密钥为空时置为 null，由 updateById 跳过该字段，保持原密钥
        Ai1ProviderDO updateObj = BeanUtils.toBean(updateReqVO, Ai1ProviderDO.class);
        updateObj.setHeaders(normalizeHeaders(updateReqVO.getHeaders()));
        if (StrUtil.isBlank(updateObj.getApiKey())) {
            updateObj.setApiKey(null);
        }
        providerMapper.updateById(updateObj);

        // 3. 地址、密钥、Header、状态可能变化，失效本节点已缓存的模型实例
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

        // 2. 删除
        providerMapper.deleteByIds(ids);

        // 3. 失效本节点已缓存的模型实例
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

    @Override
    public Ai1ProviderConnectRespVO testProviderConnect(Long id) {
        // 1. 校验存在
        Ai1ProviderDO provider = validateProviderExists(id);

        // 2. 连通测试：调用前解析 ${ENV} 占位符，库中的原文不改
        return providerTool.testConnect(resolveSpringPlaceholders(provider.getBaseUrl()),
                resolveSpringPlaceholders(provider.getApiKey()), resolveSpringPlaceholders(provider.getHeaders()));
    }

}
