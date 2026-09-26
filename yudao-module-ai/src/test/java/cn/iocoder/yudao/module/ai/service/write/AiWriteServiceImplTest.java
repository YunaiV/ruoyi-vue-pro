package cn.iocoder.yudao.module.ai.service.write;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.biz.system.dict.DictDataCommonApi;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.dal.mysql.write.AiWriteMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.billing.AiTokenSourceEnum;
import cn.iocoder.yudao.module.ai.enums.write.AiWriteTypeEnum;
import cn.iocoder.yudao.module.ai.service.billing.AiBudgetChecker;
import cn.iocoder.yudao.module.ai.service.billing.AiModelCallLogService;
import cn.iocoder.yudao.module.ai.service.billing.AiModelPricingService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiWriteServiceImpl} 的单元测试
 */
public class AiWriteServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AiWriteServiceImpl writeService;

    @Mock
    private AiModelService modalService;
    @Mock
    private AiChatRoleService chatRoleService;
    @Mock
    private AiWriteMapper writeMapper;
    @Mock
    private AiModelCallLogService callLogService;
    @Mock
    private AiBudgetChecker budgetChecker;
    @Mock
    private AiModelPricingService modelPricingService;
    @Mock
    private DictDataCommonApi dictDataApi;

    @Mock
    private ChatModel chatModel;

    private AiModelDO model;
    private AiBudgetChecker.PreDeductResult preDeductResult;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(1L);
        DictFrameworkUtils.init(dictDataApi);
        DictFrameworkUtils.clearCache();
        model = AiModelDO.builder()
                .id(1L)
                .model("gpt-4o-mini")
                .platform(AiPlatformEnum.OPENAI.getPlatform())
                .type(AiModelTypeEnum.CHAT.getType())
                .temperature(0.7)
                .maxTokens(1024)
                .build();
        LocalDateTime now = LocalDateTime.now();
        preDeductResult = new AiBudgetChecker.PreDeductResult(1L, 2L, 100L, now, now);

        lenient().when(chatRoleService.getChatRoleListByName(anyString())).thenReturn(Collections.emptyList());
        lenient().when(modalService.getRequiredDefaultModel(eq(AiModelTypeEnum.CHAT.getType()))).thenReturn(model);
        lenient().when(modalService.getChatModel(eq(1L))).thenReturn(chatModel);
        lenient().when(budgetChecker.preDeduct(eq(1L), eq(2L), anyLong())).thenReturn(preDeductResult);
        lenient().when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.never());
        lenient().when(dictDataApi.getDictDataList(anyString())).thenReturn(Collections.emptyList());
        lenient().doAnswer(invocation -> {
            AiWriteDO writeDO = invocation.getArgument(0);
            writeDO.setId(99L);
            return 1;
        }).when(writeMapper).insert(any(AiWriteDO.class));
    }

    @AfterEach
    void tearDown() {
        DictFrameworkUtils.clearCache();
        TenantContextHolder.clear();
    }

    @Test
    public void testGenerateWriteContent_cancelShouldSettlePreDeduct() {
        AiWriteGenerateReqVO reqVO = new AiWriteGenerateReqVO();
        reqVO.setType(AiWriteTypeEnum.WRITING.getType());
        reqVO.setPrompt("测试取消");
        reqVO.setLength(1);
        reqVO.setFormat(1);
        reqVO.setTone(1);
        reqVO.setLanguage(1);

        Flux<CommonResult<String>> flux = writeService.generateWriteContent(reqVO, 2L);
        Disposable disposable = flux.subscribe();
        disposable.dispose();

        verify(callLogService, timeout(1000).times(1)).createCallLog(any());
        verify(budgetChecker, timeout(1000).times(1)).settle(eq(preDeductResult), eq(0L));
        verify(budgetChecker, never()).release(eq(preDeductResult));
    }

    @Test
    public void testGenerateWriteContent_errorPathShouldSettleWhenUpdateFails() {
        AiWriteGenerateReqVO reqVO = new AiWriteGenerateReqVO();
        reqVO.setType(AiWriteTypeEnum.WRITING.getType());
        reqVO.setPrompt("测试异常");
        reqVO.setLength(1);
        reqVO.setFormat(1);
        reqVO.setTone(1);
        reqVO.setLanguage(1);

        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.error(new RuntimeException("stream error")));
        doThrow(new RuntimeException("db error")).when(writeMapper).updateById(any(AiWriteDO.class));

        writeService.generateWriteContent(reqVO, 2L).subscribe();

        verify(callLogService, timeout(1000).times(1)).createCallLog(any());
        verify(budgetChecker, timeout(1000).times(1)).settle(eq(preDeductResult), eq(0L));
        verify(budgetChecker, never()).release(eq(preDeductResult));
    }

    @Test
    public void testGenerateWriteContent_successWithoutUsageShouldUseEstimatedCost() {
        AiWriteGenerateReqVO reqVO = new AiWriteGenerateReqVO();
        reqVO.setType(AiWriteTypeEnum.WRITING.getType());
        reqVO.setPrompt("测试成功");
        reqVO.setLength(1);
        reqVO.setFormat(1);
        reqVO.setTone(1);
        reqVO.setLanguage(1);

        ChatResponse chunk = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(chunk.getResult().getOutput().getText()).thenReturn("ok");
        when(chunk.getMetadata()).thenReturn(null);
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(chunk));

        writeService.generateWriteContent(reqVO, 2L).collectList().block();

        org.mockito.ArgumentCaptor<AiModelCallLogDO> captor =
                org.mockito.ArgumentCaptor.forClass(AiModelCallLogDO.class);
        verify(callLogService, times(1)).createCallLog(captor.capture());
        AiModelCallLogDO callLog = captor.getValue();
        assertEquals(AiTokenSourceEnum.ESTIMATED.getSource(), callLog.getTokenSource());
        assertEquals(preDeductResult.amount(), callLog.getCostAmount());
        verify(budgetChecker, times(1)).settle(eq(preDeductResult), eq(preDeductResult.amount()));
    }
}
