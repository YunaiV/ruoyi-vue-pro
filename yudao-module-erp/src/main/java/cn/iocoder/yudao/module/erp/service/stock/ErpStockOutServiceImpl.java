package cn.iocoder.yudao.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.out.ErpStockOutPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.out.ErpStockOutSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockOutItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockOutItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
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
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 其它出库单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpStockOutServiceImpl implements ErpStockOutService {

    @Resource
    private ErpStockOutMapper stockOutMapper;
    @Resource
    private ErpStockOutItemMapper stockOutItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpCustomerService customerService;
    @Resource
    private ErpStockRecordService stockRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockOut(ErpStockOutSaveReqVO createReqVO) {
        // 1.1 校验出库项的有效性
        List<ErpStockOutItemDO> stockOutItems = validateStockOutItems(createReqVO.getItems());
        // 1.2 校验客户
        customerService.validateCustomer(createReqVO.getCustomerId());
        // 1.3 生成出库单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.STOCK_OUT_NO_PREFIX);
        if (stockOutMapper.selectByNo(no) != null) {
            throw exception(STOCK_OUT_NO_EXISTS);
        }

        // 2.1 插入出库单
        ErpStockOutDO stockOut = BeanUtils.toBean(createReqVO, ErpStockOutDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setTotalCount(getSumValue(stockOutItems, ErpStockOutItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockOutItems, ErpStockOutItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO)));
        stockOutMapper.insert(stockOut);
        // 2.2 插入出库单项
        stockOutItems.forEach(o -> o.setOutId(stockOut.getId()));
        stockOutItemMapper.insertBatch(stockOutItems);
        return stockOut.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockOut(ErpStockOutSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpStockOutDO stockOut = validateStockOutExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(stockOut.getStatus())) {
            throw exception(STOCK_OUT_UPDATE_FAIL_APPROVE, stockOut.getNo());
        }
        // 1.2 校验客户
        customerService.validateCustomer(updateReqVO.getCustomerId());
        // 1.3 校验出库项的有效性
        List<ErpStockOutItemDO> stockOutItems = validateStockOutItems(updateReqVO.getItems());

        // 2.1 更新出库单
        ErpStockOutDO updateObj = BeanUtils.toBean(updateReqVO, ErpStockOutDO.class, in -> in
                .setTotalCount(getSumValue(stockOutItems, ErpStockOutItemDO::getCount, BigDecimal::add))
                .setTotalPrice(getSumValue(stockOutItems, ErpStockOutItemDO::getTotalPrice, BigDecimal::add)));
        stockOutMapper.updateById(updateObj);
        // 2.2 更新出库单项
        updateStockOutItemList(updateReqVO.getId(), stockOutItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockOutStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpStockOutDO stockOut = validateStockOutExists(id);
        // 1.2 校验状态
        if (stockOut.getStatus().equals(status)) {
            throw exception(approve ? STOCK_OUT_APPROVE_FAIL : STOCK_OUT_PROCESS_FAIL);
        }

        // 2. 更新状态
        int updateCount = stockOutMapper.updateByIdAndStatus(id, stockOut.getStatus(),
                new ErpStockOutDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? STOCK_OUT_APPROVE_FAIL : STOCK_OUT_PROCESS_FAIL);
        }

        // 3. 变更库存
        List<ErpStockOutItemDO> stockOutItems = stockOutItemMapper.selectListByOutId(id);
        Integer bizType = approve ? ErpStockRecordBizTypeEnum.OTHER_OUT.getType()
                : ErpStockRecordBizTypeEnum.OTHER_OUT_CANCEL.getType();
        stockOutItems.forEach(stockOutItem -> {
            BigDecimal count = approve ? stockOutItem.getCount().negate() : stockOutItem.getCount();
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    stockOutItem.getProductId(), stockOutItem.getWarehouseId(), count,
                    bizType, stockOutItem.getOutId(), stockOutItem.getId(), stockOut.getNo()));
        });
    }

    private List<ErpStockOutItemDO> validateStockOutItems(List<ErpStockOutSaveReqVO.Item> list) {
        // 1.1 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpStockOutSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 1.2 校验仓库存在
        warehouseService.validWarehouseList(convertSet(list, ErpStockOutSaveReqVO.Item::getWarehouseId));
        // 2. 转化为 ErpStockOutItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpStockOutItemDO.class, item -> item
                .setProductUnitId(productMap.get(item.getProductId()).getUnitId())
                .setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()))));
    }

    private void updateStockOutItemList(Long id, List<ErpStockOutItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpStockOutItemDO> oldList = stockOutItemMapper.selectListByOutId(id);
        List<List<ErpStockOutItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOutId(id));
            stockOutItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            stockOutItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            stockOutItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpStockOutItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockOut(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpStockOutDO> stockOuts = stockOutMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(stockOuts)) {
            return;
        }
        stockOuts.forEach(stockOut -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(stockOut.getStatus())) {
                throw exception(STOCK_OUT_DELETE_FAIL_APPROVE, stockOut.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        stockOuts.forEach(stockOut -> {
            // 2.1 删除出库单
            stockOutMapper.deleteById(stockOut.getId());
            // 2.2 删除出库单项
            stockOutItemMapper.deleteByOutId(stockOut.getId());
        });
    }

    private ErpStockOutDO validateStockOutExists(Long id) {
        ErpStockOutDO stockOut = stockOutMapper.selectById(id);
        if (stockOut == null) {
            throw exception(STOCK_OUT_NOT_EXISTS);
        }
        return stockOut;
    }

    @Override
    public ErpStockOutDO getStockOut(Long id) {
        return stockOutMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockOutDO> getStockOutPage(ErpStockOutPageReqVO pageReqVO) {
        return stockOutMapper.selectPage(pageReqVO);
    }

    // ==================== 出库项 ====================

    @Override
    public List<ErpStockOutItemDO> getStockOutItemListByOutId(Long outId) {
        return stockOutItemMapper.selectListByOutId(outId);
    }

    @Override
    public List<ErpStockOutItemDO> getStockOutItemListByOutIds(Collection<Long> outIds) {
        if (CollUtil.isEmpty(outIds)) {
            return Collections.emptyList();
        }
        return stockOutItemMapper.selectListByOutIds(outIds);
    }

}