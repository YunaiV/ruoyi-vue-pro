package cn.iocoder.yudao.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.in.ErpStockInPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.in.ErpStockInSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockInItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockInItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockInMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_IN_NOT_EXISTS;


/**
 * ERP 其它入库单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpStockInServiceImpl implements ErpStockInService {

    @Resource
    private ErpStockInMapper stockInMapper;
    @Resource
    private ErpStockInItemMapper stockInItemMapper;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpSupplierService supplierService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockIn(ErpStockInSaveReqVO createReqVO) {
        // 1.1 校验入库项的有效性
        List<ErpStockInItemDO> stockInItems = validateStockInItems(createReqVO.getItems());
        // 1.2 校验供应商
        supplierService.validateSupplier(createReqVO.getSupplierId());

        // 2.1 插入入库单
        ErpStockInDO stockIn = BeanUtils.toBean(createReqVO, ErpStockInDO.class, in -> in
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setTotalCount(getSumValue(stockInItems, ErpStockInItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockInItems, ErpStockInItemDO::getTotalPrice, BigDecimal::add)));
        stockInMapper.insert(stockIn);
        // 2. 插入子表
        stockInItems.forEach(o -> o.setInId(stockIn.getId()));
        stockInItemMapper.insertBatch(stockInItems);
        return stockIn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockIn(ErpStockInSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateStockInExists(updateReqVO.getId());
        // 1.2 校验供应商
        supplierService.validateSupplier(updateReqVO.getSupplierId());
        // 1.3 校验入库项的有效性
        List<ErpStockInItemDO> stockInItems = validateStockInItems(updateReqVO.getItems());

        // 2.1 更新入库单
        ErpStockInDO updateObj = BeanUtils.toBean(updateReqVO, ErpStockInDO.class, in -> in
                .setTotalCount(getSumValue(stockInItems, ErpStockInItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockInItems, ErpStockInItemDO::getTotalPrice, BigDecimal::add)));
        stockInMapper.updateById(updateObj);
        // 2.2 更新入库单项
        updateStockInItemList(updateReqVO.getId(), stockInItems);
    }

    private List<ErpStockInItemDO> validateStockInItems(List<ErpStockInSaveReqVO.Item> list) {
        // 1.1 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(convertSet(list, ErpStockInSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 1.2 校验仓库存在
        warehouseService.validWarehouseList(convertSet(list, ErpStockInSaveReqVO.Item::getWarehouseId));
        // 2. 转化为 ErpStockInItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpStockInItemDO.class, item -> item
                .setProductUnitId(productMap.get(item.getProductId()).getUnitId())
                .setTotalPrice(item.getProductPrice().multiply(item.getCount()))));
    }

    private void updateStockInItemList(Long id, List<ErpStockInItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpStockInItemDO> oldList = stockInItemMapper.selectListByInId(id);
        List<List<ErpStockInItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setInId(id));
            stockInItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            stockInItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            stockInItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpStockInItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockIn(Long id) {
        // 1. 校验存在
        validateStockInExists(id);
        // TODO 芋艿：校验一下；

        // 2.1 删除
        stockInMapper.deleteById(id);
        // 2.2 删除子表
        stockInItemMapper.deleteByInId(id);
    }

    private void validateStockInExists(Long id) {
        if (stockInMapper.selectById(id) == null) {
            throw exception(STOCK_IN_NOT_EXISTS);
        }
    }

    @Override
    public ErpStockInDO getStockIn(Long id) {
        return stockInMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockInDO> getStockInPage(ErpStockInPageReqVO pageReqVO) {
        return stockInMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（ERP 其它入库单项） ====================

    @Override
    public List<ErpStockInItemDO> getStockInItemListByInId(Long inId) {
        return stockInItemMapper.selectListByInId(inId);
    }

    @Override
    public List<ErpStockInItemDO> getStockInItemListByInIds(Collection<Long> inIds) {
        if (CollUtil.isEmpty(inIds)) {
            return Collections.emptyList();
        }
        return stockInItemMapper.selectListByInIds(inIds);
    }

}