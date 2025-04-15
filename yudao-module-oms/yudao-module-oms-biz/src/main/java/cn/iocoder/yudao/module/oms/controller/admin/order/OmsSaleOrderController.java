package cn.iocoder.yudao.module.oms.controller.admin.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductRespDTO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderRespVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductItemDO;
import cn.iocoder.yudao.module.oms.service.OmsOrderService;
import cn.iocoder.yudao.module.oms.service.OmsShopProductItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - OMS 销售订单")
@RestController
@RequestMapping("/oms/sale-order")
@Validated
public class OmsSaleOrderController {

    @Resource
    private OmsOrderService omsOrderService;
    @Resource
    private OmsShopProductItemService omsShopProductItemService;

    @Resource
    private ErpProductApi erpProductApi;

    @GetMapping("/page")
    @Operation(summary = "获得销售订单分页")
    @PreAuthorize("@ss.hasPermission('oms:sale-out:query')")
    public CommonResult<PageResult<OmsOrderRespVO>> getSaleOrderPage(@Valid OmsOrderPageReqVO pageReqVO) {
        PageResult<OmsOrderDO> pageResult = omsOrderService.getOrderPage(pageReqVO);
        return success(buildSaleOrderVOPageResult(pageResult));
    }

//    private PageResult<OmsOrderRespVO> buildSaleOrderVOPageResult(PageResult<OmsOrderDO> pageResult) {
//        if (CollUtil.isEmpty(pageResult.getList())) {
//            return PageResult.empty(pageResult.getTotal());
//        }
//        PageResult<OmsOrderRespVO> allPageResult = new PageResult<>();
//        List<OmsOrderRespVO> allList = new ArrayList<>();
//
//        // 1.1 订单项
//        List<OmsOrderItemDO> saleOrderItemList = omsOrderService.getSaleOrderItemListByOrderIds(
//            convertSet(pageResult.getList(), OmsOrderDO::getId));
//        Map<Long, List<OmsOrderItemDO>> saleOrderItemMap = convertMultiMap(saleOrderItemList, OmsOrderItemDO::getOrderId);
//        //1.2 产品信息
//        saleOrderItemMap.keySet().forEach(orderId -> {
//            List<OmsOrderItemDO> saleOrderItems = saleOrderItemMap.get(orderId);
//            List<String> shopProductCodes = saleOrderItems.stream().map(OmsOrderItemDO::getShopProductCode).toList();
//            List<OmsShopProductItemDO> shopProductItemList = omsShopProductItemService.getShopProductItemsByShopProductCodes(shopProductCodes);
//            List<Long> productIds = shopProductItemList.stream().map(OmsShopProductItemDO::getProductId).distinct().toList();
//            Map<Long, ErpProductRespDTO> productMap = erpProductApi.getProductDTOMap(productIds);
//            PageResult<OmsOrderRespVO> bean = BeanUtils.toBean(pageResult, OmsOrderRespVO.class, saleOrder -> {
//                List<OmsOrderRespVO.Item> items = new ArrayList<>();
//                for (OmsShopProductItemDO omsShopProductItemDO : shopProductItemList) {
//                    OmsOrderRespVO.Item item = new OmsOrderRespVO.Item();
//                    item.setProductId(omsShopProductItemDO.getProductId());
//                    item.setProductName(productMap.getOrDefault(omsShopProductItemDO.getProductId(), null).getName());
//                    item.setProductBarCode(productMap.getOrDefault(omsShopProductItemDO.getProductId(), null).getBarCode());
//                    item.setProductUnitName(productMap.getOrDefault(omsShopProductItemDO.getProductId(), null).getUnitName());
//                    items.add(item);
//                }
//                saleOrder.setItems(items);
//                saleOrder.setProductNames(CollUtil.join(items, "，", OmsOrderRespVO.Item::getProductName));
//            });
//            allList.addAll(bean.getList());
//        });
//        allPageResult.setTotal(pageResult.getTotal());
//        allPageResult.setList(allList);
//
////
////        // 1.2 产品信息
////        List<String> shopProductCodes = saleOrderItemList.stream().map(OmsOrderItemDO::getShopProductCode).toList();
////        List<OmsShopProductItemDO> shopProductItemList = omsShopProductItemService.getShopProductItemsByShopProductCodes(shopProductCodes);
////        List<Long> productIds = shopProductItemList.stream().map(OmsShopProductItemDO::getProductId).distinct().toList();
////        Map<Long, ErpProductRespDTO> productMap = erpProductApi.getProductDTOMap(productIds);
////
////        PageResult<OmsOrderRespVO> bean = BeanUtils.toBean(pageResult, OmsOrderRespVO.class, saleOrder -> {
////            List<OmsOrderRespVO.Item> items = new ArrayList<>();
////            for (OmsShopProductItemDO omsShopProductItemDO : shopProductItemList) {
////                OmsOrderRespVO.Item item = new OmsOrderRespVO.Item();
////                item.setProductId(omsShopProductItemDO.getProductId());
////                item.setProductName(productMap.getOrDefault(omsShopProductItemDO.getProductId(), null).getName());
////                item.setProductBarCode(productMap.getOrDefault(omsShopProductItemDO.getProductId(), null).getBarCode());
////                item.setProductUnitName(productMap.getOrDefault(omsShopProductItemDO.getProductId(), null).getUnitName());
////                items.add(item);
////            }
////            saleOrder.setItems(items);
////            saleOrder.setProductNames(CollUtil.join(items, "，", OmsOrderRespVO.Item::getProductName));
////        });
//
////        // 2. 开始拼接
////        PageResult<OmsOrderRespVO> bean1  = BeanUtils.toBean(pageResult, OmsOrderRespVO.class, saleOrder -> {
////            saleOrder.setItems(BeanUtils.toBean(saleOrderItemMap.get(saleOrder.getId()), OmsOrderRespVO.Item.class,
////                item -> MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
////                    .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()))));
////            saleOrder.setProductNames(CollUtil.join(saleOrder.getItems(), "，", OmsOrderRespVO.Item::getProductName));
////        });
//
//        return allPageResult;
//    }


    private PageResult<OmsOrderRespVO> buildSaleOrderVOPageResult(PageResult<OmsOrderDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        PageResult<OmsOrderRespVO> allPageResult = new PageResult<>();
        allPageResult.setTotal(pageResult.getTotal());
        allPageResult.setList(new ArrayList<>());
        List<OmsOrderDO> saleOrderList = pageResult.getList();
        for (OmsOrderDO omsOrderDO : saleOrderList) {
            OmsOrderRespVO omsOrderRespVO = BeanUtils.toBean(omsOrderDO, OmsOrderRespVO.class);
            // 1.1 订单项
            List<OmsOrderItemDO> saleOrderItemList = omsOrderService.getSaleOrderItemListByOrderIds(List.of(omsOrderDO.getId()));
            Map<Long, List<OmsOrderItemDO>> saleOrderItemMap = new HashMap<>();
            saleOrderItemMap.put(omsOrderDO.getId(), saleOrderItemList);

            List<OmsOrderItemDO> saleOrderItems = saleOrderItemMap.get(omsOrderDO.getId());
            List<String> shopProductCodes = saleOrderItems.stream().map(OmsOrderItemDO::getShopProductCode).toList();
            List<OmsShopProductItemDO> shopProductItemList = omsShopProductItemService.getShopProductItemsByShopProductCodes(shopProductCodes);
            List<Long> productIds = shopProductItemList.stream().map(OmsShopProductItemDO::getProductId).distinct().toList();
            Map<Long, ErpProductRespDTO> productMap = erpProductApi.getProductDTOMap(productIds);
            List<OmsOrderRespVO.Item> items = new ArrayList<>();
            productMap.forEach((productId, erpProductRespDTO) -> {
                OmsOrderRespVO.Item item = new OmsOrderRespVO.Item();
                item.setProductId(productId);
                item.setProductName(erpProductRespDTO.getName());
                item.setProductBarCode(erpProductRespDTO.getBarCode());
                item.setProductUnitName(erpProductRespDTO.getUnitName());
                items.add(item);
            });
            omsOrderRespVO.setProductNames(CollUtil.join(items, "，", OmsOrderRespVO.Item::getProductName));
            omsOrderRespVO.setItems(items);
            allPageResult.getList().add(omsOrderRespVO);
        }
        return allPageResult;
    }
}