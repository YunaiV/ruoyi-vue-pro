package cn.iocoder.yudao.module.ai1.service.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderConnectRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.model.Ai1ProviderMapper;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.harness.model.Ai1ProviderTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1ProviderServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1ProviderServiceImpl.class)
public class Ai1ProviderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1ProviderServiceImpl providerService;

    @Resource
    private Ai1ProviderMapper providerMapper;

    @MockitoBean
    private Ai1ModelService modelService;
    @MockitoBean
    private Ai1LlmModelFactory llmModelFactory;
    @MockitoBean
    private Ai1ProviderTool providerTool;

    @Test
    public void testCreateProvider_success() {
        // 准备参数
        Ai1ProviderSaveReqVO reqVO = randomProviderSaveReqVO(o -> o.setHeaders(Map.of("X-Session", "{session}")));

        // 调用
        Long id = providerService.createProvider(reqVO);
        // 断言
        Ai1ProviderDO provider = providerMapper.selectById(id);
        assertEquals(reqVO.getName(), provider.getName());
        assertEquals(reqVO.getApiKey(), provider.getApiKey());
    }

    @Test
    public void testUpdateProvider_blankApiKeyKeepsOriginal() {
        // mock 数据
        Ai1ProviderDO dbProvider = randomProviderDO();
        providerMapper.insert(dbProvider);
        // 准备参数
        Ai1ProviderSaveReqVO reqVO = randomProviderSaveReqVO(o -> o.setId(dbProvider.getId()).setApiKey(""));

        // 调用
        providerService.updateProvider(reqVO);
        // 断言
        Ai1ProviderDO provider = providerMapper.selectById(dbProvider.getId());
        assertEquals(dbProvider.getApiKey(), provider.getApiKey());
        assertEquals(reqVO.getBaseUrl(), provider.getBaseUrl());
        verify(llmModelFactory).evictByProviderId(dbProvider.getId());
    }

    @Test
    public void testDeleteProvider_hasModel() {
        // mock 数据
        Ai1ProviderDO dbProvider = randomProviderDO();
        providerMapper.insert(dbProvider);
        // mock modelService 的方法
        when(modelService.getModelCountByProviderIds(anyList())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> providerService.deleteProvider(dbProvider.getId()), PROVIDER_HAS_MODEL);
        // 断言
        assertNotNull(providerMapper.selectById(dbProvider.getId()));
    }

    @Test
    public void testDeleteProvider_success() {
        // mock 数据
        Ai1ProviderDO dbProvider = randomProviderDO();
        providerMapper.insert(dbProvider);
        // mock modelService 的方法
        when(modelService.getModelCountByProviderIds(anyList())).thenReturn(0L);

        // 调用
        providerService.deleteProvider(dbProvider.getId());
        // 断言
        assertNull(providerMapper.selectById(dbProvider.getId()));
        verify(llmModelFactory).evictByProviderId(dbProvider.getId());
    }

    @Test
    public void testTestProviderConnect_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> providerService.testProviderConnect(randomLongId()), PROVIDER_NOT_EXISTS);
    }

    @Test
    public void testTestProviderConnect_success() {
        // mock 数据
        Ai1ProviderDO dbProvider = randomProviderDO(o -> o.setHeaders(Map.of("X-App", "yudao")));
        providerMapper.insert(dbProvider);
        // mock 方法
        Ai1ProviderConnectRespVO result = randomPojo(Ai1ProviderConnectRespVO.class);
        when(providerTool.testConnect(eq(dbProvider.getBaseUrl()), eq(dbProvider.getApiKey()),
                eq(Map.of("X-App", "yudao"))))
                .thenReturn(result);

        // 调用
        Ai1ProviderConnectRespVO connectResult = providerService.testProviderConnect(dbProvider.getId());
        // 断言
        assertSame(result, connectResult);
    }

    @Test
    public void testCreateAndUpdateProvider_headers() {
        // 准备参数
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(" X-Z ", "${HEADER_TOKEN}");
        headers.put("X-A", "{session}");
        Ai1ProviderSaveReqVO req = randomProviderSaveReqVO(o -> o.setHeaders(headers));

        // 调用
        Long id = providerService.createProvider(req);
        // 断言
        Ai1ProviderDO created = providerMapper.selectById(id);
        assertEquals(List.of("X-Z", "X-A"), List.copyOf(created.getHeaders().keySet()));
        assertEquals("${HEADER_TOKEN}", created.getHeaders().get("X-Z"));
        assertEquals("{session}", created.getHeaders().get("X-A"));

        // 准备参数
        req.setId(id).setHeaders(null);
        // 调用
        providerService.updateProvider(req);
        // 断言
        assertEquals(created.getHeaders(), providerMapper.selectById(id).getHeaders());

        // 准备参数
        req.setHeaders(Map.of("X-New", "new"));
        // 调用
        providerService.updateProvider(req);
        // 断言
        assertEquals(req.getHeaders(), providerMapper.selectById(id).getHeaders());

        // 准备参数
        req.setHeaders(Map.of());
        // 调用
        providerService.updateProvider(req);
        // 断言
        assertEquals(Map.of(), providerMapper.selectById(id).getHeaders());
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static Ai1ProviderDO randomProviderDO(Consumer<Ai1ProviderDO>... consumers) {
        Consumer<Ai1ProviderDO> consumer = o -> o.setId(null).setHeaders(null).setStatus(CommonStatusEnum.ENABLE.getStatus());
        return randomPojo(Ai1ProviderDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static Ai1ProviderSaveReqVO randomProviderSaveReqVO(Consumer<Ai1ProviderSaveReqVO>... consumers) {
        Consumer<Ai1ProviderSaveReqVO> consumer = o -> o.setId(null).setHeaders(null).setStatus(CommonStatusEnum.ENABLE.getStatus());
        return randomPojo(Ai1ProviderSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

}
