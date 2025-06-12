package cn.iocoder.yudao.module.srm.convert.purchase;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderGenerateContractReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestMergeReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.SrmPurchaseOrderBO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.SrmPurchaseOrderItemBO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.word.SrmPurchaseOrderItemWordBO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.word.SrmPurchaseOrderWordBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DatePattern.NORM_DATE_PATTERN;

@Mapper
public interface SrmOrderConvert {

    SrmOrderConvert INSTANCE = Mappers.getMapper(SrmOrderConvert.class);

    /**
     * 让采购项关联申请项 采购项:申请项 N:1
     *
     * @param itemDO          采购项
     * @param requestItemsMap 申请项Map key:申请项id value:申请项
     * @param itemDOMap       采购项Map
     * @return 采购订单的入参reqVO的item
     */
    default SrmPurchaseOrderSaveReqVO.Item convertToErpPurchaseOrderSaveReqVOItem(SrmPurchaseRequestItemsDO itemDO, Map<Long, SrmPurchaseRequestMergeReqVO.requestItems> requestItemsMap, Map<Long, SrmPurchaseRequestItemsDO> itemDOMap) {
        SrmPurchaseOrderSaveReqVO.Item item = new SrmPurchaseOrderSaveReqVO.Item();
        //SrmPurchaseRequestItemsDO ->  SrmPurchaseOrderSaveReqVO.Item
        SrmPurchaseRequestItemsDO requestItemsDO = itemDOMap.get(itemDO.getId());
        BeanUtil.copyProperties(requestItemsDO, item);
        SrmPurchaseRequestMergeReqVO.requestItems reqVO = requestItemsMap.get(itemDO.getId());
        BeanUtil.copyProperties(reqVO, item);
        item.setId(null);
        //产品项
        item.setPurchaseApplyItemId(itemDO.getId());
        //申请项id
        item.setPurchaseApplyItemId(itemDO.getId());
        // 设置下单数量(采购) == 申请项批准数量
        item.setQty(reqVO.getOrderQuantity()); //合并时vo -> 数量
        //vo 必填项 ，特殊
        item.setProductPrice(reqVO.getProductPrice());//产品含税单价->产品价格
        //价税合计
        item.setDeliveryTime(reqVO.getDeliveryTime());//交货日期 -> 期望到货日期
        return item;
    }

    //list
    default List<SrmPurchaseOrderSaveReqVO.Item> convertToErpPurchaseOrderSaveReqVOItemList(List<SrmPurchaseRequestItemsDO> itemDOList, Map<Long, SrmPurchaseRequestMergeReqVO.requestItems> requestItemsMap, Map<Long, SrmPurchaseRequestItemsDO> itemDOMap) {
        return itemDOList.stream().map(itemDO -> convertToErpPurchaseOrderSaveReqVOItem(itemDO, requestItemsMap, itemDOMap)).collect(Collectors.toList());
    }

    /**
     * 合同word 渲染BO用
     */
    default SrmPurchaseOrderWordBO bindDataFormOrderItemDO(List<SrmPurchaseOrderItemDO> itemDOS, SrmPurchaseOrderDO orderDO,
                                                           SrmPurchaseOrderGenerateContractReqVO vo, Map<Long, FmsCompanyDTO> dtoMap, SrmSupplierDO srmSupplierDO) {
        //        Set<Long> productIds = itemDOS.stream().map(SrmPurchaseOrderItemDO::getProductId).collect(Collectors.toSet());
        //        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(productIds);
        //收集产品的单位map getProductUnitMap
        //        Map<Long, ErpProductUnitDTO> unitMap = erpProductUnitApi.getProductUnitMap(productMap.values().stream().map(ErpProductDTO::getUnitId).collect(Collectors.toSet()));
        AtomicInteger index = new AtomicInteger(1);
        //单位名称
        //不含税总额
        //总金额
        //数量*含税单价
        return BeanUtils.toBean(orderDO, SrmPurchaseOrderWordBO.class, peek -> {
            BeanUtils.copyProperties(vo, peek);
            //signingDateFormat 日期格式化
            peek.setSigningDateFormat(DateUtil.format(peek.getSigningDate(), NORM_DATE_PATTERN));
            peek.setProducts(BeanUtils.toBean(itemDOS, SrmPurchaseOrderItemWordBO.class, item -> {
                item.setIndex(index.getAndIncrement());
                //不含税总额
                item.setTotalPriceUntaxed(item.getProductPrice().multiply(item.getQty()).setScale(2, RoundingMode.HALF_UP));
                item.setTotalGrossPrice(item.getTax().setScale(2, RoundingMode.HALF_UP));
                //总金额
                item.setTotalProductPrice(item.getQty().multiply(item.getGrossPrice()).setScale(2, RoundingMode.HALF_UP));//数量*含税单价
                item.setDeliveryTimeFormat(DateUtil.format(item.getDeliveryTime(), NORM_DATE_PATTERN));
                item.setQty(item.getQty().setScale(0, RoundingMode.HALF_UP));
            })).setTotalCount(peek.getTotalCount().setScale(0, RoundingMode.HALF_UP));
            //渲染甲方乙方
            peek.setA(dtoMap.get(vo.getPartyAId()));
            peek.setB(srmSupplierDO);
            //渲染起始目的港口名称
            peek.setPortOfLoading(orderDO.getFromPortName());
            peek.setPortOfDischarge(orderDO.getToPortName());
            //付款条款
            peek.setPaymentTerms(vo.getPaymentTerms());
        });
    }

    /**
     * 采购订单项BO -> 采购订单BO
     *
     * @param itemBOList 采购订单项BO集合
     * @return 采购订单BO集合
     */
    default List<SrmPurchaseOrderBO> convertToSrmPurchaseOrderBOList(List<SrmPurchaseOrderItemBO> itemBOList) {
        if (itemBOList == null) {
            return Collections.emptyList();
        }
        //订单ID : 订单项s
        Map<Long, List<SrmPurchaseOrderItemBO>> orderIdMap = itemBOList.stream().collect(Collectors.groupingBy(SrmPurchaseOrderItemBO::getOrderId));

        // 转换为订单BO列表
        return orderIdMap.values().stream().map(orderItems -> {
            if (CollUtil.isEmpty(orderItems)) {
                return null;
            }
            // 子项按创建时间降序
            orderItems.sort(
                Comparator.comparing(SrmPurchaseOrderItemBO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );

            SrmPurchaseOrderBO orderBO = new SrmPurchaseOrderBO();
            // 获取第一个子项来设置主表信息
            SrmPurchaseOrderItemBO firstItem = orderItems.get(0);
            if (firstItem.getSrmPurchaseOrderDO() == null) {
                return null;
            }
            // 设置主表基本信息
            BeanUtils.copyProperties(firstItem.getSrmPurchaseOrderDO(), orderBO);
            // 设置子表信息
            orderBO.setSrmPurchaseOrderItemDOS(BeanUtils.toBean(orderItems, SrmPurchaseOrderItemDO.class));

            return orderBO;
        }).filter(Objects::nonNull).sorted(Comparator.comparing(SrmPurchaseOrderBO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
    }
}
