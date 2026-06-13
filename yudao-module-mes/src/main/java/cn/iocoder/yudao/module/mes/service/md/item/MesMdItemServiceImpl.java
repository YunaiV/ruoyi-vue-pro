package cn.iocoder.yudao.module.mes.service.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemSaveReqVO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemBatchConfigDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.item.MesMdItemMapper;
import cn.iocoder.yudao.module.mes.enums.md.MesMdItemTypeEnum;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodeRuleCodeEnum;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.md.autocode.MesMdAutoCodeRecordService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 物料产品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdItemServiceImpl implements MesMdItemService {

    @Resource
    private MesMdItemMapper itemMapper;

    @Resource
    private MesMdItemTypeService itemTypeService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;
    @Resource
    private MesMdItemBatchConfigService itemBatchConfigService;
    @Resource
    private MesMdProductBomService productBomService;
    @Resource
    private MesMdProductSopService productSopService;
    @Resource
    private MesMdProductSipService productSipService;
    @Resource
    private MesWmBarcodeService barcodeService;
    @Resource
    private MesMdAutoCodeRecordService autoCodeRecordService;

    @Override
    public Long createItem(MesMdItemSaveReqVO createReqVO) {
        // 校验数据
        validateItemSaveData(createReqVO);

        // 插入
        MesMdItemDO item = BeanUtils.toBean(createReqVO, MesMdItemDO.class);
        item.setStatus(CommonStatusEnum.DISABLE.getStatus()); // 默认禁用。信息完成后，再启用
        clearStockIfNotSafe(item); // 如果未启用安全库存，清零库存上下限
        itemMapper.insert(item);

        // 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.ITEM.getValue(),
                item.getId(), item.getCode(), item.getName());
        return item.getId();
    }

    @Override
    public void updateItem(MesMdItemSaveReqVO updateReqVO) {
        // 校验存在
        validateItemExists(updateReqVO.getId());
        // 校验数据
        validateItemSaveData(updateReqVO);

        // 更新
        MesMdItemDO updateObj = BeanUtils.toBean(updateReqVO, MesMdItemDO.class);
        clearStockIfNotSafe(updateObj); // 如果未启用安全库存，清零库存上下限
        itemMapper.updateById(updateObj);
    }

    private void validateItemSaveData(MesMdItemSaveReqVO reqVO) {
        // 校验物料编码的唯一性
        validateItemCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验物料名称的唯一性
        validateItemNameUnique(reqVO.getId(), reqVO.getName());
        // 校验物料分类存在
        validateItemTypeExists(reqVO.getItemTypeId());
        // 校验计量单位存在
        validateUnitMeasureExists(reqVO.getUnitMeasureId());
    }

    @Override
    public void updateItemStatus(Long id, Integer status) {
        // 校验存在
        MesMdItemDO item = validateItemExists(id);
        if (CommonStatusEnum.isEnable(status)) {
            // 如果启用了批次管理，校验批次属性配置
            if (Boolean.TRUE.equals(item.getBatchFlag())) {
                validateBatchConfigExists(id);
            }
            // 如果是产品类型，校验必须配置 BOM
            MesMdItemTypeDO itemType = itemTypeService.getItemType(item.getItemTypeId());
            if (MesMdItemTypeEnum.isProduct(itemType.getItemOrProduct())) {
                if (CollUtil.isEmpty(productBomService.getProductBomListByItemId(id))) {
                    throw exception(MD_ITEM_PRODUCT_BOM_REQUIRED);
                }
            }
        }

        // 更新状态
        itemMapper.updateById(new MesMdItemDO().setId(id).setStatus(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        // 校验存在
        validateItemExists(id);

        // 级联删除批次属性配置
        itemBatchConfigService.deleteItemBatchConfigByItemId(id);
        // 级联删除产品BOM
        productBomService.deleteProductBomByItemId(id);
        // 级联删除产品SOP
        productSopService.deleteProductSopByItemId(id);
        // 级联删除产品SIP
        productSipService.deleteProductSipByItemId(id);
        // 删除
        itemMapper.deleteById(id);
    }

    @Override
    public MesMdItemDO validateItemExists(Long id) {
        MesMdItemDO item = itemMapper.selectById(id);
        if (item == null) {
            throw exception(MD_ITEM_NOT_EXISTS);
        }
        return item;
    }

    @Override
    public MesMdItemDO validateItemExistsAndEnable(Long id) {
        MesMdItemDO item = validateItemExists(id);
        if (ObjUtil.notEqual(CommonStatusEnum.ENABLE.getStatus(), item.getStatus())) {
            throw exception(MD_ITEM_IS_DISABLE);
        }
        return item;
    }

    private void validateItemCodeUnique(Long id, String code) {
        MesMdItemDO item = itemMapper.selectByCode(code);
        if (item == null) {
            return;
        }
        if (ObjUtil.notEqual(item.getId(), id)) {
            throw exception(MD_ITEM_CODE_DUPLICATE);
        }
    }

    private void validateItemNameUnique(Long id, String name) {
        MesMdItemDO item = itemMapper.selectByName(name);
        if (item == null) {
            return;
        }
        if (ObjUtil.notEqual(item.getId(), id)) {
            throw exception(MD_ITEM_NAME_DUPLICATE);
        }
    }

    private void validateItemTypeExists(Long itemTypeId) {
        if (itemTypeService.getItemType(itemTypeId) == null) {
            throw exception(MD_ITEM_TYPE_NOT_EXISTS);
        }
        // 校验必须是叶子分类（没有子分类）
        List<MesMdItemTypeDO> children = itemTypeService.getItemTypeChildrenList(itemTypeId);
        if (CollUtil.isNotEmpty(children)) {
            throw exception(MD_ITEM_TYPE_NOT_LEAF);
        }
    }

    private void validateUnitMeasureExists(Long unitMeasureId) {
        if (unitMeasureService.getUnitMeasure(unitMeasureId) == null) {
            throw exception(MD_UNIT_MEASURE_NOT_EXISTS);
        }
    }

    /**
     * 如果未启用安全库存，清零库存上下限
     */
    private void clearStockIfNotSafe(MesMdItemDO item) {
        if (Boolean.TRUE.equals(item.getSafeStockFlag())) {
            return;
        }
        item.setMinStock(BigDecimal.ZERO).setMaxStock(BigDecimal.ZERO);
    }

    /**
     * 如果启用了批次管理且状态为开启，校验批次属性配置是否存在且至少一个属性为 true
     */
    private void validateBatchConfigExists(Long itemId) {
        MesMdItemBatchConfigDO config = itemBatchConfigService.getItemBatchConfigByItemId(itemId);
        if (config == null) {
            throw exception(MD_ITEM_BATCH_CONFIG_NOT_EXISTS);
        }
        boolean hasAnyFlag = Boolean.TRUE.equals(config.getProduceDateFlag())
                || Boolean.TRUE.equals(config.getExpireDateFlag())
                || Boolean.TRUE.equals(config.getReceiptDateFlag())
                || Boolean.TRUE.equals(config.getVendorFlag())
                || Boolean.TRUE.equals(config.getClientFlag())
                || Boolean.TRUE.equals(config.getSalesOrderCodeFlag())
                || Boolean.TRUE.equals(config.getPurchaseOrderCodeFlag())
                || Boolean.TRUE.equals(config.getWorkOrderFlag())
                || Boolean.TRUE.equals(config.getTaskFlag())
                || Boolean.TRUE.equals(config.getWorkstationFlag())
                || Boolean.TRUE.equals(config.getToolFlag())
                || Boolean.TRUE.equals(config.getMoldFlag())
                || Boolean.TRUE.equals(config.getLotNumberFlag())
                || Boolean.TRUE.equals(config.getQualityStatusFlag());
        if (!hasAnyFlag) {
            throw exception(MD_ITEM_BATCH_CONFIG_AT_LEAST_ONE_FLAG);
        }
    }

    @Override
    public MesMdItemDO getItem(Long id) {
        return itemMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdItemDO> getItemPage(MesMdItemPageReqVO pageReqVO) {
        // 查找时，如果查找某个分类编号，则包含它的子分类
        Set<Long> itemTypeIds = null;
        if (pageReqVO.getItemTypeId() != null) {
            itemTypeIds = new HashSet<>();
            itemTypeIds.add(pageReqVO.getItemTypeId());
            List<MesMdItemTypeDO> children = itemTypeService.getItemTypeChildrenList(pageReqVO.getItemTypeId());
            itemTypeIds.addAll(convertSet(children, MesMdItemTypeDO::getId));
        }
        // 分页查询
        return itemMapper.selectPage(pageReqVO, itemTypeIds);
    }

    @Override
    public List<MesMdItemDO> getItemList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return itemMapper.selectByIds(ids);
    }

    @Override
    public Long getItemCountByItemTypeId(Long itemTypeId) {
        return itemMapper.selectCountByItemTypeId(itemTypeId);
    }

    @Override
    public Long getItemCountByUnitMeasureId(Long unitMeasureId) {
        return itemMapper.selectCountByUnitMeasureId(unitMeasureId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MesMdItemImportRespVO importItemList(List<MesMdItemImportExcelVO> importItems, boolean updateSupport) {
        // 1. 参数校验
        if (CollUtil.isEmpty(importItems)) {
            throw exception(MD_ITEM_IMPORT_LIST_IS_EMPTY);
        }

        // 2. 遍历，逐个创建 or 更新
        MesMdItemImportRespVO respVO = MesMdItemImportRespVO.builder()
                .createCodes(new ArrayList<>()).updateCodes(new ArrayList<>())
                .failureCodes(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importItems.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            // 2.1 校验字段
            if (StrUtil.isBlank(importItem.getCode())) {
                // 空编码时自动生成
                importItem.setCode(autoCodeRecordService.generateAutoCode(MesMdAutoCodeRuleCodeEnum.MD_ITEM_CODE.getCode()));
            }
            String key = importItem.getCode();
            if (StrUtil.isBlank(importItem.getName())) {
                respVO.getFailureCodes().put(key, "物料名称不能为空");
                return;
            }
            if (StrUtil.isBlank(importItem.getUnitMeasureCode())) {
                respVO.getFailureCodes().put(key, "单位编码不能为空");
                return;
            }
            // 将单位编码转换为单位 ID
            MesMdUnitMeasureDO unitMeasure = unitMeasureService.getUnitMeasureByCode(importItem.getUnitMeasureCode());
            if (unitMeasure == null) {
                respVO.getFailureCodes().put(key, "单位编码[" + importItem.getUnitMeasureCode() + "]不存在");
                return;
            }
            if (importItem.getItemTypeId() == null) {
                respVO.getFailureCodes().put(key, "物料分类编号不能为空");
                return;
            }
            // 2.2 校验分类是否存在
            try {
                validateItemTypeExists(importItem.getItemTypeId());
            } catch (ServiceException ex) {
                respVO.getFailureCodes().put(key, ex.getMessage());
                return;
            }

            // 2.3 判断：创建 or 更新
            MesMdItemDO existItem = itemMapper.selectByCode(importItem.getCode());
            if (existItem == null) {
                // 2.3.1 创建
                try {
                    validateItemNameUnique(null, importItem.getName());
                } catch (ServiceException ex) {
                    respVO.getFailureCodes().put(key, ex.getMessage());
                    return;
                }
                MesMdItemDO item = BeanUtils.toBean(importItem, MesMdItemDO.class);
                item.setUnitMeasureId(unitMeasure.getId());
                item.setStatus(CommonStatusEnum.DISABLE.getStatus()); // 默认禁用。信息完成后，再启用
                clearStockIfNotSafe(item);
                itemMapper.insert(item);
                // 自动生成条码
                barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.ITEM.getValue(),
                        item.getId(), item.getCode(), item.getName());
                respVO.getCreateCodes().add(importItem.getCode());
            } else if (updateSupport) {
                // 2.3.2 更新
                try {
                    validateItemNameUnique(existItem.getId(), importItem.getName());
                } catch (ServiceException ex) {
                    respVO.getFailureCodes().put(key, ex.getMessage());
                    return;
                }
                MesMdItemDO updateObj = BeanUtils.toBean(importItem, MesMdItemDO.class);
                updateObj.setId(existItem.getId());
                updateObj.setUnitMeasureId(unitMeasure.getId());
                clearStockIfNotSafe(updateObj);
                itemMapper.updateById(updateObj);
                respVO.getUpdateCodes().add(importItem.getCode());
            } else {
                // 不支持更新
                respVO.getFailureCodes().put(key, "物料编码已存在");
            }
        });
        return respVO;
    }

}
