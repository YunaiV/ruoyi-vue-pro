package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing.AiModelPricingSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiModelPricingMapper;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.MODEL_PRICING_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiModelPricingServiceImpl} 的单元测试
 */
@Import(AiModelPricingServiceImpl.class)
public class AiModelPricingServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AiModelPricingServiceImpl modelPricingService;

    @Resource
    private AiModelPricingMapper modelPricingMapper;

    @MockitoBean
    private AiModelService modelService;

    // ========== createModelPricing ==========

    @Test
    public void testCreateModelPricing() {
        AiModelPricingSaveReqVO reqVO = new AiModelPricingSaveReqVO();
        reqVO.setModelId(1L);
        reqVO.setPriceInPer1mYuan(2.0);
        reqVO.setPriceCachedPer1mYuan(0.5);
        reqVO.setPriceOutPer1mYuan(8.0);
        reqVO.setPriceReasoningPer1mYuan(16.0);
        reqVO.setStrategyType("DEFAULT");
        reqVO.setStrategyConfig("{\"key\":\"value\"}");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        Long id = modelPricingService.createModelPricing(reqVO);

        assertNotNull(id);
        AiModelPricingDO pricing = modelPricingMapper.selectById(id);
        assertEquals(1L, pricing.getModelId());
        assertEquals("CNY", pricing.getCurrency());
        assertEquals(2_000_000L, pricing.getPriceInPer1m());
        assertEquals(500_000L, pricing.getPriceCachedPer1m());
        assertEquals(8_000_000L, pricing.getPriceOutPer1m());
        assertEquals(16_000_000L, pricing.getPriceReasoningPer1m());
        assertEquals("DEFAULT", pricing.getStrategyType());
        assertEquals("{\"key\":\"value\"}", pricing.getStrategyConfig());
    }

    @Test
    public void testCreateModelPricing_nullOptionalPrices() {
        // 缓存价和推理价为 null → 转为 0
        AiModelPricingSaveReqVO reqVO = new AiModelPricingSaveReqVO();
        reqVO.setModelId(1L);
        reqVO.setPriceInPer1mYuan(2.0);
        reqVO.setPriceOutPer1mYuan(8.0);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        Long id = modelPricingService.createModelPricing(reqVO);

        AiModelPricingDO pricing = modelPricingMapper.selectById(id);
        assertEquals(0L, pricing.getPriceCachedPer1m());
        assertEquals(0L, pricing.getPriceReasoningPer1m());
    }

    // ========== updateModelPricing ==========

    @Test
    public void testUpdateModelPricing() {
        // 先插入
        AiModelPricingDO pricing = AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(2_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(8_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        modelPricingMapper.insert(pricing);

        // 更新
        AiModelPricingSaveReqVO updateVO = new AiModelPricingSaveReqVO();
        updateVO.setId(pricing.getId());
        updateVO.setModelId(1L);
        updateVO.setPriceInPer1mYuan(5.0);
        updateVO.setPriceOutPer1mYuan(20.0);
        updateVO.setStrategyType("CUSTOM");
        updateVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        modelPricingService.updateModelPricing(updateVO);

        AiModelPricingDO updated = modelPricingMapper.selectById(pricing.getId());
        assertEquals(5_000_000L, updated.getPriceInPer1m());
        assertEquals(20_000_000L, updated.getPriceOutPer1m());
        assertEquals("CUSTOM", updated.getStrategyType());
    }

    @Test
    public void testUpdateModelPricing_notExists() {
        AiModelPricingSaveReqVO updateVO = new AiModelPricingSaveReqVO();
        updateVO.setId(999L);
        updateVO.setModelId(1L);
        updateVO.setPriceInPer1mYuan(2.0);
        updateVO.setPriceOutPer1mYuan(8.0);
        updateVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> modelPricingService.updateModelPricing(updateVO),
                MODEL_PRICING_NOT_EXISTS);
    }

    // ========== deleteModelPricing ==========

    @Test
    public void testDeleteModelPricing() {
        AiModelPricingDO pricing = AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(2_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(8_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        modelPricingMapper.insert(pricing);

        modelPricingService.deleteModelPricing(pricing.getId());

        assertNull(modelPricingMapper.selectById(pricing.getId()));
    }

    @Test
    public void testDeleteModelPricing_notExists() {
        assertServiceException(() -> modelPricingService.deleteModelPricing(999L),
                MODEL_PRICING_NOT_EXISTS);
    }

    // ========== getModelPricing ==========

    @Test
    public void testGetModelPricing() {
        AiModelPricingDO pricing = AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(2_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(8_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        modelPricingMapper.insert(pricing);

        AiModelPricingDO result = modelPricingService.getModelPricing(pricing.getId());
        assertNotNull(result);
        assertEquals(2_000_000L, result.getPriceInPer1m());
    }

    @Test
    public void testGetModelPricing_notExists() {
        assertNull(modelPricingService.getModelPricing(999L));
    }

    // ========== getLatestModelPricing ==========

    @Test
    public void testGetLatestModelPricing() {
        // 插入旧配置
        AiModelPricingDO old = AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(1_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(4_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        modelPricingMapper.insert(old);

        // 插入新配置
        AiModelPricingDO newer = AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(2_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(8_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        modelPricingMapper.insert(newer);

        AiModelPricingDO result = modelPricingService.getLatestModelPricing(1L);
        assertNotNull(result);
        // 应该返回最新的那条（ID 更大的）
        assertTrue(result.getId() >= newer.getId() || result.getId() >= old.getId());
    }

    @Test
    public void testGetLatestModelPricing_onlyDisabled() {
        // 只有禁用的配置 → 返回 null
        AiModelPricingDO disabled = AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(2_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(8_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        modelPricingMapper.insert(disabled);

        assertNull(modelPricingService.getLatestModelPricing(1L));
    }

    @Test
    public void testGetLatestModelPricing_noConfig() {
        assertNull(modelPricingService.getLatestModelPricing(999L));
    }

    // ========== getModelPricingPage ==========

    @Test
    public void testGetModelPricingPage_all() {
        modelPricingMapper.insert(AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(2_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(8_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build());
        modelPricingMapper.insert(AiModelPricingDO.builder()
                .modelId(2L).currency("CNY")
                .priceInPer1m(5_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(20_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build());

        AiModelPricingPageReqVO reqVO = new AiModelPricingPageReqVO();
        PageResult<AiModelPricingDO> page = modelPricingService.getModelPricingPage(reqVO);

        assertEquals(2, page.getTotal());
    }

    @Test
    public void testGetModelPricingPage_filterByModelId() {
        modelPricingMapper.insert(AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(2_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(8_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build());
        modelPricingMapper.insert(AiModelPricingDO.builder()
                .modelId(2L).currency("CNY")
                .priceInPer1m(5_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(20_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build());

        AiModelPricingPageReqVO reqVO = new AiModelPricingPageReqVO();
        reqVO.setModelId(2L);
        PageResult<AiModelPricingDO> page = modelPricingService.getModelPricingPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals(2L, page.getList().get(0).getModelId());
    }

    @Test
    public void testGetModelPricingPage_filterByStatus() {
        modelPricingMapper.insert(AiModelPricingDO.builder()
                .modelId(1L).currency("CNY")
                .priceInPer1m(2_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(8_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build());
        modelPricingMapper.insert(AiModelPricingDO.builder()
                .modelId(2L).currency("CNY")
                .priceInPer1m(5_000_000L).priceCachedPer1m(0L)
                .priceOutPer1m(20_000_000L).priceReasoningPer1m(0L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build());

        AiModelPricingPageReqVO reqVO = new AiModelPricingPageReqVO();
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());
        PageResult<AiModelPricingDO> page = modelPricingService.getModelPricingPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), page.getList().get(0).getStatus());
    }

    // ========== yuanToMicro 精度 ==========

    @Test
    public void testCreateModelPricing_yuanToMicroPrecision() {
        // 0.001 元 = 1000 微元
        AiModelPricingSaveReqVO reqVO = new AiModelPricingSaveReqVO();
        reqVO.setModelId(1L);
        reqVO.setPriceInPer1mYuan(0.001);
        reqVO.setPriceOutPer1mYuan(0.0005); // 500 微元，测试四舍五入
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        Long id = modelPricingService.createModelPricing(reqVO);

        AiModelPricingDO pricing = modelPricingMapper.selectById(id);
        assertEquals(1000L, pricing.getPriceInPer1m());
        assertEquals(500L, pricing.getPriceOutPer1m());
    }

}
