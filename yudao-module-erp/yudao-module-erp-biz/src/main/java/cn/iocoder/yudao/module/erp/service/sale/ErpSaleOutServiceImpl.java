package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOutItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOutMapper;
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
 * ERP 销售出库 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpSaleOutServiceImpl implements ErpSaleOutService {

    @Resource
    private ErpSaleOutMapper saleOutMapper;
    @Resource
    private ErpSaleOutItemMapper saleOutItemMapper;

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
    public Long createSaleOut(ErpSaleOutSaveReqVO createReqVO) {
        // 1.1 校验销售订单已审核
        ErpSaleOrderDO saleOrder = saleOrderService.validateSaleOrder(createReqVO.getOrderId());
        // 1.2 校验出库项的有效性
        List<ErpSaleOutItemDO> saleOutItems = validateSaleOutItems(createReqVO.getItems());
        // 1.3 校验结算账户
        accountService.validateAccount(createReqVO.getAccountId());
        // 1.4 校验销售人员
        if (createReqVO.getSaleUserId() != null) {
            adminUserApi.validateUser(createReqVO.getSaleUserId());
        }
        // 1.5 生成出库单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_OUT_NO_PREFIX);
        if (saleOutMapper.selectByNo(no) != null) {
            throw exception(SALE_OUT_NO_EXISTS);
        }

        // 2.1 插入出库
        ErpSaleOutDO saleOut = BeanUtils.toBean(createReqVO, ErpSaleOutDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus()))
                .setOrderNo(saleOrder.getNo()).setCustomerId(saleOrder.getCustomerId());
        calculateTotalPrice(saleOut, saleOutItems);
        saleOutMapper.insert(saleOut);
        // 2.2 插入出库项
        saleOutItems.forEach(o -> o.setOutId(saleOut.getId()));
        saleOutItemMapper.insertBatch(saleOutItems);

        // 3. 更新销售订单的出库数量
        updateSaleOrderOutCount(createReqVO.getOrderId());
        return saleOut.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOut(ErpSaleOutSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpSaleOutDO saleOut = validateSaleOutExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(saleOut.getStatus())) {
            throw exception(SALE_OUT_UPDATE_FAIL_APPROVE, saleOut.getNo());
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
        List<ErpSaleOutItemDO> saleOutItems = validateSaleOutItems(updateReqVO.getItems());

        // 2.1 更新出库
        ErpSaleOutDO updateObj = BeanUtils.toBean(updateReqVO, ErpSaleOutDO.class)
                .setOrderNo(saleOrder.getNo()).setCustomerId(saleOrder.getCustomerId());
        calculateTotalPrice(updateObj, saleOutItems);
        saleOutMapper.updateById(updateObj);
        // 2.2 更新出库项
        updateSaleOutItemList(updateReqVO.getId(), saleOutItems);

        // 3.1 更新销售订单的出库数量
        updateSaleOrderOutCount(updateObj.getOrderId());
        // 3.2 注意：如果销售订单编号变更了，需要更新“老”销售订单的出库数量
        if (ObjectUtil.notEqual(saleOut.getOrderId(), updateObj.getOrderId())) {
            updateSaleOrderOutCount(saleOut.getOrderId());
        }
    }

    private void calculateTotalPrice(ErpSaleOutDO saleOut, List<ErpSaleOutItemDO> saleOutItems) {
        saleOut.setTotalCount(getSumValue(saleOutItems, ErpSaleOutItemDO::getCount, BigDecimal::add));
        saleOut.setTotalProductPrice(getSumValue(saleOutItems, ErpSaleOutItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        saleOut.setTotalTaxPrice(getSumValue(saleOutItems, ErpSaleOutItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        saleOut.setTotalPrice(saleOut.getTotalProductPrice().add(saleOut.getTotalTaxPrice()));
        // 计算优惠价格
        if (saleOut.getDiscountPercent() == null) {
            saleOut.setDiscountPercent(BigDecimal.ZERO);
        }
        saleOut.setDiscountPrice(MoneyUtils.priceMultiplyPercent(saleOut.getTotalPrice(), saleOut.getDiscountPercent()));
        saleOut.setTotalPrice(saleOut.getTotalPrice().subtract(saleOut.getDiscountPrice().add(saleOut.getOtherPrice())));
    }

    private void updateSaleOrderOutCount(Long orderId) {
        // 1.1 查询销售订单对应的销售出库单列表
        List<ErpSaleOutDO> saleOuts = saleOutMapper.selectListByOrderId(orderId);
        // 1.2 查询对应的销售订单项的出库数量
        Map<Long, BigDecimal> returnCountMap = saleOutItemMapper.selectOrderItemCountSumMapByOutIds(
                convertList(saleOuts, ErpSaleOutDO::getId));
        // 2. 更新销售订单的出库数量
        saleOrderService.updateSaleOrderOutCount(orderId, returnCountMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOutStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpSaleOutDO saleOut = validateSaleOutExists(id);
        // 1.2 校验状态
        if (saleOut.getStatus().equals(status)) {
            throw exception(approve ? SALE_OUT_APPROVE_FAIL : SALE_OUT_PROCESS_FAIL);
        }
        // 1.3 校验已退款
        if (!approve && saleOut.getReceiptPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(SALE_OUT_PROCESS_FAIL_EXISTS_RECEIPT);
        }

        // 2. 更新状态
        int updateCount = saleOutMapper.updateByIdAndStatus(id, saleOut.getStatus(),
                new ErpSaleOutDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? SALE_OUT_APPROVE_FAIL : SALE_OUT_PROCESS_FAIL);
        }

        // 3. 变更库存
        List<ErpSaleOutItemDO> saleOutItems = saleOutItemMapper.selectListByOutId(id);
        Integer bizType = approve ? ErpStockRecordBizTypeEnum.SALE_OUT.getType()
                : ErpStockRecordBizTypeEnum.SALE_OUT_CANCEL.getType();
        saleOutItems.forEach(saleOutItem -> {
            BigDecimal count = approve ? saleOutItem.getCount().negate() : saleOutItem.getCount();
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    saleOutItem.getProductId(), saleOutItem.getWarehouseId(), count,
                    bizType, saleOutItem.getOutId(), saleOutItem.getId(), saleOut.getNo()));
        });
    }

    @Override
    public void updateSaleInReceiptPrice(Long id, BigDecimal receiptPrice) {
        ErpSaleOutDO saleOut = saleOutMapper.selectById(id);
        if (saleOut.getReceiptPrice().equals(receiptPrice)) {
            return;
        }
        if (receiptPrice.compareTo(saleOut.getTotalPrice()) > 0) {
            throw exception(SALE_OUT_FAIL_RECEIPT_PRICE_EXCEED, receiptPrice,  saleOut.getTotalPrice());
        }
        saleOutMapper.updateById(new ErpSaleOutDO().setId(id).setReceiptPrice(receiptPrice));
    }

    private List<ErpSaleOutItemDO> validateSaleOutItems(List<ErpSaleOutSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpSaleOutSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 转化为 ErpSaleOutItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpSaleOutItemDO.class, item -> {
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

    private void updateSaleOutItemList(Long id, List<ErpSaleOutItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpSaleOutItemDO> oldList = saleOutItemMapper.selectListByOutId(id);
        List<List<ErpSaleOutItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOutId(id));
            saleOutItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            saleOutItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            saleOutItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpSaleOutItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleOut(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpSaleOutDO> saleOuts = saleOutMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(saleOuts)) {
            return;
        }
        saleOuts.forEach(saleOut -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(saleOut.getStatus())) {
                throw exception(SALE_OUT_DELETE_FAIL_APPROVE, saleOut.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        saleOuts.forEach(saleOut -> {
            // 2.1 删除订单
            saleOutMapper.deleteById(saleOut.getId());
            // 2.2 删除订单项
            saleOutItemMapper.deleteByOutId(saleOut.getId());

            // 2.3 更新销售订单的出库数量
            updateSaleOrderOutCount(saleOut.getOrderId());
        });

    }

    private ErpSaleOutDO validateSaleOutExists(Long id) {
        ErpSaleOutDO saleOut = saleOutMapper.selectById(id);
        if (saleOut == null) {
            throw exception(SALE_OUT_NOT_EXISTS);
        }
        return saleOut;
    }

    @Override
    public ErpSaleOutDO getSaleOut(Long id) {
        return saleOutMapper.selectById(id);
    }

    @Override
    public ErpSaleOutDO validateSaleOut(Long id) {
        ErpSaleOutDO saleOut = validateSaleOutExists(id);
        if (ObjectUtil.notEqual(saleOut.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(SALE_OUT_NOT_APPROVE);
        }
        return saleOut;
    }

    @Override
    public PageResult<ErpSaleOutDO> getSaleOutPage(ErpSaleOutPageReqVO pageReqVO) {
        return saleOutMapper.selectPage(pageReqVO);
    }

    // ==================== 销售出库项 ====================

    @Override
    public List<ErpSaleOutItemDO> getSaleOutItemListByOutId(Long outId) {
        return saleOutItemMapper.selectListByOutId(outId);
    }

    @Override
    public List<ErpSaleOutItemDO> getSaleOutItemListByOutIds(Collection<Long> outIds) {
        if (CollUtil.isEmpty(outIds)) {
            return Collections.emptyList();
        }
        return saleOutItemMapper.selectListByOutIds(outIds);
    }

}