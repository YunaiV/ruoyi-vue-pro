package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplatecategory.FmsVoucherTemplateCategorySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplate.FmsVoucherTemplateEntryVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplate.FmsVoucherTemplateSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherTemplateCategoryDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherTemplateDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsVoucherTemplateCategoryMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsVoucherTemplateMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import(FmsVoucherTemplateServiceImpl.class)
public class FmsVoucherTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsVoucherTemplateServiceImpl voucherTemplateService;
    @Resource
    private FmsVoucherTemplateCategoryMapper templateCategoryMapper;
    @Resource
    private FmsVoucherTemplateMapper templateMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsAuxiliaryItemService auxiliaryItemService;
    @MockBean
    private FmsAuxiliaryTypeService auxiliaryTypeService;

    @Test
    public void testCreateTemplateCategory_success() {
        // 准备参数
        FmsVoucherTemplateCategorySaveReqVO reqVO = buildCategorySaveReqVO(null, 1L, "日常收支");

        // 调用
        Long categoryId = voucherTemplateService.createTemplateCategory(reqVO, 10L);

        // 断言
        FmsVoucherTemplateCategoryDO category = templateCategoryMapper.selectById(categoryId);
        assertEquals("日常收支", category.getName());
        assertEquals(1L, category.getAccountSetId());
    }

    @Test
    public void testCreateTemplateCategory_nameDuplicate() {
        // mock 数据
        templateCategoryMapper.insert(buildCategory(1L, "日常收支"));
        // 准备参数
        FmsVoucherTemplateCategorySaveReqVO reqVO = buildCategorySaveReqVO(null, 1L, "日常收支");

        // 调用，并断言
        assertServiceException(() -> voucherTemplateService.createTemplateCategory(reqVO, 10L),
                VOUCHER_TEMPLATE_CATEGORY_NAME_DUPLICATE);
    }

    @Test
    public void testDeleteTemplateCategory_inUse() {
        // mock 数据
        FmsVoucherTemplateCategoryDO category = buildCategory(1L, "日常收支");
        templateCategoryMapper.insert(category);
        templateMapper.insert(new FmsVoucherTemplateDO().setAccountSetId(1L)
                .setCategoryId(category.getId()).setName("办公用品采购")
                .setEntries(Collections.emptyList()));

        // 调用，并断言
        assertServiceException(() -> voucherTemplateService.deleteTemplateCategory(
                1L, category.getId(), 10L), VOUCHER_TEMPLATE_CATEGORY_IN_USE);
    }

    @Test
    public void testCreateVoucherTemplate_jsonContent() {
        // mock 数据
        FmsVoucherTemplateCategoryDO category = buildCategory(1L, "日常收支");
        templateCategoryMapper.insert(category);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.singletonList(31L)),
                new FmsSubjectDO().setId(201L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList())));
        when(auxiliaryItemService.validateAuxiliaryItemList(eq(1L), anyCollection()))
                .thenReturn(Collections.singletonList(new FmsAuxiliaryItemDO().setId(41L)
                        .setAuxiliaryTypeId(31L).setName("上海客户")
                        .setStatus(CommonStatusEnum.ENABLE.getStatus())));
        when(auxiliaryTypeService.validateAuxiliaryTypeList(eq(1L), anyCollection()))
                .thenReturn(Collections.singletonList(new FmsAuxiliaryTypeDO().setId(31L).setType(1)));
        // 准备参数
        FmsVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(category.getId());
        FmsVoucherTemplateEntryVO.AuxiliaryItem auxiliary = new FmsVoucherTemplateEntryVO.AuxiliaryItem();
        auxiliary.setTypeId(31L);
        auxiliary.setItemId(41L);
        CollUtil.getFirst(reqVO.getEntries()).setAuxiliaries(Collections.singletonList(auxiliary));

        // 调用
        Long templateId = voucherTemplateService.createVoucherTemplate(reqVO, 10L);

        // 断言
        FmsVoucherTemplateDO template = templateMapper.selectById(templateId);
        assertEquals("办公用品采购", template.getName());
        assertEquals(2, template.getEntries().size());
        assertEquals(new BigDecimal("100.00"), CollUtil.getFirst(template.getEntries()).getDebitAmount());
        FmsVoucherEntryDO.AuxiliaryItem auxiliaryItem = CollUtil.getFirst(
                CollUtil.getFirst(template.getEntries()).getAuxiliaries());
        assertEquals(1, auxiliaryItem.getType());
        assertEquals(31L, auxiliaryItem.getTypeId());
        assertEquals(41L, auxiliaryItem.getItemId());
        assertEquals("上海客户", auxiliaryItem.getName());
    }

    @Test
    public void testCreateVoucherTemplate_negativeAmount() {
        // mock 数据
        FmsVoucherTemplateCategoryDO category = buildCategory(1L, "日常收支");
        templateCategoryMapper.insert(category);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList()),
                new FmsSubjectDO().setId(201L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList())));
        // 准备参数
        FmsVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(category.getId());
        reqVO.getEntries().get(0).setDebitAmount(new BigDecimal("-20.00"));
        reqVO.getEntries().get(1).setCreditAmount(new BigDecimal("-20.00"));

        // 调用
        Long templateId = voucherTemplateService.createVoucherTemplate(reqVO, 10L);

        // 断言
        FmsVoucherTemplateDO template = templateMapper.selectById(templateId);
        assertEquals(new BigDecimal("-20.00"), CollUtil.getFirst(template.getEntries()).getDebitAmount());
    }

    @Test
    public void testCreateVoucherTemplate_amountUnbalanced() {
        // mock 数据
        FmsVoucherTemplateCategoryDO category = buildCategory(1L, "日常收支");
        templateCategoryMapper.insert(category);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList()),
                new FmsSubjectDO().setId(201L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList())));
        // 准备参数
        FmsVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(category.getId());
        reqVO.getEntries().get(1).setCreditAmount(new BigDecimal("90.00"));

        // 调用，并断言
        assertServiceException(() -> voucherTemplateService.createVoucherTemplate(reqVO, 10L),
                VOUCHER_AMOUNT_UNBALANCED);
    }

    @Test
    public void testCreateVoucherTemplate_quantityAmountFloor() {
        // mock 数据
        FmsVoucherTemplateCategoryDO category = buildCategory(1L, "日常收支");
        templateCategoryMapper.insert(category);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setQuantityAccounting(true).setAuxiliaryTypeIds(Collections.emptyList()),
                new FmsSubjectDO().setId(201L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList())));
        // 准备参数
        FmsVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(category.getId());
        CollUtil.getFirst(reqVO.getEntries()).setDebitAmount(new BigDecimal("1.99"));
        CollUtil.getFirst(reqVO.getEntries()).setQuantity(new BigDecimal("1"));
        CollUtil.getFirst(reqVO.getEntries()).setUnitPrice(new BigDecimal("1.999"));
        reqVO.getEntries().get(1).setCreditAmount(new BigDecimal("1.99"));

        // 调用
        Long templateId = voucherTemplateService.createVoucherTemplate(reqVO, 10L);

        // 断言
        FmsVoucherTemplateDO template = templateMapper.selectById(templateId);
        assertEquals(new BigDecimal("1.99"), CollUtil.getFirst(template.getEntries()).getDebitAmount());
    }

    @Test
    public void testCreateVoucherTemplate_subjectDisabled() {
        // mock 数据
        FmsVoucherTemplateCategoryDO category = buildCategory(1L, "日常收支");
        templateCategoryMapper.insert(category);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setStatus(CommonStatusEnum.DISABLE.getStatus()),
                new FmsSubjectDO().setId(201L).setStatus(CommonStatusEnum.ENABLE.getStatus())));
        // 准备参数
        FmsVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(category.getId());

        // 调用，并断言
        assertServiceException(() -> voucherTemplateService.createVoucherTemplate(reqVO, 10L),
                VOUCHER_SUBJECT_DISABLED);
    }

    @Test
    public void testCreateVoucherTemplate_auxiliaryItemDisabled() {
        // mock 数据
        FmsVoucherTemplateCategoryDO category = buildCategory(1L, "日常收支");
        templateCategoryMapper.insert(category);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.singletonList(31L)),
                new FmsSubjectDO().setId(201L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList())));
        when(auxiliaryItemService.validateAuxiliaryItemList(eq(1L), anyCollection()))
                .thenReturn(Collections.singletonList(new FmsAuxiliaryItemDO().setId(41L)
                        .setAuxiliaryTypeId(31L).setName("上海客户")
                        .setStatus(CommonStatusEnum.DISABLE.getStatus())));
        when(auxiliaryTypeService.validateAuxiliaryTypeList(eq(1L), anyCollection()))
                .thenReturn(Collections.singletonList(new FmsAuxiliaryTypeDO().setId(31L).setType(1)));
        // 准备参数
        FmsVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(category.getId());
        FmsVoucherTemplateEntryVO.AuxiliaryItem auxiliary = new FmsVoucherTemplateEntryVO.AuxiliaryItem();
        auxiliary.setTypeId(31L);
        auxiliary.setItemId(41L);
        CollUtil.getFirst(reqVO.getEntries()).setAuxiliaries(Collections.singletonList(auxiliary));

        // 调用，并断言
        assertServiceException(() -> voucherTemplateService.createVoucherTemplate(reqVO, 10L),
                VOUCHER_AUXILIARY_REQUIRED);
    }

    @Test
    public void testUpdateVoucherTemplate_success() {
        // mock 数据
        FmsVoucherTemplateCategoryDO category = buildCategory(1L, "日常收支");
        templateCategoryMapper.insert(category);
        FmsVoucherTemplateDO template = new FmsVoucherTemplateDO().setAccountSetId(1L)
                .setCategoryId(category.getId()).setName("办公用品采购")
                .setEntries(Collections.emptyList());
        templateMapper.insert(template);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList()),
                new FmsSubjectDO().setId(201L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                        .setAuxiliaryTypeIds(Collections.emptyList())));
        // 准备参数
        FmsVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(category.getId());
        reqVO.setId(template.getId());
        reqVO.setName("办公用品采购（已修改）");

        // 调用
        voucherTemplateService.updateVoucherTemplate(reqVO, 10L);

        // 断言
        FmsVoucherTemplateDO dbTemplate = templateMapper.selectById(template.getId());
        assertEquals("办公用品采购（已修改）", dbTemplate.getName());
        assertEquals(category.getId(), dbTemplate.getCategoryId());
        assertEquals(2, dbTemplate.getEntries().size());
    }

    @Test
    public void testDeleteVoucherTemplate_success() {
        // mock 数据
        FmsVoucherTemplateDO template = new FmsVoucherTemplateDO().setAccountSetId(1L)
                .setCategoryId(11L).setName("办公用品采购").setEntries(Collections.emptyList());
        templateMapper.insert(template);

        // 调用
        voucherTemplateService.deleteVoucherTemplate(1L, template.getId(), 10L);

        // 断言
        assertNull(templateMapper.selectById(template.getId()));
    }

    @Test
    public void testDeleteVoucherTemplate_wrongAccountSet() {
        // mock 数据
        FmsVoucherTemplateDO template = new FmsVoucherTemplateDO().setAccountSetId(1L)
                .setCategoryId(11L).setName("办公用品采购").setEntries(Collections.emptyList());
        templateMapper.insert(template);

        // 调用，并断言
        assertServiceException(() -> voucherTemplateService.deleteVoucherTemplate(
                2L, template.getId(), 10L), VOUCHER_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testGetVoucherTemplateCountByAuxiliary() {
        // mock 数据
        FmsVoucherTemplateDO.Entry entry = new FmsVoucherTemplateDO.Entry().setAuxiliaries(
                Collections.singletonList(FmsVoucherEntryDO.AuxiliaryItem.builder()
                        .typeId(31L).itemId(41L).build()));
        templateMapper.insert(new FmsVoucherTemplateDO().setName("收款模板")
                .setCategoryId(1L).setAccountSetId(1L)
                .setEntries(Collections.singletonList(entry)));
        templateMapper.insert(new FmsVoucherTemplateDO().setName("其他账套模板")
                .setCategoryId(1L).setAccountSetId(2L)
                .setEntries(Collections.singletonList(entry)));

        // 调用，并断言
        assertEquals(1L, voucherTemplateService.getVoucherTemplateCountByAuxiliaryItemIds(
                1L, Collections.singletonList(41L)));
        assertEquals(1L, voucherTemplateService.getVoucherTemplateCountByAuxiliaryTypeId(1L, 31L));
        assertEquals(0L, voucherTemplateService.getVoucherTemplateCountByAuxiliaryItemIds(
                1L, Collections.singletonList(42L)));
        assertEquals(0L, voucherTemplateService.getVoucherTemplateCountByAuxiliaryItemIds(
                1L, Collections.emptyList()));
    }

    // ========== 随机对象 ==========

    private FmsVoucherTemplateCategorySaveReqVO buildCategorySaveReqVO(
            Long id, Long accountSetId, String name) {
        FmsVoucherTemplateCategorySaveReqVO reqVO = new FmsVoucherTemplateCategorySaveReqVO();
        reqVO.setId(id);
        reqVO.setAccountSetId(accountSetId);
        reqVO.setName(name);
        return reqVO;
    }

    private FmsVoucherTemplateCategoryDO buildCategory(Long accountSetId, String name) {
        return randomPojo(FmsVoucherTemplateCategoryDO.class, category -> category.setId(null)
                .setAccountSetId(accountSetId).setName(name));
    }

    private FmsVoucherTemplateSaveReqVO buildTemplateSaveReqVO(Long categoryId) {
        FmsVoucherTemplateEntryVO debitEntry = new FmsVoucherTemplateEntryVO();
        debitEntry.setDigest("购买办公用品");
        debitEntry.setSubjectId(101L);
        debitEntry.setDebitAmount(new BigDecimal("100.00"));
        debitEntry.setAuxiliaries(Collections.emptyList());
        FmsVoucherTemplateEntryVO creditEntry = new FmsVoucherTemplateEntryVO();
        creditEntry.setDigest("购买办公用品");
        creditEntry.setSubjectId(201L);
        creditEntry.setCreditAmount(new BigDecimal("100.00"));
        creditEntry.setAuxiliaries(Collections.emptyList());
        FmsVoucherTemplateSaveReqVO reqVO = new FmsVoucherTemplateSaveReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setCategoryId(categoryId);
        reqVO.setName("办公用品采购");
        reqVO.setEntries(Arrays.asList(debitEntry, creditEntry));
        return reqVO;
    }

}
