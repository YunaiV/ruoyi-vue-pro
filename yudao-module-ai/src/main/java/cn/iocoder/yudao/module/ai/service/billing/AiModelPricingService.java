package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import jakarta.validation.Valid;

/**
 * AI 模型计费配置 Service 接口
 *
 * @author 芋道源码
 */
public interface AiModelPricingService {

    /**
     * 创建模型计费配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createModelPricing(@Valid AiModelPricingSaveReqVO createReqVO);

    /**
     * 更新模型计费配置
     *
     * @param updateReqVO 更新信息
     */
    void updateModelPricing(@Valid AiModelPricingSaveReqVO updateReqVO);

    /**
     * 删除模型计费配置
     *
     * @param id 编号
     */
    void deleteModelPricing(Long id);

    /**
     * 获得模型计费配置
     *
     * @param id 编号
     * @return 计费配置
     */
    AiModelPricingDO getModelPricing(Long id);

    /**
     * 获得模型计费配置分页
     *
     * @param pageReqVO 分页查询
     * @return 计费配置分页
     */
    PageResult<AiModelPricingDO> getModelPricingPage(AiModelPricingPageReqVO pageReqVO);

    /**
     * 获得指定模型最新的启用计费配置
     *
     * @param modelId 模型编号
     * @return 计费配置，不存在返回 null
     */
    AiModelPricingDO getLatestModelPricing(Long modelId);

}
