package cn.iocoder.yudao.module.ai1.service.provider;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model.Ai1ModelImportReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.model.Ai1ModelSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.provider.Ai1ModelMapper;
import cn.iocoder.yudao.module.ai1.enums.provider.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.core.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
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
    private Ai1LlmModelFactory llmModelFactory;

    @Test
    public void testCreateModel_duplicate() {
        // mock 数据：同一 Provider 下已存在相同模型标识
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        // 准备参数
        Ai1ModelSaveReqVO reqVO = randomModelSaveReqVO(o -> o.setProviderId(dbModel.getProviderId()).setModel(dbModel.getModel()));

        // 调用，并断言异常
        assertServiceException(() -> modelService.createModel(reqVO), MODEL_DUPLICATE, dbModel.getModel());
    }

    @Test
    public void testCreateModel_sameModelInOtherProvider() {
        // mock 数据：其他 Provider 下存在相同模型标识，不冲突
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
        verify(llmModelFactory).evictByModelId(dbModel.getId());
    }

    @Test
    public void testDeleteModel_usedByAgent() {
        // mock 数据
        Ai1ModelDO dbModel = randomModelDO();
        modelMapper.insert(dbModel);
        // mock 方法：被 Agent 使用
        when(agentService.getAgentCountByModelIds(anyList())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> modelService.deleteModel(dbModel.getId()), MODEL_USED_BY_AGENT);
        assertNotNull(modelMapper.selectById(dbModel.getId()));
    }

    @Test
    public void testImportRemoteModelList_skipExisting() {
        // mock 数据：已存在 deepseek-chat
        Long providerId = randomLongId();
        modelMapper.insert(randomModelDO(o -> o.setProviderId(providerId).setModel("deepseek-chat")));
        // 准备参数：包含已存在、重复、空白项
        Ai1ModelImportReqVO reqVO = new Ai1ModelImportReqVO().setProviderId(providerId)
                .setModels(Arrays.asList("deepseek-chat", "deepseek-reasoner", " deepseek-reasoner ", " "));

        // 调用
        Integer count = modelService.importRemoteModelList(reqVO);

        // 断言：只导入 deepseek-reasoner，默认对话类型、开启状态
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
    public void testGetRemoteModelList_loadFail() {
        // mock 方法：供应商接口地址不可达
        Long providerId = randomLongId();
        Ai1ProviderDO provider = randomPojo(Ai1ProviderDO.class, o -> o.setId(providerId)
                .setBaseUrl("http://127.0.0.1:1/v1").setApiKey(null).setHeaders(null));
        when(providerService.validateProviderExists(providerId)).thenReturn(provider);

        // 调用，并断言异常
        assertServiceException(() -> modelService.getRemoteModelList(providerId), PROVIDER_REMOTE_MODEL_LOAD_FAIL);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static Ai1ModelDO randomModelDO(Consumer<Ai1ModelDO>... consumers) {
        Consumer<Ai1ModelDO> consumer = o -> o.setId(null).setName(o.getName().substring(0, 10))
                .setType(Ai1ModelTypeEnum.CHAT.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus());
        return randomPojo(Ai1ModelDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static Ai1ModelSaveReqVO randomModelSaveReqVO(Consumer<Ai1ModelSaveReqVO>... consumers) {
        Consumer<Ai1ModelSaveReqVO> consumer = o -> o.setId(null).setName(o.getName().substring(0, 10))
                .setType(Ai1ModelTypeEnum.EMBEDDING.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus());
        return randomPojo(Ai1ModelSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

}
