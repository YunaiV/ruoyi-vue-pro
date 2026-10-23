package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogStatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogStatRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import cn.iocoder.yudao.module.ai.dal.mysql.billing.AiModelCallLogMapper;
import cn.iocoder.yudao.module.ai.enums.billing.AiCallStatusEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiTokenSourceEnum;
import cn.iocoder.yudao.module.ai.service.billing.pricing.AiPricingStrategyManager;
import cn.iocoder.yudao.module.ai.service.billing.pricing.DefaultPricingStrategy;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link AiModelCallLogServiceImpl} 的单元测试
 */
@Import({AiModelCallLogServiceImpl.class, AiPricingStrategyManager.class, DefaultPricingStrategy.class})
public class AiModelCallLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AiModelCallLogServiceImpl callLogService;

    @Resource
    private AiModelCallLogMapper callLogMapper;

    @MockitoBean
    private AiModelPricingService modelPricingService;

    @Test
    public void testCreateCallLog_withPricing() {
        // 准备计费配置
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceReasoningPer1m(0L);
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(pricing);

        // 准备调用日志
        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L)
                .platform("DEEP_SEEK")
                .modelId(100L)
                .model("deepseek-chat")
                .bizType("CHAT_MESSAGE")
                .bizId(200L)
                .requestTime(LocalDateTime.now())
                .responseTime(LocalDateTime.now())
                .durationMs(1500)
                .status(AiCallStatusEnum.SUCCESS.getStatus())
                .promptTokens(1000)
                .completionTokens(500)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource())
                .build();

        // 调用
        Long id = callLogService.createCallLog(callLog);

        // 校验
        assertNotNull(id);
        AiModelCallLogDO dbLog = callLogMapper.selectById(id);
        assertNotNull(dbLog);
        // 验证价格快照
        assertEquals(2_000_000L, dbLog.getPriceInPer1m());
        assertEquals(8_000_000L, dbLog.getPriceOutPer1m());
        // 验证费用计算: 1000 * 2 + 500 * 8 = 6000 微元
        assertEquals(6000L, dbLog.getCostAmount());
        assertEquals("CNY", dbLog.getCurrency());
        assertFalse(dbLog.getBlocked());
    }

    @Test
    public void testCreateCallLog_noPricing() {
        // 无计费配置
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(null);

        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L)
                .platform("OPENAI")
                .modelId(100L)
                .model("gpt-4o")
                .bizType("CHAT_MESSAGE")
                .bizId(200L)
                .requestTime(LocalDateTime.now())
                .responseTime(LocalDateTime.now())
                .durationMs(800)
                .status(AiCallStatusEnum.SUCCESS.getStatus())
                .promptTokens(500)
                .completionTokens(200)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource())
                .build();

        Long id = callLogService.createCallLog(callLog);

        AiModelCallLogDO dbLog = callLogMapper.selectById(id);
        // 无计费配置时，单价和费用均为 0
        assertEquals(0L, dbLog.getPriceInPer1m());
        assertEquals(0L, dbLog.getPriceOutPer1m());
        assertEquals(0L, dbLog.getCostAmount());
    }

    @Test
    public void testCreateCallLog_failWithNoTokens() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceReasoningPer1m(0L);
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(pricing);

        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L)
                .platform("DEEP_SEEK")
                .modelId(100L)
                .model("deepseek-chat")
                .bizType("CHAT_MESSAGE")
                .bizId(200L)
                .requestTime(LocalDateTime.now())
                .responseTime(LocalDateTime.now())
                .durationMs(100)
                .status(AiCallStatusEnum.FAIL.getStatus())
                .errorMessage("Connection timeout")
                .tokenSource(AiTokenSourceEnum.NONE.getSource())
                .build();

        Long id = callLogService.createCallLog(callLog);

        AiModelCallLogDO dbLog = callLogMapper.selectById(id);
        // 失败且无 token 时费用为 0
        assertEquals(0L, dbLog.getCostAmount());
    }

    @Test
    public void testCreateCallLog_estimatedCostShouldBePreserved() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceReasoningPer1m(0L);
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(pricing);

        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L)
                .platform("DEEP_SEEK")
                .modelId(100L)
                .model("deepseek-chat")
                .bizType("WRITE")
                .bizId(201L)
                .requestTime(LocalDateTime.now())
                .responseTime(LocalDateTime.now())
                .durationMs(1200)
                .status(AiCallStatusEnum.SUCCESS.getStatus())
                .tokenSource(AiTokenSourceEnum.ESTIMATED.getSource())
                .costAmount(12345L)
                .build();

        Long id = callLogService.createCallLog(callLog);

        AiModelCallLogDO dbLog = callLogMapper.selectById(id);
        assertEquals(AiTokenSourceEnum.ESTIMATED.getSource(), dbLog.getTokenSource());
        assertEquals(12345L, dbLog.getCostAmount());
        assertEquals(2_000_000L, dbLog.getPriceInPer1m());
        assertEquals(8_000_000L, dbLog.getPriceOutPer1m());
    }

    @Test
    public void testGetCallLogStat_empty() {
        AiModelCallLogStatReqVO reqVO = new AiModelCallLogStatReqVO();
        AiModelCallLogStatRespVO stat = callLogService.getCallLogStat(reqVO);

        assertEquals(0L, stat.getTotalCount());
        assertEquals(0L, stat.getSuccessCount());
        assertEquals(0L, stat.getFailCount());
        assertEquals(0L, stat.getTotalPromptTokens());
        assertEquals(0L, stat.getTotalCostAmount());
    }

    @Test
    public void testGetCallLogStat_withData() {
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(null);

        // 插入两条日志
        AiModelCallLogDO log1 = AiModelCallLogDO.builder()
                .userId(1L).platform("DEEP_SEEK").modelId(100L).model("deepseek-chat")
                .bizType("CHAT_MESSAGE").bizId(1L)
                .requestTime(LocalDateTime.now()).responseTime(LocalDateTime.now())
                .durationMs(1000).status(AiCallStatusEnum.SUCCESS.getStatus())
                .promptTokens(500).completionTokens(200).totalTokens(700)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource())
                .build();
        callLogService.createCallLog(log1);

        AiModelCallLogDO log2 = AiModelCallLogDO.builder()
                .userId(1L).platform("DEEP_SEEK").modelId(100L).model("deepseek-chat")
                .bizType("CHAT_MESSAGE").bizId(2L)
                .requestTime(LocalDateTime.now()).responseTime(LocalDateTime.now())
                .durationMs(2000).status(AiCallStatusEnum.FAIL.getStatus())
                .errorMessage("error")
                .tokenSource(AiTokenSourceEnum.NONE.getSource())
                .build();
        callLogService.createCallLog(log2);

        AiModelCallLogStatReqVO reqVO = new AiModelCallLogStatReqVO();
        AiModelCallLogStatRespVO stat = callLogService.getCallLogStat(reqVO);

        assertEquals(2L, stat.getTotalCount());
        assertEquals(1L, stat.getSuccessCount());
        assertEquals(1L, stat.getFailCount());
        assertEquals(500L, stat.getTotalPromptTokens());
        assertEquals(200L, stat.getTotalCompletionTokens());
    }

    // ========== 四档计费 ==========

    @Test
    public void testCreateCallLog_fourTierPricing() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(10_000_000L);
        pricing.setPriceCachedPer1m(2_000_000L);
        pricing.setPriceOutPer1m(20_000_000L);
        pricing.setPriceReasoningPer1m(40_000_000L);
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(pricing);

        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L).platform("DEEP_SEEK").modelId(100L).model("deepseek-r1")
                .bizType("CHAT_MESSAGE").bizId(300L)
                .requestTime(LocalDateTime.now()).responseTime(LocalDateTime.now())
                .durationMs(2000).status(AiCallStatusEnum.SUCCESS.getStatus())
                .promptTokens(1000).completionTokens(800)
                .cachedTokens(300).reasoningTokens(200)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource())
                .build();

        Long id = callLogService.createCallLog(callLog);

        AiModelCallLogDO dbLog = callLogMapper.selectById(id);
        // 验证四档价格快照
        assertEquals(10_000_000L, dbLog.getPriceInPer1m());
        assertEquals(2_000_000L, dbLog.getPriceCachedPer1m());
        assertEquals(20_000_000L, dbLog.getPriceOutPer1m());
        assertEquals(40_000_000L, dbLog.getPriceReasoningPer1m());
        // 费用: (700*10 + 300*2 + 600*20 + 200*40) = 7000+600+12000+8000 = 27600
        assertEquals(27600L, dbLog.getCostAmount());
    }

    @Test
    public void testCreateCallLog_withStrategyType() {
        // 带 strategyType 的计费配置
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceReasoningPer1m(0L);
        pricing.setStrategyType("DEFAULT");
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(pricing);

        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L).platform("DEEP_SEEK").modelId(100L).model("deepseek-chat")
                .bizType("CHAT_MESSAGE").bizId(400L)
                .requestTime(LocalDateTime.now()).responseTime(LocalDateTime.now())
                .durationMs(1000).status(AiCallStatusEnum.SUCCESS.getStatus())
                .promptTokens(1000).completionTokens(500)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource())
                .build();

        Long id = callLogService.createCallLog(callLog);

        AiModelCallLogDO dbLog = callLogMapper.selectById(id);
        assertEquals(6000L, dbLog.getCostAmount());
    }

    @Test
    public void testCreateCallLog_nullModelId() {
        // modelId 为 null → 不查询计费配置，费用为 0
        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L).platform("OPENAI").model("gpt-4o")
                .bizType("CHAT_MESSAGE").bizId(500L)
                .requestTime(LocalDateTime.now()).responseTime(LocalDateTime.now())
                .durationMs(500).status(AiCallStatusEnum.SUCCESS.getStatus())
                .promptTokens(100).completionTokens(50)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource())
                .build();

        Long id = callLogService.createCallLog(callLog);

        AiModelCallLogDO dbLog = callLogMapper.selectById(id);
        assertEquals(0L, dbLog.getPriceInPer1m());
        assertEquals(0L, dbLog.getCostAmount());
    }

    @Test
    public void testCreateCallLog_failWithTokens() {
        // 失败但有 token（部分响应后失败）→ 应该计费
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceReasoningPer1m(0L);
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(pricing);

        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L).platform("DEEP_SEEK").modelId(100L).model("deepseek-chat")
                .bizType("CHAT_MESSAGE").bizId(600L)
                .requestTime(LocalDateTime.now()).responseTime(LocalDateTime.now())
                .durationMs(3000).status(AiCallStatusEnum.FAIL.getStatus())
                .errorMessage("Stream interrupted")
                .promptTokens(1000).completionTokens(200)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource())
                .build();

        Long id = callLogService.createCallLog(callLog);

        AiModelCallLogDO dbLog = callLogMapper.selectById(id);
        // 失败但有 token → 正常计费: 1000*2 + 200*8 = 3600
        assertEquals(3600L, dbLog.getCostAmount());
    }

    // ========== getCallLogPage ==========

    @Test
    public void testGetCallLogPage_all() {
        when(modelPricingService.getLatestModelPricing(anyLong())).thenReturn(null);

        insertCallLog(1L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(2L, "OPENAI", 200L, "WRITE", AiCallStatusEnum.FAIL.getStatus());
        insertCallLog(1L, "DEEP_SEEK", 100L, "MIND_MAP", AiCallStatusEnum.SUCCESS.getStatus());

        AiModelCallLogPageReqVO reqVO = new AiModelCallLogPageReqVO();
        PageResult<AiModelCallLogDO> page = callLogService.getCallLogPage(reqVO);

        assertEquals(3, page.getTotal());
    }

    @Test
    public void testGetCallLogPage_filterByUserId() {
        when(modelPricingService.getLatestModelPricing(anyLong())).thenReturn(null);

        insertCallLog(1L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(2L, "OPENAI", 200L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());

        AiModelCallLogPageReqVO reqVO = new AiModelCallLogPageReqVO();
        reqVO.setUserId(1L);
        PageResult<AiModelCallLogDO> page = callLogService.getCallLogPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals(1L, page.getList().get(0).getUserId());
    }

    @Test
    public void testGetCallLogPage_filterByPlatform() {
        when(modelPricingService.getLatestModelPricing(anyLong())).thenReturn(null);

        insertCallLog(1L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(1L, "OPENAI", 200L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());

        AiModelCallLogPageReqVO reqVO = new AiModelCallLogPageReqVO();
        reqVO.setPlatform("OPENAI");
        PageResult<AiModelCallLogDO> page = callLogService.getCallLogPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals("OPENAI", page.getList().get(0).getPlatform());
    }

    @Test
    public void testGetCallLogPage_filterByStatus() {
        when(modelPricingService.getLatestModelPricing(anyLong())).thenReturn(null);

        insertCallLog(1L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(1L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.FAIL.getStatus());

        AiModelCallLogPageReqVO reqVO = new AiModelCallLogPageReqVO();
        reqVO.setStatus(AiCallStatusEnum.FAIL.getStatus());
        PageResult<AiModelCallLogDO> page = callLogService.getCallLogPage(reqVO);

        assertEquals(1, page.getTotal());
        assertEquals(AiCallStatusEnum.FAIL.getStatus(), page.getList().get(0).getStatus());
    }

    // ========== getCallLogList ==========

    @Test
    public void testGetCallLogList_filterByBizType() {
        when(modelPricingService.getLatestModelPricing(anyLong())).thenReturn(null);

        insertCallLog(1L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(1L, "DEEP_SEEK", 100L, "WRITE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(1L, "DEEP_SEEK", 100L, "MIND_MAP", AiCallStatusEnum.SUCCESS.getStatus());

        AiModelCallLogPageReqVO reqVO = new AiModelCallLogPageReqVO();
        reqVO.setBizType("WRITE");
        java.util.List<AiModelCallLogDO> list = callLogService.getCallLogList(reqVO);

        assertEquals(1, list.size());
        assertEquals("WRITE", list.get(0).getBizType());
    }

    // ========== getCallLogStat 过滤 ==========

    @Test
    public void testGetCallLogStat_filterByUserId() {
        when(modelPricingService.getLatestModelPricing(anyLong())).thenReturn(null);

        insertCallLog(1L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(2L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(2L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.FAIL.getStatus());

        AiModelCallLogStatReqVO reqVO = new AiModelCallLogStatReqVO();
        reqVO.setUserId(2L);
        AiModelCallLogStatRespVO stat = callLogService.getCallLogStat(reqVO);

        assertEquals(2L, stat.getTotalCount());
        assertEquals(1L, stat.getSuccessCount());
        assertEquals(1L, stat.getFailCount());
    }

    @Test
    public void testGetCallLogStat_filterByBizType() {
        when(modelPricingService.getLatestModelPricing(anyLong())).thenReturn(null);

        insertCallLog(1L, "DEEP_SEEK", 100L, "CHAT_MESSAGE", AiCallStatusEnum.SUCCESS.getStatus());
        insertCallLog(1L, "DEEP_SEEK", 100L, "WRITE", AiCallStatusEnum.SUCCESS.getStatus());

        AiModelCallLogStatReqVO reqVO = new AiModelCallLogStatReqVO();
        reqVO.setBizType("CHAT_MESSAGE");
        AiModelCallLogStatRespVO stat = callLogService.getCallLogStat(reqVO);

        assertEquals(1L, stat.getTotalCount());
    }

    @Test
    public void testGetCallLogStat_costAmountYuan() {
        // 验证 totalCostAmountYuan 转换
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceReasoningPer1m(0L);
        when(modelPricingService.getLatestModelPricing(eq(100L))).thenReturn(pricing);

        AiModelCallLogDO callLog = AiModelCallLogDO.builder()
                .userId(1L).platform("DEEP_SEEK").modelId(100L).model("deepseek-chat")
                .bizType("CHAT_MESSAGE").bizId(700L)
                .requestTime(LocalDateTime.now()).responseTime(LocalDateTime.now())
                .durationMs(1000).status(AiCallStatusEnum.SUCCESS.getStatus())
                .promptTokens(1000).completionTokens(500)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource())
                .build();
        callLogService.createCallLog(callLog);

        AiModelCallLogStatReqVO reqVO = new AiModelCallLogStatReqVO();
        AiModelCallLogStatRespVO stat = callLogService.getCallLogStat(reqVO);

        assertEquals(6000L, stat.getTotalCostAmount());
        assertEquals(6000.0 / 1_000_000.0, stat.getTotalCostAmountYuan(), 0.0001);
    }

    // ========== 辅助方法 ==========

    private void insertCallLog(Long userId, String platform, Long modelId,
                               String bizType, String status) {
        AiModelCallLogDO.AiModelCallLogDOBuilder builder = AiModelCallLogDO.builder()
                .userId(userId).platform(platform).modelId(modelId).model("test-model")
                .bizType(bizType).bizId(System.nanoTime())
                .requestTime(LocalDateTime.now()).responseTime(LocalDateTime.now())
                .durationMs(1000).status(status)
                .tokenSource(AiTokenSourceEnum.PROVIDER.getSource());
        if (AiCallStatusEnum.SUCCESS.getStatus().equals(status)) {
            builder.promptTokens(100).completionTokens(50).totalTokens(150);
        } else {
            builder.errorMessage("test error")
                    .tokenSource(AiTokenSourceEnum.NONE.getSource());
        }
        callLogService.createCallLog(builder.build());
    }

}
