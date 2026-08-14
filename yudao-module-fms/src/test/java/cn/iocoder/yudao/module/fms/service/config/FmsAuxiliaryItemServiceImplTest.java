package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAuxiliaryItemMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsAuxiliaryTypeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherTemplateService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsAuxiliaryItemServiceImpl.class)
public class FmsAuxiliaryItemServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsAuxiliaryItemServiceImpl auxiliaryItemService;
    @Resource
    private FmsAuxiliaryItemMapper auxiliaryItemMapper;
    @MockitoBean
    private FmsAccountSetService accountSetService;
    @MockitoBean
    private FmsAuxiliaryTypeService auxiliaryTypeService;
    @MockitoBean
    private FmsAuxiliaryCombinationService auxiliaryCombinationService;
    @MockitoBean
    private FmsVoucherService voucherService;
    @MockitoBean
    private FmsVoucherTemplateService voucherTemplateService;
    @MockitoBean
    private FmsInitialBalanceService initialBalanceService;

    @Test
    public void testCreateAuxiliaryItem_success() {
        // mock 方法
        mockAuxiliaryType(1L, 11L);
        // 准备参数
        FmsAuxiliaryItemSaveReqVO reqVO = buildSaveReqVO(1L, 11L, "KH001", "测试客户");

        // 调用
        Long auxiliaryItemId = auxiliaryItemService.createAuxiliaryItem(reqVO, 10L);

        // 断言
        FmsAuxiliaryItemDO auxiliaryItem = auxiliaryItemMapper.selectById(auxiliaryItemId);
        assertEquals("KH001", auxiliaryItem.getCode());
        assertEquals("测试客户", auxiliaryItem.getName());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), auxiliaryItem.getStatus());
        assertEquals(11L, auxiliaryItem.getAuxiliaryTypeId());
        verify(accountSetService).validateAccountSetWritePermission(1L, 10L);
    }

    @Test
    public void testCreateAuxiliaryItem_codeDuplicate() {
        // mock 数据
        mockAuxiliaryType(1L, 11L);
        auxiliaryItemMapper.insert(buildAuxiliaryItem(1L, 11L, "KH001", "原客户"));
        // 准备参数
        FmsAuxiliaryItemSaveReqVO reqVO = buildSaveReqVO(1L, 11L, "KH001", "新客户");

        // 调用，并断言
        assertServiceException(() -> auxiliaryItemService.createAuxiliaryItem(reqVO, 10L),
                AUXILIARY_ITEM_CODE_DUPLICATE);
    }

    @Test
    public void testDeleteAuxiliaryItemList_success() {
        // mock 数据
        FmsAuxiliaryItemDO auxiliaryItem = buildAuxiliaryItem(1L, 11L, "KH001", "测试客户");
        auxiliaryItemMapper.insert(auxiliaryItem);

        // 调用
        auxiliaryItemService.deleteAuxiliaryItemList(
                1L, Collections.singletonList(auxiliaryItem.getId()), 10L);

        // 断言
        assertNull(auxiliaryItemMapper.selectById(auxiliaryItem.getId()));
        verify(auxiliaryCombinationService).deleteAuxiliaryCombinationByAuxiliaryItemIds(
                eq(1L), anyCollection());
    }

    @Test
    public void testDeleteAuxiliaryItemList_voucherTemplateInUse() {
        // mock 数据
        FmsAuxiliaryItemDO auxiliaryItem = buildAuxiliaryItem(1L, 11L, "KH001", "测试客户");
        auxiliaryItemMapper.insert(auxiliaryItem);
        // mock 方法
        when(voucherTemplateService.getVoucherTemplateCountByAuxiliaryItemIds(
                eq(1L), anyCollection())).thenReturn(1L);

        // 调用，并断言
        assertServiceException(() -> auxiliaryItemService.deleteAuxiliaryItemList(
                1L, Collections.singletonList(auxiliaryItem.getId()), 10L),
                AUXILIARY_ITEM_VOUCHER_TEMPLATE_IN_USE, 1L);
    }

    @Test
    public void testUpdateAuxiliaryItemStatus_success() {
        // mock 数据
        FmsAuxiliaryItemDO auxiliaryItem = buildAuxiliaryItem(1L, 11L, "KH001", "测试客户");
        auxiliaryItemMapper.insert(auxiliaryItem);

        // 调用
        auxiliaryItemService.updateAuxiliaryItemStatus(1L, auxiliaryItem.getId(),
                CommonStatusEnum.DISABLE.getStatus(), 10L);

        // 断言
        assertEquals(CommonStatusEnum.DISABLE.getStatus(),
                auxiliaryItemMapper.selectById(auxiliaryItem.getId()).getStatus());
        verify(accountSetService).validateAccountSetWritePermission(1L, 10L);
    }

    @Test
    public void testGetAuxiliaryItemPage_search() {
        // mock 数据
        mockAuxiliaryType(1L, 11L);
        auxiliaryItemMapper.insert(buildAuxiliaryItem(1L, 11L, "KH001", "上海测试"));
        auxiliaryItemMapper.insert(buildAuxiliaryItem(1L, 11L, "KH002", "北京云财务"));
        // 准备参数
        FmsAuxiliaryItemPageReqVO reqVO = new FmsAuxiliaryItemPageReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setAuxiliaryTypeId(11L);
        reqVO.setSearch("测试");

        // 调用
        PageResult<FmsAuxiliaryItemDO> pageResult = auxiliaryItemService
                .getAuxiliaryItemPage(reqVO, 10L);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals("KH001", CollUtil.getFirst(pageResult.getList()).getCode());
    }

    @Test
    public void testGetAuxiliaryItemListByAccountSetIdAndAuxiliaryTypeIdAndStatus() {
        // mock 数据
        mockAuxiliaryType(1L, 11L);
        auxiliaryItemMapper.insert(buildAuxiliaryItem(1L, 11L, "KH002", "北京客户"));
        auxiliaryItemMapper.insert(buildAuxiliaryItem(1L, 11L, "KH001", "上海客户"));
        auxiliaryItemMapper.insert(buildAuxiliaryItem(1L, 11L, "KH003", "停用客户")
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));

        // 调用
        List<FmsAuxiliaryItemDO> result = auxiliaryItemService
                .getAuxiliaryItemListByAccountSetIdAndAuxiliaryTypeIdAndStatus(
                        1L, 11L, CommonStatusEnum.ENABLE.getStatus(), 10L);

        // 断言
        assertEquals(2, result.size());
        assertEquals("KH001", result.get(0).getCode());
        assertEquals("KH002", result.get(1).getCode());
    }

    @Test
    public void testImportAuxiliaryItemList_empty() {
        // 调用，并断言
        assertServiceException(() -> auxiliaryItemService.importAuxiliaryItemList(
                1L, 11L, Collections.emptyList(), 10L), AUXILIARY_ITEM_IMPORT_LIST_IS_EMPTY);
    }

    @Test
    public void testImportAuxiliaryItemList_partialSuccess() {
        // mock 数据
        mockAuxiliaryType(1L, 11L);
        auxiliaryItemMapper.insert(buildAuxiliaryItem(1L, 11L, "KH001", "原客户"));
        FmsAuxiliaryItemImportExcelVO successRow = buildImportRow("KH002", "新客户");
        successRow.setRemark("重点客户");
        FmsAuxiliaryItemImportExcelVO existRow = buildImportRow("KH001", "重复客户");
        FmsAuxiliaryItemImportExcelVO duplicateRow = buildImportRow("KH002", "文件内重复");

        // 调用
        FmsAuxiliaryItemImportRespVO result = auxiliaryItemService.importAuxiliaryItemList(
                1L, 11L, Arrays.asList(successRow, existRow, duplicateRow), 10L);

        // 断言
        assertEquals(3, result.getTotalCount());
        assertEquals(Collections.singletonList("KH002"), result.getSuccessItemCodes());
        assertEquals(2, result.getFailureReasons().size());
        FmsAuxiliaryItemDO item = auxiliaryItemMapper.selectByTypeIdAndCode(1L, 11L, "KH002");
        assertEquals("新客户", item.getName());
        assertEquals("重点客户", item.getRemark());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), item.getStatus());
    }

    @Test
    public void testImportAuxiliaryItemList_inventory() {
        // mock 数据
        when(auxiliaryTypeService.validateAuxiliaryTypeList(eq(1L), anyCollection()))
                .thenReturn(Collections.singletonList(new FmsAuxiliaryTypeDO()
                        .setId(16L).setAccountSetId(1L).setName("存货").setSystemPreset(true)
                        .setType(FmsAuxiliaryTypeEnum.INVENTORY.getType())));
        FmsAuxiliaryItemImportExcelVO row = buildImportRow("SP001", "测试商品");
        row.setRemark("库存备注");
        row.setSpecification("标准版");
        row.setUnit("台");

        // 调用
        auxiliaryItemService.importAuxiliaryItemList(
                1L, 16L, Collections.singletonList(row), 10L);

        // 断言
        FmsAuxiliaryItemDO item = auxiliaryItemMapper.selectByTypeIdAndCode(1L, 16L, "SP001");
        assertEquals("库存备注", item.getRemark());
        assertEquals("标准版", item.getSpecification());
        assertEquals("台", item.getUnit());
    }

    // ========== 随机对象 ==========

    private void mockAuxiliaryType(Long accountSetId, Long auxiliaryTypeId) {
        when(auxiliaryTypeService.validateAuxiliaryTypeList(eq(accountSetId), anyCollection()))
                .thenReturn(Collections.singletonList(new FmsAuxiliaryTypeDO()
                        .setId(auxiliaryTypeId).setAccountSetId(accountSetId)
                        .setName("客户").setSystemPreset(true)
                        .setType(FmsAuxiliaryTypeEnum.CUSTOMER.getType())));
    }

    private FmsAuxiliaryItemSaveReqVO buildSaveReqVO(
            Long accountSetId, Long auxiliaryTypeId, String code, String name) {
        FmsAuxiliaryItemSaveReqVO reqVO = new FmsAuxiliaryItemSaveReqVO();
        reqVO.setAccountSetId(accountSetId);
        reqVO.setAuxiliaryTypeId(auxiliaryTypeId);
        reqVO.setCode(code);
        reqVO.setName(name);
        return reqVO;
    }

    private FmsAuxiliaryItemImportExcelVO buildImportRow(String code, String name) {
        FmsAuxiliaryItemImportExcelVO row = new FmsAuxiliaryItemImportExcelVO();
        row.setCode(code);
        row.setName(name);
        return row;
    }

    private FmsAuxiliaryItemDO buildAuxiliaryItem(
            Long accountSetId, Long auxiliaryTypeId, String code, String name) {
        return randomPojo(FmsAuxiliaryItemDO.class, auxiliaryItem -> auxiliaryItem.setId(null)
                .setAccountSetId(accountSetId).setAuxiliaryTypeId(auxiliaryTypeId)
                .setCode(code).setName(name).setStatus(CommonStatusEnum.ENABLE.getStatus()));
    }

}
