package cn.iocoder.yudao.module.erp.convert.purchase;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.finance.dto.ErpFinanceSubjectDTO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderGenerateContractReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestOrderReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.service.purchase.bo.ErpPurchaseOrderItemBO;
import cn.iocoder.yudao.module.erp.service.purchase.bo.ErpPurchaseOrderWordBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DatePattern.NORM_DATE_PATTERN;

@Mapper
public interface ErpOrderConvert {
    ErpOrderConvert INSTANCE = Mappers.getMapper(ErpOrderConvert.class);


    /**
     * 让采购项关联申请项 采购项:申请项 N:1
     *
     * @param itemDO          采购项
     * @param requestItemsMap ?
     * @param itemDOMap       采购项Map
     * @return 采购订单的入参reqVO的item
     */
    default ErpPurchaseOrderSaveReqVO.Item convertToErpPurchaseOrderSaveReqVOItem(ErpPurchaseRequestItemsDO itemDO, Map<Long, ErpPurchaseRequestOrderReqVO.requestItems> requestItemsMap, Map<Long, ErpPurchaseRequestItemsDO> itemDOMap) {
        ErpPurchaseOrderSaveReqVO.Item item = new ErpPurchaseOrderSaveReqVO.Item();
        BeanUtil.copyProperties(itemDOMap.get(itemDO.getId()), item);
        item.setId(null);
        // 设置产品ID
        item.setProductId(itemDO.getProductId());
        item.setProductPrice(itemDO.getActTaxPrice());
        //采购订单的申请数量
        //产品项
        item.setPurchaseApplyItemId(itemDO.getId());
        // 设置下单数量(采购) == 申请项批准数量
        item.setCount(itemDO.getApproveCount());
        //价税合计
        // 自动映射其他属性
        item.setDeliveryTime(itemDOMap.get(itemDO.getId()).getExpectArrivalDate());
        item.setProductPrice(itemDO.getActTaxPrice());//含税单价 -> 产品单价
        item.setDeliveryTime(itemDO.getExpectArrivalDate());//交货日期 -> 期望到货日期
        //
        //申请项id
        item.setPurchaseApplyItemId(itemDO.getId());
        return item;
    }

    //list
    default List<ErpPurchaseOrderSaveReqVO.Item> convertToErpPurchaseOrderSaveReqVOItemList(List<ErpPurchaseRequestItemsDO> itemDOList, Map<Long, ErpPurchaseRequestOrderReqVO.requestItems> requestItemsMap, Map<Long, ErpPurchaseRequestItemsDO> itemDOMap) {
        return itemDOList.stream().map(itemDO -> convertToErpPurchaseOrderSaveReqVOItem(itemDO, requestItemsMap, itemDOMap)).collect(Collectors.toList());
    }

    //ErpPurchaseOrderSaveReqVO.Item->ErpPurchaseOrderItemDO

//    ErpPurchaseOrderItemDO convertToErpPurchaseOrderItemDO(ErpPurchaseOrderSaveReqVO.Item item);

    /**
     * 合同渲染BO用
     */
    default ErpPurchaseOrderWordBO bindDataFormOrderItemDO(List<ErpPurchaseOrderItemDO> itemDOS, ErpPurchaseOrderDO orderDO, ErpPurchaseOrderGenerateContractReqVO vo, Map<Long, ErpFinanceSubjectDTO> dtoMap) {
//        Set<Long> productIds = itemDOS.stream().map(ErpPurchaseOrderItemDO::getProductId).collect(Collectors.toSet());
//        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(productIds);
        //收集产品的单位map getProductUnitMap
//        Map<Long, ErpProductUnitDTO> unitMap = erpProductUnitApi.getProductUnitMap(productMap.values().stream().map(ErpProductDTO::getUnitId).collect(Collectors.toSet()));
        AtomicInteger index = new AtomicInteger(1);
        //单位名称
        //不含税总额
        //总金额
        //数量*含税单价
        return BeanUtils.toBean(orderDO, ErpPurchaseOrderWordBO.class, peek -> {
            BeanUtils.copyProperties(vo, peek);
            //signingDateFormat 日期格式化
            peek.setSigningDateFormat(DateUtil.format(peek.getSigningDate(), NORM_DATE_PATTERN));
            peek.setProducts(BeanUtils.toBean(itemDOS, ErpPurchaseOrderItemBO.class, item -> {
                item.setIndex(index.getAndIncrement());
                //不含税总额
                item.setTotalPriceUntaxed(item.getProductPrice().multiply(item.getCount()).setScale(2, RoundingMode.HALF_UP));
                item.setTotalTaxPrice(item.getTaxPrice().setScale(2, RoundingMode.HALF_UP));
                //总金额
                item.setTotalProductPrice(item.getCount().multiply(item.getActTaxPrice()).setScale(2, RoundingMode.HALF_UP));//数量*含税单价
                item.setDeliveryTimeFormat(DateUtil.format(item.getDeliveryTime(), NORM_DATE_PATTERN));
                item.setCount(item.getCount().setScale(0, RoundingMode.HALF_UP));
            })).setTotalCount(peek.getTotalCount().setScale(0, RoundingMode.HALF_UP));
            //渲染甲方乙方
            peek.setA(dtoMap.get(vo.getPartyAId()));
            peek.setB(dtoMap.get(vo.getPartyBId()));
            //付款条款
            peek.setPaymentTerms(vo.getPaymentTerms());
        });
    }
}
