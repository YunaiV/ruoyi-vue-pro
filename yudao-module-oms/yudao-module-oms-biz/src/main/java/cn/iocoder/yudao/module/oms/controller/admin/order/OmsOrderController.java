package cn.iocoder.yudao.module.oms.controller.admin.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductRespDTO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopRespVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductItemDO;
import cn.iocoder.yudao.module.oms.service.OmsOrderService;
import cn.iocoder.yudao.module.oms.service.OmsShopProductItemService;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - OMS 销售订单")
@RestController
@RequestMapping("/oms/order")
@Validated
public class OmsOrderController {

    @Resource
    private OmsOrderService omsOrderService;
    @Resource
    private OmsShopProductItemService omsShopProductItemService;

    @Resource
    private OmsShopService omsShopService;

    @Resource
    private ErpProductApi erpProductApi;

    @GetMapping("/page")
    @Operation(summary = "获得销售订单分页")
    public CommonResult<PageResult<OmsOrderRespVO>> getOrderPage(@Valid OmsOrderPageReqVO pageReqVO) {
        PageResult<OmsOrderDO> pageResult = omsOrderService.getOrderPage(pageReqVO);
        return success(buildOrderVOPageResult(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "获得订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<OmsOrderRespVO> getOrder(@RequestParam("id") Long id) {
        OmsOrderDO order = omsOrderService.getOrder(id);
        if (order == null) {
            return success(null);
        }

        OmsOrderPageReqVO vo = OmsOrderPageReqVO.builder().no(order.getNo()).shopId(order.getShopId()).sourceNo(order.getSourceNo()).build();
        PageResult<OmsOrderDO> pageResult = new PageResult<>();
        pageResult.setTotal(1L);
        pageResult.setList(List.of(order));
        PageResult<OmsOrderRespVO> result = buildOrderVOPageResult(pageResult);
        return success(result.getList().get(0));
    }


    private PageResult<OmsOrderRespVO> buildOrderVOPageResult(PageResult<OmsOrderDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }


        // 1 组装订单项 orderItemMap 中key 为订单id，value 为单个订单所对应的订单项
        List<OmsOrderItemDO> orderItemList = omsOrderService.getOrderItemListByOrderIds(convertSet(pageResult.getList(), OmsOrderDO::getId));
        Map<Long, List<OmsOrderItemDO>> orderItemMap = convertMultiMap(orderItemList, OmsOrderItemDO::getOrderId);

        // 2 组装产品信息 productMap 中key 为订单id，value 为单个订单所对应的所有产品信息
        Map<Long, List<ErpProductRespDTO>> productMap = new HashMap<>();
        List<OmsOrderDO> omsOrderList = pageResult.getList();
        for (OmsOrderDO omsOrderDO : omsOrderList) {
            //2.1 先找出单个订单所对应的订单项
            List<OmsOrderItemDO> orderItems = orderItemMap.get(omsOrderDO.getId());
            //2.2 收集单个订单的所有产品编码
            List<String> shopProductCodes = orderItems.stream().map(OmsOrderItemDO::getShopProductCode).toList();
            List<OmsShopProductItemDO> shopProductItemList = omsShopProductItemService.getShopProductItemsByShopProductCodes(shopProductCodes);
            List<Long> productIds = shopProductItemList.stream().map(OmsShopProductItemDO::getProductId).distinct().toList();
            //2.3 根据产品编码查询产品信息
            Map<Long, ErpProductRespDTO> productDTOMap = erpProductApi.getProductDTOMap(productIds);
            productMap.put(omsOrderDO.getId(), productDTOMap.values().stream().toList());
        }

        //1.3 店铺信息 shopMap 中key 为单个订单中的shop_id，value为所对应的店铺信息
        Set<Long> shopIds = omsOrderList.stream().map(OmsOrderDO::getShopId).collect(Collectors.toSet());
        Map<Long, OmsShopRespVO> shopMap = omsShopService.getShopMapByIds(shopIds);


        return BeanUtils.toBean(pageResult, OmsOrderRespVO.class, order -> {
            MapUtils.findAndThen(productMap, order.getId(), productList -> {
                order.setProductNames(CollUtil.join(productList, "，", ErpProductRespDTO::getName));

                List<OmsOrderRespVO.Item> items = productList.stream().flatMap(product -> {
                    OmsOrderRespVO.Item item = new OmsOrderRespVO.Item();
                    return Stream.of(item.setProductId(product.getId())
                        .setProductName(product.getName())
                        .setProductBarCode(product.getBarCode())
                        .setProductUnitName(product.getUnitName()));
                }).toList();
                order.setItems(items);
            });

            MapUtils.findAndThen(shopMap, order.getShopId(), shop -> {
                order.setShopName(shop.getName());
            });
        });
    }
}