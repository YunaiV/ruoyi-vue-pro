package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalesOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSalesOrderItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 销售订单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpSaleOrderServiceImpl implements ErpSaleOrderService {

    @Resource
    private ErpSaleOrderMapper saleOrderMapper;
    @Resource
    private ErpSalesOrderItemMapper salesOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSaleOrder(ErpSaleOrderSaveReqVO createReqVO) {
        // 插入
        ErpSaleOrderDO saleOrder = BeanUtils.toBean(createReqVO, ErpSaleOrderDO.class);
        saleOrderMapper.insert(saleOrder);

        // 插入子表
        createSalesOrderItemsList(saleOrder.getId(), createReqVO.getSalesOrderItems());
        // 返回
        return saleOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOrder(ErpSaleOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateSaleOrderExists(updateReqVO.getId());
        // 更新
        ErpSaleOrderDO updateObj = BeanUtils.toBean(updateReqVO, ErpSaleOrderDO.class);
        saleOrderMapper.updateById(updateObj);

        // 更新子表
        updateSalesOrderItemsList(updateReqVO.getId(), updateReqVO.getSalesOrderItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleOrder(Long id) {
        // 校验存在
        validateSaleOrderExists(id);
        // 删除
        saleOrderMapper.deleteById(id);

        // 删除子表
        deleteSalesOrderItemsById(id);
    }

    private void validateSaleOrderExists(Long id) {
        if (saleOrderMapper.selectById(id) == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public ErpSaleOrderDO getSaleOrder(Long id) {
        return saleOrderMapper.selectById(id);
    }

    @Override
    public PageResult<ErpSaleOrderDO> getSaleOrderPage(ErpSaleOrderPageReqVO pageReqVO) {
        return saleOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（ERP 销售订单明细） ====================

    private void createSalesOrderItemsList(Long id, List<ErpSalesOrderItemDO> list) {
        list.forEach(o -> o.setId(id));
        salesOrderItemMapper.insertBatch(list);
    }

    private void updateSalesOrderItemsList(Long id, List<ErpSalesOrderItemDO> list) {
        deleteSalesOrderItemsById(id);
		list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createSalesOrderItemsList(id, list);
    }

    private void deleteSalesOrderItemsById(Long id) {
        salesOrderItemMapper.deleteById(id);
    }

}