package cn.iocoder.yudao.module.ai1.service.model;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelImportReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.model.Ai1ModelMapper;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.harness.model.Ai1ProviderTool;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeBaseService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link Ai1ModelServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1ModelServiceImpl.class)
public class Ai1ModelServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1ModelServiceImpl modelService;

    @Resource
    private Ai1ModelMapper modelMapper;

    @MockitoBean
    private Ai1ProviderService providerService;
    @MockitoBean
    private Ai1AgentService agentService;
    @MockitoBean
    private Ai1KnowledgeBaseService knowledgeBaseService;
    @MockitoBean
    private Ai1LlmModelFactory llmModelFactory;
    @MockitoBean
    private Ai1ProviderTool providerTool;

    @Test
    public void testCreateModel_duplicate() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        // 准备参数
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setProviderId(dbModel.getProviderId()).setModel(dbModel.getModel()));

        // 调用，并断言异常
        assertServiceException(() -> modelService.createModel(reqVO), MODEL_DUPLICATE, dbModel.getModel());
    }

    @Test
    public void testCreateModel_sameModelInOtherProvider() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        // 准备参数
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setProviderId(dbModel.getProviderId() + 1).setModel(dbModel.getModel()));

        // 调用
        Long id = modelService.createModel(reqVO);
        // 断言
        assertEquals(reqVO.getModel(), modelMapper.selectById(id).getModel());
    }

    @Test
    public void testUpdateModel_evictCache() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        // 准备参数
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setId(dbModel.getId()).setProviderId(dbModel.getProviderId()));

        // 调用
        modelService.updateModel(reqVO);
        // 断言
        assertEquals(reqVO.getModel(), modelMapper.selectById(dbModel.getId()).getModel());
        assertEquals(dbModel.getProviderId(), modelMapper.selectById(dbModel.getId()).getProviderId());
        verify(llmModelFactory).evictByModelId(dbModel.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void testUpdateModel_providerCannotChange(int type) {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO(o -> o.setType(type));
        modelMapper.insert(dbModel);
        // 准备参数：同类型模型改挂到其他供应商，并同时修改名称与标识
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setId(dbModel.getId())
                .setProviderId(dbModel.getProviderId() + 1).setType(type)
                .setName("新名称").setModel("new-model"));

        // 调用，并断言异常
        assertServiceException(() -> modelService.updateModel(reqVO), MODEL_NOT_BELONG_PROVIDER);
        // 断言归属和其他字段均未修改，也未校验新供应商或清理缓存
        Ai1ModelDO model = modelMapper.selectById(dbModel.getId());
        assertEquals(dbModel.getProviderId(), model.getProviderId());
        assertEquals(dbModel.getName(), model.getName());
        assertEquals(dbModel.getModel(), model.getModel());
        assertEquals(dbModel.getType(), model.getType());
        verifyNoInteractions(providerService, agentService, knowledgeBaseService, llmModelFactory);
    }

    @Test
    public void testDeleteModel_usedByAgent() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        // mock agentService 的方法
        when(agentService.getAgentCountByModelIds(anyList())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> modelService.deleteModel(dbModel.getId()), MODEL_USED_BY_AGENT);
        // 断言
        assertNotNull(modelMapper.selectById(dbModel.getId()));
    }

    @Test
    public void testDeleteModel_usedByKnowledgeBase() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        // mock knowledgeBaseService 的方法
        when(knowledgeBaseService.getKnowledgeBaseCountByEmbeddingModelIds(Collections.singletonList(dbModel.getId())))
                .thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> modelService.deleteModel(dbModel.getId()), MODEL_USED_BY_KNOWLEDGE_BASE);
        // 断言
        assertNotNull(modelMapper.selectById(dbModel.getId()));
        verifyNoInteractions(llmModelFactory);
    }

    @Test
    public void testDeleteModelListByIds_usedByKnowledgeBase() {
        // mock 数据
        Ai1ModelDO first = randomModelDO();
        Ai1ModelDO second = randomModelDO();
        modelMapper.insert(first);
        modelMapper.insert(second);
        // 准备参数
        List<Long> ids = Arrays.asList(first.getId(), second.getId());
        when(knowledgeBaseService.getKnowledgeBaseCountByEmbeddingModelIds(ids)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> modelService.deleteModelListByIds(ids), MODEL_USED_BY_KNOWLEDGE_BASE);
        // 断言整批未删除或清理缓存
        assertNotNull(modelMapper.selectById(first.getId()));
        assertNotNull(modelMapper.selectById(second.getId()));
        verifyNoInteractions(llmModelFactory);
    }

    @Test
    public void testDeleteModelListByIds_success() {
        // mock 数据
        Ai1ModelDO first = randomModelDO();
        Ai1ModelDO second = randomModelDO();
        modelMapper.insert(first);
        modelMapper.insert(second);
        List<Long> ids = Arrays.asList(first.getId(), second.getId());

        // 调用
        modelService.deleteModelListByIds(ids);
        // 断言
        assertNull(modelMapper.selectById(first.getId()));
        assertNull(modelMapper.selectById(second.getId()));
        verify(knowledgeBaseService).getKnowledgeBaseCountByEmbeddingModelIds(ids);
        verify(llmModelFactory).evictByModelId(first.getId());
        verify(llmModelFactory).evictByModelId(second.getId());
    }

    @Test
    public void testUpdateModel_typeUsedByKnowledgeBase() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO(o -> o.setType(Ai1ModelTypeEnum.EMBEDDING.getType()));
        modelMapper.insert(dbModel);
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setId(dbModel.getId())
                .setProviderId(dbModel.getProviderId()).setType(Ai1ModelTypeEnum.CHAT.getType()));
        when(knowledgeBaseService.getKnowledgeBaseCountByEmbeddingModelIds(Collections.singletonList(dbModel.getId())))
                .thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> modelService.updateModel(reqVO), MODEL_TYPE_USED_BY_KNOWLEDGE_BASE);
        // 断言
        assertEquals(Ai1ModelTypeEnum.EMBEDDING.getType(), modelMapper.selectById(dbModel.getId()).getType());
        verifyNoInteractions(llmModelFactory);
    }

    @Test
    public void testUpdateModel_typeUsedByAgent() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO(o -> o.setType(Ai1ModelTypeEnum.CHAT.getType()));
        modelMapper.insert(dbModel);
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setId(dbModel.getId()).setProviderId(dbModel.getProviderId()));
        when(agentService.getAgentCountByModelIds(Collections.singletonList(dbModel.getId()))).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> modelService.updateModel(reqVO), MODEL_TYPE_USED_BY_AGENT);
        // 断言
        assertEquals(Ai1ModelTypeEnum.CHAT.getType(), modelMapper.selectById(dbModel.getId()).getType());
        verifyNoInteractions(llmModelFactory);
    }

    @Test
    public void testUpdateModel_sameTypeDoesNotCheckReferences() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setId(dbModel.getId()).setProviderId(dbModel.getProviderId()).setType(dbModel.getType()));

        // 调用
        modelService.updateModel(reqVO);
        // 断言改名等操作不查询引用
        assertEquals(reqVO.getName(), modelMapper.selectById(dbModel.getId()).getName());
        verifyNoInteractions(agentService, knowledgeBaseService);
        verify(llmModelFactory).evictByModelId(dbModel.getId());
    }

    @Test
    public void testUpdateModel_unusedTypeCanChange() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO(o -> o.setType(Ai1ModelTypeEnum.EMBEDDING.getType()));
        modelMapper.insert(dbModel);
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setId(dbModel.getId())
                .setProviderId(dbModel.getProviderId()).setType(Ai1ModelTypeEnum.CHAT.getType()));

        // 调用
        modelService.updateModel(reqVO);
        // 断言
        assertEquals(Ai1ModelTypeEnum.CHAT.getType(), modelMapper.selectById(dbModel.getId()).getType());
        verify(llmModelFactory).evictByModelId(dbModel.getId());
    }

    @Test
    public void testImportRemoteModelList_skipExisting() {
        // mock 数据
        Long providerId = randomLongId();
        modelMapper.insert(randomModelDO(o -> o.setProviderId(providerId).setModel("deepseek-chat")));
        // 准备参数
        Ai1ModelImportReqVO reqVO = new Ai1ModelImportReqVO().setProviderId(providerId)
                .setModels(Arrays.asList("deepseek-chat", "deepseek-reasoner", " deepseek-reasoner ", " "));

        // 调用
        Integer count = modelService.importRemoteModelList(reqVO);
        // 断言
        assertEquals(1, count);
        List<Ai1ModelDO> models = modelMapper.selectListByProviderId(providerId);
        assertEquals(Arrays.asList("deepseek-chat", "deepseek-reasoner"), convertList(models, Ai1ModelDO::getModel));
        Ai1ModelDO imported = models.get(1);
        assertEquals(Ai1ModelTypeEnum.CHAT.getType(), imported.getType());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), imported.getStatus());
    }

    @Test
    public void testImportRemoteModelList_allExists() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        // 准备参数
        Ai1ModelImportReqVO reqVO = new Ai1ModelImportReqVO().setProviderId(dbModel.getProviderId())
                .setModels(Arrays.asList(dbModel.getModel()));

        // 调用，并断言异常
        assertServiceException(() -> modelService.importRemoteModelList(reqVO), MODEL_IMPORT_ALL_EXISTS);
    }

    @Test
    public void testGetRemoteModelList_success() {
        // mock providerService 和 providerTool 的方法
        Long providerId = randomLongId();
        Ai1ProviderDO provider = randomPojo(Ai1ProviderDO.class, o -> o.setId(providerId).setHeaders(null));
        when(providerService.validateProviderExists(providerId)).thenReturn(provider);
        List<String> remoteModels = Arrays.asList("deepseek-chat", "deepseek-reasoner");
        when(providerTool.listModels(provider.getBaseUrl(), provider.getApiKey(), null)).thenReturn(remoteModels);

        // 调用
        List<String> models = modelService.getRemoteModelList(providerId);
        // 断言
        assertEquals(remoteModels, models);
    }

    @Test
    public void testGetModelRespBO_providerDisable() {
        // mock 方法
        Ai1ProviderDO provider = randomPojo(Ai1ProviderDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        when(providerService.validateProviderExists(provider.getId())).thenReturn(provider);

        // 调用，并断言异常
        assertServiceException(() -> modelService.getModelRespBO(provider.getId(), randomLongId()),
                PROVIDER_DISABLE, provider.getName());
    }

    @Test
    public void testGetModelRespBO_modelNotBelong() {
        // mock 方法
        Ai1ProviderDO provider = randomPojo(Ai1ProviderDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(providerService.validateProviderExists(provider.getId())).thenReturn(provider);
        // mock 数据
        Ai1ModelDO model = randomModelDO(o -> o.setProviderId(provider.getId() + 1));
        modelMapper.insert(model);

        // 调用，并断言异常
        assertServiceException(() -> modelService.getModelRespBO(provider.getId(), model.getId()), MODEL_NOT_BELONG_PROVIDER);
    }

    @Test
    public void testGetModelRespBO_success() {
        // mock 方法
        Ai1ProviderDO provider = randomPojo(Ai1ProviderDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setHeaders(Map.of("X-App", "yudao")));
        when(providerService.validateProviderExists(provider.getId())).thenReturn(provider);
        // mock 数据
        Ai1ModelDO model = randomModelDO(o -> o.setProviderId(provider.getId()));
        modelMapper.insert(model);

        // 调用
        Ai1ModelRespBO respBO = modelService.getModelRespBO(provider.getId(), model.getId());
        // 断言
        assertEquals(provider.getBaseUrl(), respBO.getBaseUrl());
        assertEquals(model.getModel(), respBO.getModel());
        assertEquals(model.getType(), respBO.getModelType());
        assertEquals(Map.of("X-App", "yudao"), respBO.getHeaders());
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static Ai1ModelDO randomModelDO(Consumer<Ai1ModelDO>... consumers) {
        Consumer<Ai1ModelDO> consumer = o -> o.setId(null).setName(StrUtil.maxLength(o.getName(), 10))
                .setType(Ai1ModelTypeEnum.CHAT.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus());
        return randomPojo(Ai1ModelDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static Ai1ModelSaveReqVO randomModelSaveReqVO(Consumer<Ai1ModelSaveReqVO>... consumers) {
        Consumer<Ai1ModelSaveReqVO> consumer = o -> o.setId(null).setName(StrUtil.maxLength(o.getName(), 10))
                .setType(Ai1ModelTypeEnum.EMBEDDING.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus());
        return randomPojo(Ai1ModelSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

}
