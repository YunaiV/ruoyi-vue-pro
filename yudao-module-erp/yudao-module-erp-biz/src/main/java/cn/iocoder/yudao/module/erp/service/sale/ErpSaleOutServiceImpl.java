package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOutItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
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
 * ERP 销售订单 Service 实现类
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
    private ErpCustomerService customerService;
    @Resource
    private ErpAccountService accountService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSaleOut(ErpSaleOutSaveReqVO createReqVO) {
        // 1.1 校验订单项的有效性
        List<ErpSaleOutItemDO> saleOutItems = validateSaleOutItems(createReqVO.getItems());
        // 1.2 校验客户 TODO 芋艿：需要在瞅瞅
//        customerService.validateCustomer(createReqVO.getCustomerId());
        // 1.3 校验结算账户
        accountService.validateAccount(createReqVO.getAccountId());
        // 1.4 生成调拨单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_ORDER_NO_PREFIX);
        if (saleOutMapper.selectByNo(no) != null) {
            throw exception(SALE_ORDER_NO_EXISTS);
        }

        // 2.1 插入订单
        ErpSaleOutDO saleOut = BeanUtils.toBean(createReqVO, ErpSaleOutDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus()));
        calculateTotalPrice(saleOut, saleOutItems);
        saleOutMapper.insert(saleOut);
        // 2.2 插入订单项
        saleOutItems.forEach(o -> o.setOutId(saleOut.getId()));
        saleOutItemMapper.insertBatch(saleOutItems);
        return saleOut.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOut(ErpSaleOutSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpSaleOutDO saleOut = validateSaleOutExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(saleOut.getStatus())) {
            throw exception(SALE_ORDER_UPDATE_FAIL_APPROVE, saleOut.getNo());
        }
        // 1.2 校验客户 TODO 芋艿：需要在瞅瞅
//        customerService.validateCustomer(updateReqVO.getCustomerId());
        // 1.3 校验结算账户
        accountService.validateAccount(updateReqVO.getAccountId());
        // 1.4 校验订单项的有效性
        List<ErpSaleOutItemDO> saleOutItems = validateSaleOutItems(updateReqVO.getItems());

        // 2.1 更新订单
        ErpSaleOutDO updateObj = BeanUtils.toBean(updateReqVO, ErpSaleOutDO.class);
        calculateTotalPrice(updateObj, saleOutItems);
        saleOutMapper.updateById(updateObj);
        // 2.2 更新订单项
        updateSaleOutItemList(updateReqVO.getId(), saleOutItems);
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
        saleOut.setDiscountPrice(MoneyUtils.priceMultiply(saleOut.getTotalPrice(), saleOut.getDiscountPercent()));
        saleOut.setTotalPrice(saleOut.getTotalPrice().subtract(saleOut.getDiscountPrice()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOutStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpSaleOutDO saleOut = validateSaleOutExists(id);
        // 1.2 校验状态
        if (saleOut.getStatus().equals(status)) {
            throw exception(approve ? SALE_ORDER_APPROVE_FAIL : SALE_ORDER_PROCESS_FAIL);
        }
        // TODO @芋艿：需要校验是不是有入库、有退货

        // 2. 更新状态
        int updateCount = saleOutMapper.updateByIdAndStatus(id, saleOut.getStatus(),
                new ErpSaleOutDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? SALE_ORDER_APPROVE_FAIL : SALE_ORDER_PROCESS_FAIL);
        }
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
            item.setTaxPrice(MoneyUtils.priceMultiply(item.getTotalPrice(), item.getTaxPercent()));
            item.setTotalPrice(item.getTotalPrice().add(item.getTaxPrice()));
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
                throw exception(SALE_ORDER_DELETE_FAIL_APPROVE, saleOut.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        saleOuts.forEach(saleOut -> {
            // 2.1 删除订单
            saleOutMapper.deleteById(saleOut.getId());
            // 2.2 删除订单项
            saleOutItemMapper.deleteByOutId(saleOut.getId());
        });
    }

    private ErpSaleOutDO validateSaleOutExists(Long id) {
        ErpSaleOutDO saleOut = saleOutMapper.selectById(id);
        if (saleOut == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
        return saleOut;
    }

    @Override
    public ErpSaleOutDO getSaleOut(Long id) {
        return saleOutMapper.selectById(id);
    }

    @Override
    public PageResult<ErpSaleOutDO> getSaleOutPage(ErpSaleOutPageReqVO pageReqVO) {
        return saleOutMapper.selectPage(pageReqVO);
    }

    // ==================== 订单项 ====================

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