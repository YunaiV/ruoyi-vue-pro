package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.returns.ErpSaleReturnPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.returns.ErpSaleReturnSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleReturnItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleReturnMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockRecordService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
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
 * ERP 销售退货 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpSaleReturnServiceImpl implements ErpSaleReturnService {

    @Resource
    private ErpSaleReturnMapper saleReturnMapper;
    @Resource
    private ErpSaleReturnItemMapper saleReturnItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpSaleOrderService saleOrderService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpStockRecordService stockRecordService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSaleReturn(ErpSaleReturnSaveReqVO createReqVO) {
        // 1.1 校验销售订单已审核
        ErpSaleOrderDO saleOrder = saleOrderService.validateSaleOrder(createReqVO.getOrderId());
        // 1.2 校验退货项的有效性
        List<ErpSaleReturnItemDO> saleReturnItems = validateSaleReturnItems(createReqVO.getItems());
        // 1.3 校验结算账户
        accountService.validateAccount(createReqVO.getAccountId());
        // 1.4 校验销售人员
        if (createReqVO.getSaleUserId() != null) {
            adminUserApi.validateUser(createReqVO.getSaleUserId());
        }
        // 1.5 生成退货单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_RETURN_NO_PREFIX);
        if (saleReturnMapper.selectByNo(no) != null) {
            throw exception(SALE_RETURN_NO_EXISTS);
        }

        // 2.1 插入退货
        ErpSaleReturnDO saleReturn = BeanUtils.toBean(createReqVO, ErpSaleReturnDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus()))
                .setOrderNo(saleOrder.getNo()).setCustomerId(saleOrder.getCustomerId());
        calculateTotalPrice(saleReturn, saleReturnItems);
        saleReturnMapper.insert(saleReturn);
        // 2.2 插入退货项
        saleReturnItems.forEach(o -> o.setReturnId(saleReturn.getId()));
        saleReturnItemMapper.insertBatch(saleReturnItems);

        // 3. 更新销售订单的退货数量
        updateSaleOrderReturnCount(createReqVO.getOrderId());
        return saleReturn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleReturn(ErpSaleReturnSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpSaleReturnDO saleReturn = validateSaleReturnExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(saleReturn.getStatus())) {
            throw exception(SALE_RETURN_UPDATE_FAIL_APPROVE, saleReturn.getNo());
        }
        // 1.2 校验销售订单已审核
        ErpSaleOrderDO saleOrder = saleOrderService.validateSaleOrder(updateReqVO.getOrderId());
        // 1.3 校验结算账户
        accountService.validateAccount(updateReqVO.getAccountId());
        // 1.4 校验销售人员
        if (updateReqVO.getSaleUserId() != null) {
            adminUserApi.validateUser(updateReqVO.getSaleUserId());
        }
        // 1.5 校验订单项的有效性
        List<ErpSaleReturnItemDO> saleReturnItems = validateSaleReturnItems(updateReqVO.getItems());

        // 2.1 更新退货
        ErpSaleReturnDO updateObj = BeanUtils.toBean(updateReqVO, ErpSaleReturnDO.class)
                .setOrderNo(saleOrder.getNo()).setCustomerId(saleOrder.getCustomerId());
        calculateTotalPrice(updateObj, saleReturnItems);
        saleReturnMapper.updateById(updateObj);
        // 2.2 更新退货项
        updateSaleReturnItemList(updateReqVO.getId(), saleReturnItems);

        // 3.1 更新销售订单的出库数量
        updateSaleOrderReturnCount(updateObj.getOrderId());
        // 3.2 注意：如果销售订单编号变更了，需要更新“老”销售订单的出库数量
        if (ObjectUtil.notEqual(saleReturn.getOrderId(), updateObj.getOrderId())) {
            updateSaleOrderReturnCount(saleReturn.getOrderId());
        }
    }

    private void calculateTotalPrice(ErpSaleReturnDO saleReturn, List<ErpSaleReturnItemDO> saleReturnItems) {
        saleReturn.setTotalCount(getSumValue(saleReturnItems, ErpSaleReturnItemDO::getCount, BigDecimal::add));
        saleReturn.setTotalProductPrice(getSumValue(saleReturnItems, ErpSaleReturnItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        saleReturn.setTotalTaxPrice(getSumValue(saleReturnItems, ErpSaleReturnItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        saleReturn.setTotalPrice(saleReturn.getTotalProductPrice().add(saleReturn.getTotalTaxPrice()));
        // 计算优惠价格
        if (saleReturn.getDiscountPercent() == null) {
            saleReturn.setDiscountPercent(BigDecimal.ZERO);
        }
        saleReturn.setDiscountPrice(MoneyUtils.priceMultiplyPercent(saleReturn.getTotalPrice(), saleReturn.getDiscountPercent()));
        saleReturn.setTotalPrice(saleReturn.getTotalPrice().subtract(saleReturn.getDiscountPrice().add(saleReturn.getOtherPrice())));
    }

    private void updateSaleOrderReturnCount(Long orderId) {
        // 1.1 查询销售订单对应的销售出库单列表
        List<ErpSaleReturnDO> saleReturns = saleReturnMapper.selectListByOrderId(orderId);
        // 1.2 查询对应的销售订单项的退货数量
        Map<Long, BigDecimal> returnCountMap = saleReturnItemMapper.selectOrderItemCountSumMapByReturnIds(
                convertList(saleReturns, ErpSaleReturnDO::getId));
        // 2. 更新销售订单的出库数量
        saleOrderService.updateSaleOrderReturnCount(orderId, returnCountMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleReturnStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpSaleReturnDO saleReturn = validateSaleReturnExists(id);
        // 1.2 校验状态
        if (saleReturn.getStatus().equals(status)) {
            throw exception(approve ? SALE_RETURN_APPROVE_FAIL : SALE_RETURN_PROCESS_FAIL);
        }
        // 1.3 校验已退款
        if (!approve && saleReturn.getRefundPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(SALE_RETURN_PROCESS_FAIL_EXISTS_REFUND);
        }

        // 2. 更新状态
        int updateCount = saleReturnMapper.updateByIdAndStatus(id, saleReturn.getStatus(),
                new ErpSaleReturnDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? SALE_RETURN_APPROVE_FAIL : SALE_RETURN_PROCESS_FAIL);
        }

        // 3. 变更库存
        List<ErpSaleReturnItemDO> saleReturnItems = saleReturnItemMapper.selectListByReturnId(id);
        Integer bizType = approve ? ErpStockRecordBizTypeEnum.SALE_RETURN.getType()
                : ErpStockRecordBizTypeEnum.SALE_RETURN_CANCEL.getType();
        saleReturnItems.forEach(saleReturnItem -> {
            BigDecimal count = approve ? saleReturnItem.getCount() : saleReturnItem.getCount().negate();
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    saleReturnItem.getProductId(), saleReturnItem.getWarehouseId(), count,
                    bizType, saleReturnItem.getReturnId(), saleReturnItem.getId(), saleReturn.getNo()));
        });
    }

    @Override
    public void updateSaleReturnRefundPrice(Long id, BigDecimal refundPrice) {
        ErpSaleReturnDO saleReturn = saleReturnMapper.selectById(id);
        if (saleReturn.getRefundPrice().equals(refundPrice)) {
            return;
        }
        if (refundPrice.compareTo(saleReturn.getTotalPrice()) > 0) {
            throw exception(SALE_RETURN_FAIL_REFUND_PRICE_EXCEED, refundPrice, saleReturn.getTotalPrice());
        }
        saleReturnMapper.updateById(new ErpSaleReturnDO().setId(id).setRefundPrice(refundPrice));
    }

    private List<ErpSaleReturnItemDO> validateSaleReturnItems(List<ErpSaleReturnSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpSaleReturnSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 转化为 ErpSaleReturnItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpSaleReturnItemDO.class, item -> {
            item.setProductUnitId(productMap.get(item.getProductId()).getUnitId());
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()));
            if (item.getTotalPrice() == null) {
                return;
            }
            if (item.getTaxPercent() != null) {
                item.setTaxPrice(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxPercent()));
            }
        }));
    }

    private void updateSaleReturnItemList(Long id, List<ErpSaleReturnItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpSaleReturnItemDO> oldList = saleReturnItemMapper.selectListByReturnId(id);
        List<List<ErpSaleReturnItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setReturnId(id));
            saleReturnItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            saleReturnItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            saleReturnItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpSaleReturnItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleReturn(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpSaleReturnDO> saleReturns = saleReturnMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(saleReturns)) {
            return;
        }
        saleReturns.forEach(saleReturn -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(saleReturn.getStatus())) {
                throw exception(SALE_RETURN_DELETE_FAIL_APPROVE, saleReturn.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        saleReturns.forEach(saleReturn -> {
            // 2.1 删除订单
            saleReturnMapper.deleteById(saleReturn.getId());
            // 2.2 删除订单项
            saleReturnItemMapper.deleteByReturnId(saleReturn.getId());

            // 2.3 更新销售订单的出库数量
            updateSaleOrderReturnCount(saleReturn.getOrderId());
        });

    }

    private ErpSaleReturnDO validateSaleReturnExists(Long id) {
        ErpSaleReturnDO saleReturn = saleReturnMapper.selectById(id);
        if (saleReturn == null) {
            throw exception(SALE_RETURN_NOT_EXISTS);
        }
        return saleReturn;
    }

    @Override
    public ErpSaleReturnDO getSaleReturn(Long id) {
        return saleReturnMapper.selectById(id);
    }

    @Override
    public ErpSaleReturnDO validateSaleReturn(Long id) {
        ErpSaleReturnDO saleReturn = validateSaleReturnExists(id);
        if (ObjectUtil.notEqual(saleReturn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(SALE_RETURN_NOT_APPROVE);
        }
        return saleReturn;
    }

    @Override
    public PageResult<ErpSaleReturnDO> getSaleReturnPage(ErpSaleReturnPageReqVO pageReqVO) {
        return saleReturnMapper.selectPage(pageReqVO);
    }

    // ==================== 销售退货项 ====================

    @Override
    public List<ErpSaleReturnItemDO> getSaleReturnItemListByReturnId(Long returnId) {
        return saleReturnItemMapper.selectListByReturnId(returnId);
    }

    @Override
    public List<ErpSaleReturnItemDO> getSaleReturnItemListByReturnIds(Collection<Long> returnIds) {
        if (CollUtil.isEmpty(returnIds)) {
            return Collections.emptyList();
        }
        return saleReturnItemMapper.selectListByReturnIds(returnIds);
    }

}