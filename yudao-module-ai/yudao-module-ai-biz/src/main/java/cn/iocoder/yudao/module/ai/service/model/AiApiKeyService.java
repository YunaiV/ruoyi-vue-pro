package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.apikey.AiApiKeyPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.apikey.AiApiKeySaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import jakarta.validation.Valid;

/**
 * AI API 密钥 Service 接口
 *
 * @author 芋道源码
 */
public interface AiApiKeyService {

    /**
     * 创建AI API 密钥
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createApiKey(@Valid AiApiKeySaveReqVO createReqVO);

    /**
     * 更新AI API 密钥
     *
     * @param updateReqVO 更新信息
     */
    void updateApiKey(@Valid AiApiKeySaveReqVO updateReqVO);

    /**
     * 删除AI API 密钥
     *
     * @param id 编号
     */
    void deleteApiKey(Long id);

    /**
     * 获得AI API 密钥
     *
     * @param id 编号
     * @return AI API 密钥
     */
    AiApiKeyDO getApiKey(Long id);

    /**
     * 获得AI API 密钥分页
     *
     * @param pageReqVO 分页查询
     * @return AI API 密钥分页
     */
    PageResult<AiApiKeyDO> getApiKeyPage(AiApiKeyPageReqVO pageReqVO);

}