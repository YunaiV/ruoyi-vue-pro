package cn.iocoder.yudao.module.wms.service.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.mysql.md.item.WmsItemSkuMapper;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.order.check.WmsCheckOrderDetailService;
import cn.iocoder.yudao.module.wms.service.order.movement.WmsMovementOrderDetailService;
import cn.iocoder.yudao.module.wms.service.order.receipt.WmsReceiptOrderDetailService;
import cn.iocoder.yudao.module.wms.service.order.shipment.WmsShipmentOrderDetailService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 商品 SKU Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsItemSkuServiceImpl implements WmsItemSkuService {

    @Resource
    private WmsItemSkuMapper itemSkuMapper;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsInventoryService inventoryService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsReceiptOrderDetailService receiptOrderDetailService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsShipmentOrderDetailService shipmentOrderDetailService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsMovementOrderDetailService movementOrderDetailService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsCheckOrderDetailService checkOrderDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createItemSkuList(Long itemId, List<WmsItemSkuSaveReqVO> skus) {
        validateItemSkuList(skus);
        List<WmsItemSkuDO> list = BeanUtils.toBean(skus, WmsItemSkuDO.class, sku ->
                sku.setItemId(itemId).setId(null)); // id 由数据库自增分配，避免前端误操作
        itemSkuMapper.insertBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItemSkuList(Long itemId, List<WmsItemSkuSaveReqVO> skus) {
        validateItemSkuList(skus);

        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<WmsItemSkuDO> oldList = itemSkuMapper.selectListByItemId(itemId);
        List<WmsItemSkuDO> newList = BeanUtils.toBean(skus, WmsItemSkuDO.class);
        List<List<WmsItemSkuDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> ObjectUtil.equal(oldVal.getId(), newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            List<Long> deleteSkuIds = convertList(diffList.get(2), WmsItemSkuDO::getId);
            // 校验 SKU 是否被使用
            validateItemSkuUnused(diffList.get(2));
            // 删除 SKU
            itemSkuMapper.deleteByIds(deleteSkuIds);
        }
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            // 新增场景下忽略前端误传的 SKU id：统一置 null，由数据库自增分配
            diffList.get(0).forEach(sku -> {
                sku.setItemId(itemId);
                sku.setId(null);
            });
            itemSkuMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            diffList.get(1).forEach(sku -> sku.setItemId(itemId));
            itemSkuMapper.updateBatch(diffList.get(1));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItemSkuListByItemId(Long itemId) {
        // 校验 SKU 是否被使用
        List<WmsItemSkuDO> skus = itemSkuMapper.selectListByItemId(itemId);
        validateItemSkuUnused(skus);
        // 删除 SKU
        itemSkuMapper.deleteByItemId(itemId);
    }

    @Override
    public WmsItemSkuDO validateItemSkuExists(Long id) {
        WmsItemSkuDO sku = itemSkuMapper.selectById(id);
        if (sku == null) {
            throw exception(ITEM_SKU_NOT_EXISTS);
        }
        return sku;
    }

    @Override
    public List<WmsItemSkuDO> getItemSkuList(Long itemId) {
        return itemSkuMapper.selectListByItemId(itemId);
    }

    @Override
    public List<WmsItemSkuDO> getItemSkuList(Collection<Long> itemIds) {
        return itemSkuMapper.selectListByItemIds(itemIds);
    }

    @Override
    public List<WmsItemSkuDO> getItemSkuList(Collection<Long> itemIds, String code, String name) {
        return itemSkuMapper.selectList(itemIds, code, name);
    }

    @Override
    public List<WmsItemSkuDO> getItemSkuListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.of();
        }
        return itemSkuMapper.selectByIds(ids);
    }

    @Override
    public PageResult<WmsItemSkuDO> getItemSkuPage(WmsItemSkuPageReqVO pageReqVO) {
        return itemSkuMapper.selectPage(pageReqVO);
    }

    private void validateItemSkuList(List<WmsItemSkuSaveReqVO> skus) {
        // 校验至少存在一个商品 SKU
        if (CollUtil.isEmpty(skus)) {
            throw exception(ITEM_SKU_REQUIRED);
        }
        // 校验 SKU 名称不重复
        Set<String> names = new HashSet<>();
        for (WmsItemSkuSaveReqVO sku : skus) {
            if (!names.add(sku.getName())) {
                throw exception(ITEM_SKU_NAME_DUPLICATE, sku.getName());
            }
        }
    }

    private void validateItemSkuUnused(List<WmsItemSkuDO> skus) {
        if (CollUtil.isEmpty(skus)) {
            return;
        }
        for (WmsItemSkuDO sku : skus) {
            validateItemSkuOrderUnused(sku);
            if (inventoryService.getInventoryCountBySkuId(sku.getId()) > 0) {
                throw exception(ITEM_SKU_HAS_INVENTORY, sku.getName());
            }
        }
    }

    private void validateItemSkuOrderUnused(WmsItemSkuDO sku) {
        if (receiptOrderDetailService.getReceiptOrderDetailCountBySkuId(sku.getId()) > 0) {
            throw exception(ITEM_SKU_HAS_ORDER, sku.getName(), WmsOrderTypeEnum.RECEIPT.getName());
        }
        if (shipmentOrderDetailService.getShipmentOrderDetailCountBySkuId(sku.getId()) > 0) {
            throw exception(ITEM_SKU_HAS_ORDER, sku.getName(), WmsOrderTypeEnum.SHIPMENT.getName());
        }
        if (movementOrderDetailService.getMovementOrderDetailCountBySkuId(sku.getId()) > 0) {
            throw exception(ITEM_SKU_HAS_ORDER, sku.getName(), WmsOrderTypeEnum.MOVEMENT.getName());
        }
        if (checkOrderDetailService.getCheckOrderDetailCountBySkuId(sku.getId()) > 0) {
            throw exception(ITEM_SKU_HAS_ORDER, sku.getName(), WmsOrderTypeEnum.CHECK.getName());
        }
    }

}
