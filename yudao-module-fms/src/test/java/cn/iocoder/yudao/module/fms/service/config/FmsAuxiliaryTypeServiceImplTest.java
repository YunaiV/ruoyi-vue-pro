package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliarytype.FmsAuxiliaryTypeSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAuxiliaryTypeMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsAuxiliaryTypeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherTemplateService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsAuxiliaryTypeServiceImpl.class)
public class FmsAuxiliaryTypeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsAuxiliaryTypeServiceImpl auxiliaryTypeService;
    @Resource
    private FmsAuxiliaryTypeMapper auxiliaryTypeMapper;
    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsAuxiliaryItemService auxiliaryItemService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsVoucherService voucherService;
    @MockBean
    private FmsVoucherTemplateService voucherTemplateService;
    @MockBean
    private FmsInitialBalanceService initialBalanceService;

    @Test
    public void testInitializeDefaultTypes_success() {
        // 准备参数
        Long accountSetId = 1L;

        // 调用
        auxiliaryTypeService.initializeDefaultTypes(accountSetId);

        // 断言
        List<FmsAuxiliaryTypeDO> types = auxiliaryTypeMapper.selectListByAccountSetId(accountSetId);
        assertEquals(6, types.size());
        assertEquals(FmsAuxiliaryTypeEnum.CUSTOMER.getName(), types.get(0).getName());
        assertEquals(FmsAuxiliaryTypeEnum.INVENTORY.getName(), types.get(5).getName());
        assertTrue(types.stream().allMatch(FmsAuxiliaryTypeDO::getSystemPreset));
        assertTrue(types.stream().allMatch(type -> accountSetId.equals(type.getAccountSetId())));
        assertFalse(types.stream().anyMatch(
                type -> FmsAuxiliaryTypeEnum.CUSTOM.getType().equals(type.getType())));
    }

    @Test
    public void testGetAuxiliaryTypeIdMap() {
        // mock 数据
        Long accountSetId = 1L;
        FmsAuxiliaryTypeDO customer = buildAuxiliaryType(accountSetId, "客户", true);
        FmsAuxiliaryTypeDO store = buildAuxiliaryType(accountSetId, "门店", false);
        auxiliaryTypeMapper.insert(customer);
        auxiliaryTypeMapper.insert(store);

        // 调用
        Map<String, Long> auxiliaryTypeIdMap = auxiliaryTypeService.getAuxiliaryTypeIdMap(accountSetId);

        // 断言
        assertEquals(2, auxiliaryTypeIdMap.size());
        assertEquals(customer.getId(), auxiliaryTypeIdMap.get("客户"));
        assertEquals(store.getId(), auxiliaryTypeIdMap.get("门店"));
    }

    @Test
    public void testCreateAuxiliaryType_success() {
        // 准备参数
        FmsAuxiliaryTypeSaveReqVO reqVO = new FmsAuxiliaryTypeSaveReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setName("门店");

        // 调用
        Long auxiliaryTypeId = auxiliaryTypeService.createAuxiliaryType(reqVO, 10L);

        // 断言
        FmsAuxiliaryTypeDO auxiliaryType = auxiliaryTypeMapper.selectById(auxiliaryTypeId);
        assertEquals("门店", auxiliaryType.getName());
        assertEquals(FmsAuxiliaryTypeEnum.CUSTOM.getType(), auxiliaryType.getType());
        assertFalse(auxiliaryType.getSystemPreset());
        assertEquals(1L, auxiliaryType.getAccountSetId());
        verify(accountSetService).validateAccountSetWritePermission(1L, 10L);
    }

    @Test
    public void testCreateAuxiliaryType_nameDuplicate() {
        // mock 数据
        auxiliaryTypeMapper.insert(buildAuxiliaryType(1L, "门店", false));
        // 准备参数
        FmsAuxiliaryTypeSaveReqVO reqVO = new FmsAuxiliaryTypeSaveReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setName("门店");

        // 调用，并断言
        assertServiceException(() -> auxiliaryTypeService.createAuxiliaryType(reqVO, 10L),
                AUXILIARY_TYPE_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateAuxiliaryType_systemPreset() {
        // mock 数据
        FmsAuxiliaryTypeDO auxiliaryType = buildAuxiliaryType(1L, "客户", true);
        auxiliaryTypeMapper.insert(auxiliaryType);
        // 准备参数
        FmsAuxiliaryTypeSaveReqVO reqVO = new FmsAuxiliaryTypeSaveReqVO();
        reqVO.setId(auxiliaryType.getId());
        reqVO.setAccountSetId(1L);
        reqVO.setName("客户资料");

        // 调用，并断言
        assertServiceException(() -> auxiliaryTypeService.updateAuxiliaryType(reqVO, 10L),
                AUXILIARY_TYPE_SYSTEM_NOT_EDITABLE);
    }

    @Test
    public void testDeleteAuxiliaryType_hasItem() {
        // mock 数据
        FmsAuxiliaryTypeDO auxiliaryType = buildAuxiliaryType(1L, "门店", false);
        auxiliaryTypeMapper.insert(auxiliaryType);
        // mock 方法
        when(auxiliaryItemService.getAuxiliaryItemCountByAuxiliaryTypeId(
                1L, auxiliaryType.getId())).thenReturn(1L);

        // 调用，并断言
        assertServiceException(() -> auxiliaryTypeService.deleteAuxiliaryType(
                1L, auxiliaryType.getId(), 10L), AUXILIARY_TYPE_HAS_ITEM, 1L);
    }

    @Test
    public void testDeleteAuxiliaryType_inUse() {
        // mock 数据
        FmsAuxiliaryTypeDO auxiliaryType = buildAuxiliaryType(1L, "门店", false);
        auxiliaryTypeMapper.insert(auxiliaryType);
        when(subjectService.getSubjectCountByAuxiliaryTypeId(1L, auxiliaryType.getId()))
                .thenReturn(1L);

        // 调用，并断言
        assertServiceException(() -> auxiliaryTypeService.deleteAuxiliaryType(
                1L, auxiliaryType.getId(), 10L), AUXILIARY_TYPE_SUBJECT_IN_USE, 1L);
    }

    // ========== 随机对象 ==========

    private FmsAuxiliaryTypeDO buildAuxiliaryType(Long accountSetId, String name, boolean systemPreset) {
        return randomPojo(FmsAuxiliaryTypeDO.class, auxiliaryType -> auxiliaryType.setId(null)
                .setName(name).setSystemPreset(systemPreset).setAccountSetId(accountSetId)
                .setType(systemPreset
                        ? FmsAuxiliaryTypeEnum.CUSTOMER.getType() : FmsAuxiliaryTypeEnum.CUSTOM.getType()));
    }

}
