package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_KEY_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_KEY_VALID;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link BpmModelServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class BpmModelServiceImplTest extends BaseMockitoUnitTest {

    private static final Long TENANT_ID = 1L;
    private static final String MODEL_ID = "model-id";

    @InjectMocks
    private BpmModelServiceImpl modelService;

    @Mock
    private RepositoryService repositoryService;
    @Mock
    private ModelQuery modelQuery;
    @Mock
    private BpmProcessDefinitionService processDefinitionService;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.setTenantId(TENANT_ID);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testExportModel_bpmn() {
        // 准备参数
        Model model = mockModel(BpmModelTypeEnum.BPMN.getType());
        String bpmnXml = "<definitions />";
        // mock 方法（repositoryService）
        mockGetModel(model);
        when(repositoryService.getModelEditorSource(eq(MODEL_ID))).thenReturn(StrUtil.utf8Bytes(bpmnXml));

        // 调用
        BpmModelSaveReqVO result = modelService.exportModel(MODEL_ID);

        // 断言
        assertEquals(model.getKey(), result.getKey());
        assertEquals(model.getName(), result.getName());
        assertEquals(model.getCategory(), result.getCategory());
        assertEquals(BpmModelTypeEnum.BPMN.getType(), result.getType());
        assertEquals(bpmnXml, result.getBpmnXml());
        assertNull(result.getSimpleModel());
    }

    @Test
    public void testExportModel_simple() {
        // 准备参数
        Model model = mockModel(BpmModelTypeEnum.SIMPLE.getType());
        BpmSimpleModelNodeVO simpleModel = new BpmSimpleModelNodeVO();
        simpleModel.setId("start");
        simpleModel.setType(BpmSimpleModelNodeTypeEnum.START_NODE.getType());
        // mock 方法（repositoryService）
        mockGetModel(model);
        when(repositoryService.getModelEditorSourceExtra(eq(MODEL_ID)))
                .thenReturn(JsonUtils.toJsonByte(simpleModel));

        // 调用
        BpmModelSaveReqVO result = modelService.exportModel(MODEL_ID);

        // 断言
        assertEquals(BpmModelTypeEnum.SIMPLE.getType(), result.getType());
        assertNotNull(result.getSimpleModel());
        assertEquals(simpleModel.getId(), result.getSimpleModel().getId());
        assertEquals(simpleModel.getType(), result.getSimpleModel().getType());
        assertNull(result.getBpmnXml());
    }

    @Test
    public void testExportModel_notExists() {
        // mock 方法（repositoryService）
        when(repositoryService.createModelQuery()).thenReturn(modelQuery);
        when(modelQuery.modelId(eq(MODEL_ID))).thenReturn(modelQuery);
        when(modelQuery.modelTenantId(eq(TENANT_ID.toString()))).thenReturn(modelQuery);
        when(modelQuery.singleResult()).thenReturn(null);

        // 调用，并断言异常
        assertServiceException(() -> modelService.exportModel(MODEL_ID), MODEL_NOT_EXISTS);
    }

    @Test
    public void testImportModel_bpmn() {
        // 准备参数
        BpmModelSaveReqVO reqVO = new BpmModelSaveReqVO();
        reqVO.setKey("test_process");
        reqVO.setName("测试流程");
        reqVO.setCategory("OA");
        reqVO.setType(BpmModelTypeEnum.BPMN.getType());
        reqVO.setBpmnXml("<definitions />");
        reqVO.setStartUserIds(Arrays.asList(10L, 20L));
        reqVO.setStartDeptIds(Collections.singletonList(30L));
        reqVO.setManagerUserIds(Collections.singletonList(40L));
        Model model = mock(Model.class);
        when(model.getId()).thenReturn(MODEL_ID);
        // mock 方法（repositoryService）
        when(repositoryService.createModelQuery()).thenReturn(modelQuery);
        when(modelQuery.modelTenantId(eq(TENANT_ID.toString()))).thenReturn(modelQuery);
        when(modelQuery.modelKey(eq(reqVO.getKey()))).thenReturn(modelQuery);
        when(modelQuery.singleResult()).thenReturn(null);
        when(repositoryService.newModel()).thenReturn(model);

        // 调用
        String result = modelService.importModel(reqVO);

        // 断言
        assertEquals(MODEL_ID, result);
        verify(model).setTenantId(TENANT_ID.toString());
        verify(repositoryService).saveModel(same(model));
        verify(repositoryService).addModelEditorSource(eq(MODEL_ID),
                aryEq(StrUtil.utf8Bytes(reqVO.getBpmnXml())));
        ArgumentCaptor<String> metaInfoCaptor = ArgumentCaptor.forClass(String.class);
        verify(model).setMetaInfo(metaInfoCaptor.capture());
        BpmModelMetaInfoVO metaInfo = JsonUtils.parseObject(metaInfoCaptor.getValue(), BpmModelMetaInfoVO.class);
        assertNotNull(metaInfo);
        assertEquals(Arrays.asList(10L, 20L), metaInfo.getStartUserIds());
        assertEquals(Collections.singletonList(30L), metaInfo.getStartDeptIds());
        assertEquals(Collections.singletonList(40L), metaInfo.getManagerUserIds());
    }

    @Test
    public void testCreateModel_simple() {
        // 准备参数
        BpmSimpleModelNodeVO simpleModel = simpleModel();
        BpmModelSaveReqVO reqVO = new BpmModelSaveReqVO();
        reqVO.setKey("simple_process");
        reqVO.setName("简单流程");
        reqVO.setCategory("OA");
        reqVO.setType(BpmModelTypeEnum.SIMPLE.getType());
        reqVO.setSimpleModel(simpleModel);
        Model model = mock(Model.class);
        when(model.getId()).thenReturn(MODEL_ID);
        when(model.getKey()).thenReturn(reqVO.getKey());
        when(model.getName()).thenReturn(reqVO.getName());
        mockModelKeyNotExists(reqVO.getKey());
        when(repositoryService.newModel()).thenReturn(model);

        // 调用
        String result = modelService.createModel(reqVO);

        // 断言
        assertEquals(MODEL_ID, result);
        verify(repositoryService).saveModel(same(model));
        verify(repositoryService).addModelEditorSource(eq(MODEL_ID), any(byte[].class));
        verify(repositoryService).addModelEditorSourceExtra(eq(MODEL_ID), aryEq(JsonUtils.toJsonByte(simpleModel)));
    }

    @Test
    public void testCreateModel_invalidKey() {
        // 准备参数
        BpmModelSaveReqVO reqVO = new BpmModelSaveReqVO().setKey("bad key");

        // 调用，并断言异常
        assertServiceException(() -> modelService.createModel(reqVO), MODEL_KEY_VALID);
        verifyNoInteractions(repositoryService);
    }

    @Test
    public void testCreateModel_duplicateKey() {
        // 准备参数
        BpmModelSaveReqVO reqVO = new BpmModelSaveReqVO().setKey("duplicate_process");
        mockModelKeyExists(reqVO.getKey());

        // 调用，并断言异常
        assertServiceException(() -> modelService.createModel(reqVO), MODEL_KEY_EXISTS, reqVO.getKey());
        verify(repositoryService, never()).newModel();
    }

    @Test
    public void testGetModelList_nameFilter() {
        // 准备参数
        Model model = mock(Model.class);
        when(repositoryService.createModelQuery()).thenReturn(modelQuery);
        when(modelQuery.modelNameLike(eq("%请假%"))).thenReturn(modelQuery);
        when(modelQuery.modelTenantId(eq(TENANT_ID.toString()))).thenReturn(modelQuery);
        when(modelQuery.list()).thenReturn(Collections.singletonList(model));

        // 调用
        List<Model> result = modelService.getModelList("请假");

        // 断言
        assertEquals(Collections.singletonList(model), result);
        verify(modelQuery).modelNameLike(eq("%请假%"));
    }

    @Test
    public void testGetSimpleModel_success() {
        // 准备参数
        Model model = mock(Model.class);
        BpmSimpleModelNodeVO simpleModel = simpleModel();
        when(model.getId()).thenReturn(MODEL_ID);
        mockGetModel(model);
        when(repositoryService.getModelEditorSourceExtra(eq(MODEL_ID))).thenReturn(JsonUtils.toJsonByte(simpleModel));

        // 调用
        BpmSimpleModelNodeVO result = modelService.getSimpleModel(MODEL_ID);

        // 断言
        assertEquals(simpleModel.getId(), result.getId());
        assertEquals(simpleModel.getType(), result.getType());
    }

    @Test
    public void testUpdateSimpleModel_success() {
        // 准备参数
        Model model = mock(Model.class);
        when(model.getId()).thenReturn(MODEL_ID);
        when(model.getKey()).thenReturn("simple_process");
        when(model.getName()).thenReturn("简单流程");
        when(model.getCreateTime()).thenReturn(new Date(1_000L));
        BpmModelMetaInfoVO metaInfo = new BpmModelMetaInfoVO();
        metaInfo.setType(BpmModelTypeEnum.SIMPLE.getType());
        metaInfo.setManagerUserIds(Collections.singletonList(10L));
        when(model.getMetaInfo()).thenReturn(JsonUtils.toJsonString(metaInfo));
        BpmSimpleModelNodeVO simpleModel = simpleModel();
        BpmSimpleModelUpdateReqVO reqVO = new BpmSimpleModelUpdateReqVO().setId(MODEL_ID).setSimpleModel(simpleModel);
        mockGetModel(model);

        // 调用
        modelService.updateSimpleModel(10L, reqVO);

        // 断言
        verify(repositoryService).addModelEditorSource(eq(MODEL_ID), any(byte[].class));
        verify(repositoryService).addModelEditorSourceExtra(eq(MODEL_ID), aryEq(JsonUtils.toJsonByte(simpleModel)));
    }

    @Test
    public void testUpdateModelSortBatch_success() {
        // 准备参数
        List<String> ids = Arrays.asList("model-1", "model-2", "model-3");
        Model model1 = mockSortableModel("model-1");
        Model model2 = mockSortableModel("model-2");
        Model model3 = mockSortableModel("model-3");
        ModelQuery listQuery = mock(ModelQuery.class);
        ModelQuery managerQuery = mock(ModelQuery.class);
        when(repositoryService.createModelQuery()).thenReturn(listQuery, managerQuery, managerQuery, managerQuery);
        when(listQuery.modelTenantId(eq(TENANT_ID.toString()))).thenReturn(listQuery);
        when(listQuery.list()).thenReturn(Arrays.asList(model1, model2, model3));
        when(managerQuery.modelId(anyString())).thenReturn(managerQuery);
        when(managerQuery.modelTenantId(eq(TENANT_ID.toString()))).thenReturn(managerQuery);
        when(managerQuery.singleResult()).thenReturn(model1, model2, model3);

        // 调用
        modelService.updateModelSortBatch(1L, ids);

        // 断言：所有模型（包括第一个）都更新排序，且按 ids 顺序递增（列表按 sort 升序展示）
        verify(repositoryService, times(3)).saveModel(any(Model.class));
        ArgumentCaptor<String> modelIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sortCaptor = ArgumentCaptor.forClass(Long.class);
        verify(processDefinitionService, times(3))
                .updateProcessDefinitionSortByModelId(modelIdCaptor.capture(), sortCaptor.capture());
        assertEquals(Arrays.asList("model-3", "model-2", "model-1"), modelIdCaptor.getAllValues());
        List<Long> sorts = sortCaptor.getAllValues();
        assertTrue(sorts.get(0) > sorts.get(1) && sorts.get(1) > sorts.get(2));
    }

    private void mockModelKeyNotExists(String key) {
        when(repositoryService.createModelQuery()).thenReturn(modelQuery);
        when(modelQuery.modelTenantId(eq(TENANT_ID.toString()))).thenReturn(modelQuery);
        when(modelQuery.modelKey(eq(key))).thenReturn(modelQuery);
        when(modelQuery.singleResult()).thenReturn(null);
    }

    private void mockModelKeyExists(String key) {
        when(repositoryService.createModelQuery()).thenReturn(modelQuery);
        when(modelQuery.modelTenantId(eq(TENANT_ID.toString()))).thenReturn(modelQuery);
        when(modelQuery.modelKey(eq(key))).thenReturn(modelQuery);
        when(modelQuery.singleResult()).thenReturn(mock(Model.class));
    }

    private BpmSimpleModelNodeVO simpleModel() {
        return new BpmSimpleModelNodeVO().setId("start-user")
                .setType(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType())
                .setChildNode(new BpmSimpleModelNodeVO().setId("end")
                        .setType(BpmSimpleModelNodeTypeEnum.END_NODE.getType()));
    }

    private Model mockModel(Integer type) {
        Model model = mock(Model.class);
        when(model.getKey()).thenReturn("test_process");
        when(model.getName()).thenReturn("测试流程");
        when(model.getCategory()).thenReturn("OA");
        when(model.getCreateTime()).thenReturn(new Date(1_000L));
        BpmModelMetaInfoVO metaInfo = new BpmModelMetaInfoVO();
        metaInfo.setType(type);
        when(model.getMetaInfo()).thenReturn(JsonUtils.toJsonString(metaInfo));
        return model;
    }

    private Model mockSortableModel(String id) {
        Model model = mock(Model.class);
        when(model.getId()).thenReturn(id);
        when(model.getCreateTime()).thenReturn(new Date(1_000L));
        BpmModelMetaInfoVO metaInfo = new BpmModelMetaInfoVO();
        metaInfo.setManagerUserIds(Collections.singletonList(1L));
        when(model.getMetaInfo()).thenReturn(JsonUtils.toJsonString(metaInfo));
        return model;
    }

    private void mockGetModel(Model model) {
        when(repositoryService.createModelQuery()).thenReturn(modelQuery);
        when(modelQuery.modelId(eq(MODEL_ID))).thenReturn(modelQuery);
        when(modelQuery.modelTenantId(eq(TENANT_ID.toString()))).thenReturn(modelQuery);
        when(modelQuery.singleResult()).thenReturn(model);
    }

}
