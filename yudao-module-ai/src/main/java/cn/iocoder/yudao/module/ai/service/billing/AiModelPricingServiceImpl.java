package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiModelPricingMapper;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.MODEL_PRICING_NOT_EXISTS;

/**
 * AI 模型计费配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AiModelPricingServiceImpl implements AiModelPricingService {

    @Resource
    private AiModelPricingMapper modelPricingMapper;

    @Resource
    private AiModelService modelService;

    @Override
    public Long createModelPricing(AiModelPricingSaveReqVO createReqVO) {
        // 1. 校验模型存在
        modelService.validateModel(createReqVO.getModelId());

        // 2. 元转微元，插入
        AiModelPricingDO pricing = convertToDO(createReqVO);
        pricing.setCurrency("CNY"); // 首期固定 CNY
        modelPricingMapper.insert(pricing);
        return pricing.getId();
    }

    @Override
    public void updateModelPricing(AiModelPricingSaveReqVO updateReqVO) {
        // 1. 校验存在
        validateModelPricingExists(updateReqVO.getId());
        modelService.validateModel(updateReqVO.getModelId());

        // 2. 元转微元，更新
        AiModelPricingDO updateObj = convertToDO(updateReqVO);
        modelPricingMapper.updateById(updateObj);
    }

    @Override
    public void deleteModelPricing(Long id) {
        // 校验存在
        validateModelPricingExists(id);
        // 删除
        modelPricingMapper.deleteById(id);
    }

    private void validateModelPricingExists(Long id) {
        if (modelPricingMapper.selectById(id) == null) {
            throw exception(MODEL_PRICING_NOT_EXISTS);
        }
    }

    @Override
    public AiModelPricingDO getModelPricing(Long id) {
        return modelPricingMapper.selectById(id);
    }

    @Override
    public PageResult<AiModelPricingDO> getModelPricingPage(AiModelPricingPageReqVO pageReqVO) {
        return modelPricingMapper.selectPage(pageReqVO);
    }

    @Override
    public AiModelPricingDO getLatestModelPricing(Long modelId) {
        return modelPricingMapper.selectLatestByModelId(modelId);
    }

    // ========== 内部方法 ==========

    /**
     * 将 VO 转换为 DO，元转微元
     */
    private AiModelPricingDO convertToDO(AiModelPricingSaveReqVO reqVO) {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setId(reqVO.getId());
        pricing.setModelId(reqVO.getModelId());
        pricing.setStatus(reqVO.getStatus());
        pricing.setStrategyType(reqVO.getStrategyType());
        pricing.setStrategyConfig(reqVO.getStrategyConfig());
        // 元转微元：元 * 1,000,000，四舍五入
        pricing.setPriceInPer1m(yuanToMicro(reqVO.getPriceInPer1mYuan()));
        pricing.setPriceCachedPer1m(yuanToMicro(reqVO.getPriceCachedPer1mYuan()));
        pricing.setPriceOutPer1m(yuanToMicro(reqVO.getPriceOutPer1mYuan()));
        pricing.setPriceReasoningPer1m(yuanToMicro(reqVO.getPriceReasoningPer1mYuan()));
        return pricing;
    }

    /**
     * 元转微元（1元 = 1,000,000微元）
     *
     * @param yuan 元，可为 null
     * @return 微元，null 时返回 0
     */
    private Long yuanToMicro(Double yuan) {
        if (yuan == null) {
            return 0L;
        }
        return Math.round(yuan * 1_000_000);
    }

}
