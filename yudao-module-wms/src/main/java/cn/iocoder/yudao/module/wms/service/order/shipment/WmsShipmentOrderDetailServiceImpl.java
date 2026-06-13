package cn.iocoder.yudao.module.wms.service.order.shipment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDetailDO;
import cn.iocoder.yudao.module.wms.dal.mysql.order.shipment.WmsShipmentOrderDetailMapper;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 出库单明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsShipmentOrderDetailServiceImpl implements WmsShipmentOrderDetailService {

    @Resource
    private WmsShipmentOrderDetailMapper shipmentOrderDetailMapper;
    @Resource
    private WmsItemSkuService itemSkuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createShipmentOrderDetailList(Long orderId, WmsShipmentOrderSaveReqVO reqVO) {
        List<WmsShipmentOrderDetailDO> list = buildShipmentOrderDetailList(reqVO);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(detail -> detail.setId(null).setOrderId(orderId));
        shipmentOrderDetailMapper.insertBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShipmentOrderDetailList(Long orderId, WmsShipmentOrderSaveReqVO reqVO) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<WmsShipmentOrderDetailDO> oldList = shipmentOrderDetailMapper.selectListByOrderId(orderId);
        List<WmsShipmentOrderDetailDO> list = buildShipmentOrderDetailList(reqVO);
        List<WmsShipmentOrderDetailDO> newList = CollUtil.isEmpty(list) ? ListUtil.of() : list;
        List<List<WmsShipmentOrderDetailDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> ObjectUtil.equal(oldVal.getId(), newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            if (CollUtil.isNotEmpty(convertList(diffList.get(0), WmsShipmentOrderDetailDO::getId))) {
                throw exception(SHIPMENT_ORDER_DETAIL_NOT_EXISTS);
            }
            diffList.get(0).forEach(detail -> detail.setOrderId(orderId));
            shipmentOrderDetailMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            diffList.get(1).forEach(detail -> detail.setOrderId(orderId));
            shipmentOrderDetailMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            shipmentOrderDetailMapper.deleteByIds(convertList(diffList.get(2), WmsShipmentOrderDetailDO::getId));
        }
    }

    @Override
    public void deleteShipmentOrderDetailListByOrderId(Long orderId) {
        shipmentOrderDetailMapper.deleteByOrderId(orderId);
    }

    @Override
    public List<WmsShipmentOrderDetailDO> getShipmentOrderDetailList(Long orderId) {
        return shipmentOrderDetailMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<WmsShipmentOrderDetailDO> getShipmentOrderDetailList(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return ListUtil.of();
        }
        return shipmentOrderDetailMapper.selectListByOrderIds(orderIds);
    }

    @Override
    public List<WmsShipmentOrderDetailDO> validateShipmentOrderDetailListExists(Long orderId) {
        List<WmsShipmentOrderDetailDO> details = shipmentOrderDetailMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(details)) {
            throw exception(SHIPMENT_ORDER_DETAIL_REQUIRED);
        }
        return details;
    }

    @Override
    public long getShipmentOrderDetailCountBySkuId(Long skuId) {
        return shipmentOrderDetailMapper.selectCountBySkuId(skuId);
    }

    private List<WmsShipmentOrderDetailDO> buildShipmentOrderDetailList(WmsShipmentOrderSaveReqVO reqVO) {
        if (CollUtil.isEmpty(reqVO.getDetails())) {
            return ListUtil.of();
        }
        return convertList(reqVO.getDetails(), detail -> {
            // 校验 SKU 存在
            itemSkuService.validateItemSkuExists(detail.getSkuId());
            // 构建对象
            WmsShipmentOrderDetailDO detailDO = BeanUtils.toBean(detail, WmsShipmentOrderDetailDO.class)
                    .setWarehouseId(reqVO.getWarehouseId());
            fillDetailTotalPrice(detailDO);
            return detailDO;
        });
    }

    private static void fillDetailTotalPrice(WmsShipmentOrderDetailDO detail) {
        if (detail.getTotalPrice() != null || detail.getQuantity() == null || detail.getPrice() == null) {
            return;
        }
        detail.setTotalPrice(MoneyUtils.priceMultiply(detail.getPrice(), detail.getQuantity()));
    }

}
