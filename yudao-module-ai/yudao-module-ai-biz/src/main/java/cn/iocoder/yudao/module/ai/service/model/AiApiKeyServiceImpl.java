package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.factory.AiClientFactory;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.apikey.AiApiKeyPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.apikey.AiApiKeySaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.mysql.model.AiApiKeyMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.image.ImageClient;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI API 密钥 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AiApiKeyServiceImpl implements AiApiKeyService {

    @Resource
    private AiApiKeyMapper apiKeyMapper;

    @Resource
    private AiClientFactory clientFactory;

    @Override
    public Long createApiKey(AiApiKeySaveReqVO createReqVO) {
        // 插入
        AiApiKeyDO apiKey = BeanUtils.toBean(createReqVO, AiApiKeyDO.class);
        apiKeyMapper.insert(apiKey);
        // 返回
        return apiKey.getId();
    }

    @Override
    public void updateApiKey(AiApiKeySaveReqVO updateReqVO) {
        // 校验存在
        validateApiKeyExists(updateReqVO.getId());
        // 更新
        AiApiKeyDO updateObj = BeanUtils.toBean(updateReqVO, AiApiKeyDO.class);
        apiKeyMapper.updateById(updateObj);
    }

    @Override
    public void deleteApiKey(Long id) {
        // 校验存在
        validateApiKeyExists(id);
        // 删除
        apiKeyMapper.deleteById(id);
    }

    private AiApiKeyDO validateApiKeyExists(Long id) {
        AiApiKeyDO apiKey = apiKeyMapper.selectById(id);
        if (apiKey == null) {
            throw exception(API_KEY_NOT_EXISTS);
        }
        return apiKey;
    }

    @Override
    public AiApiKeyDO getApiKey(Long id) {
        return apiKeyMapper.selectById(id);
    }

    @Override
    public AiApiKeyDO validateApiKey(Long id) {
        AiApiKeyDO apiKey = validateApiKeyExists(id);
        if (CommonStatusEnum.isDisable(apiKey.getStatus())) {
            throw exception(API_KEY_DISABLE);
        }
        return apiKey;
    }

    @Override
    public PageResult<AiApiKeyDO> getApiKeyPage(AiApiKeyPageReqVO pageReqVO) {
        return apiKeyMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AiApiKeyDO> getApiKeyList() {
        return apiKeyMapper.selectList();
    }

    // ========== 与 spring-ai 集成 ==========

    @Override
    public StreamingChatClient getStreamingChatClient(Long id) {
        AiApiKeyDO apiKey = validateApiKey(id);
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(apiKey.getPlatform());
        return clientFactory.getOrCreateStreamingChatClient(platform, apiKey.getApiKey(), apiKey.getUrl());
    }

    @Override
    public ImageClient getImageClient(AiPlatformEnum platform) {
        AiApiKeyDO apiKey = apiKeyMapper.selectFirstByPlatformAndStatus(platform.getName(), CommonStatusEnum.ENABLE.getStatus());
        if (apiKey == null) {
            return null;
        }
        return clientFactory.getOrCreateImageClient(platform, apiKey.getApiKey(), apiKey.getUrl());
    }

    @Override
    public MidjourneyApi getMidjourneyApi() {
        AiApiKeyDO apiKey = apiKeyMapper.selectFirstByPlatformAndStatus(
                AiPlatformEnum.MIDJOURNEY.getPlatform(), CommonStatusEnum.ENABLE.getStatus());
        if (apiKey == null) {
            return null;
        }
        return clientFactory.getOrCreateMidjourneyApi(apiKey.getApiKey(), apiKey.getUrl());
    }

    @Override
    public SunoApi getSunoApi() {
        AiApiKeyDO apiKey = apiKeyMapper.selectFirstByPlatformAndStatus(
                AiPlatformEnum.SUNO.getPlatform(), CommonStatusEnum.ENABLE.getStatus());
        if (apiKey == null) {
            return null;
        }
        return clientFactory.getOrCreateSunoApi(apiKey.getApiKey(), apiKey.getUrl());
    }

}