package cn.iocoder.yudao.module.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderItemSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderPageReqVO;
import cn.iocoder.yudao.module.oms.convert.OmsOrderConvert;
import cn.iocoder.yudao.module.oms.convert.OmsOrderItemConvert;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderRawDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsOrderItemMapper;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsOrderMapper;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsOrderRawMapper;
import cn.iocoder.yudao.module.oms.service.OmsOrderItemService;
import cn.iocoder.yudao.module.oms.service.OmsOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_ORDER_INFO_LACK;

/**
 * OMS订单 Service 实现类
 *
 * @author 谷毛毛
 */
@Service
@Validated
@Slf4j
public class OmsOrderServiceImpl implements OmsOrderService {

    private final String CREATOR = "Admin";

    private final String ORDER_CODE_PREFIX = "XSDD-";

    @Resource
    private OmsOrderMapper orderMapper;
    @Resource
    private OmsOrderItemMapper orderItemMapper;
    @Resource
    private OmsOrderItemService omsOrderItemService;
    @Resource
    private OmsOrderRawMapper orderRawMapper;


    @Override
    public OmsOrderDO getOrder(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public PageResult<OmsOrderDO> getOrderPage(OmsOrderPageReqVO pageReqVO) {
        return orderMapper.selectPage(pageReqVO);
    }


    @Transactional
    @Override
    public void createOrUpdateOrderByPlatform(List<OmsOrderSaveReqDTO> saveReqDTOs) {
        if (CollectionUtils.isEmpty(saveReqDTOs)) {
            log.info(OMS_SYNC_ORDER_INFO_LACK.getMsg());
            return;
        }

        List<OmsOrderDO> orders = OmsOrderConvert.INSTANCE.toOmsOrderDOs(saveReqDTOs);

        List<OmsOrderDO> createOrders = new ArrayList<>();
        List<OmsOrderDO> updateOrders = new ArrayList<>();

        orders.forEach(order -> {
            //用创建者区分是否是同步过来的数据还是运营新增的数据
            order.setCreator(CREATOR);
            if (order.getId() != null) {
                updateOrders.add(order);
            } else {
                order.setCode(getOrderCode());
                createOrders.add(order);
            }
        });

        //新增订单时
        if (CollectionUtil.isNotEmpty(createOrders)) {
            orderMapper.insertBatch(createOrders);
            //备份订单数据
            List<OmsOrderRawDO> createOrderRaws = createOrders.stream().map(order -> {
                return OmsOrderRawDO.builder().orderId(order.getId())
                    .data(JsonUtilsX.toJsonString(order))
                    .build();
            }).collect(Collectors.toList());
            orderRawMapper.insertBatch(createOrderRaws);
            createOrderItems(saveReqDTOs, createOrders);
        }

        if (CollectionUtil.isNotEmpty(updateOrders)) {
            orderMapper.updateById(updateOrders);
            updateOrderItems(saveReqDTOs);
            //更新备份订单数据
            updateOrderRaws(updateOrders);
        }

        log.info("sync order success,salesPlatformCode:{},orderCount:{}", saveReqDTOs.get(0).getPlatformCode(), saveReqDTOs.size());
    }

    /**
     * 更新备份订单数据
     */
    public void updateOrderRaws(List<OmsOrderDO> updateOrders) {
        List<Long> updateOrderIds = updateOrders.stream().map(order -> order.getId()).collect(Collectors.toList());
        Map<Long, OmsOrderDO> updateOrderMap = updateOrders.stream().collect(Collectors.toMap(order -> order.getId(), order -> order));
        List<OmsOrderRawDO> updateOrderRaws = orderRawMapper.selectList(OmsOrderRawDO::getOrderId, updateOrderIds);
        updateOrderRaws.forEach(orderRaw -> {
            MapUtils.findAndThen(updateOrderMap, orderRaw.getOrderId(), omsOrderDTO -> orderRaw.setData(JsonUtilsX.toJsonString(omsOrderDTO)));
        });
        orderRawMapper.updateById(updateOrderRaws);
    }


    /**
     * @Description: 生成订单编码
     */
    public String getOrderCode() {
        String orderCode = ORDER_CODE_PREFIX + DateUtil.format(new Date(), "yyyyMMdd") + "-";
        UUID uuid = UUID.randomUUID();
        long numericValue = uuid.getMostSignificantBits() & Long.MAX_VALUE; // 避免负数
        int sixDigitNumber = (int) (numericValue % 1000000);
        String formatted = String.format("%06d", sixDigitNumber); // 补零至6位
        return orderCode + formatted;
    }


    @Override
    public List<OmsOrderDO> getByPlatformCode(String platformCode) {
        return orderMapper.getByPlatformCode(platformCode);
    }

    /**
     * @param saveReqDTOs  从平台传过来的订单数据
     * @param createOrders 已创建的订单集合
     * @Description: 新增订单时新增订单项
     */
    public void createOrderItems(List<OmsOrderSaveReqDTO> saveReqDTOs, List<OmsOrderDO> createOrders) {
        List<OmsOrderSaveReqDTO> saveReqOrders = saveReqDTOs.stream()
            .filter(saveReqDTO -> saveReqDTO.getId() == null)
            .collect(Collectors.toList());

        Map<String, OmsOrderDO> createOrderMap = createOrders.stream()
            .collect(Collectors.toMap(omsOrderDO -> omsOrderDO.getExternalCode(), omsOrderDO -> omsOrderDO));

        //取出所有订单项数据，并为其设置订单项的订单ID属性
        List<OmsOrderItemDO> omsOrderItemDOs = saveReqOrders.stream()
            .flatMap(saveReqDTO -> {
                Long orderId = createOrderMap.get(saveReqDTO.getExternalCode()).getId();
                return saveReqDTO.getOmsOrderItemSaveReqDTOList().stream()
                    .map(itemReqDTO -> {
                        OmsOrderItemDO itemDO = OmsOrderItemConvert.INSTANCE.toOmsOrderItemDO(itemReqDTO);
                        itemDO.setCreator(CREATOR);
                        itemDO.setOrderId(orderId);
                        return itemDO;
                    });
            })
            .collect(Collectors.toList());
        omsOrderItemService.saveOmsOrderItemDOList(omsOrderItemDOs);
    }


    /**
     * @param saveReqDTOs 从平台传过来的订单数据，里边包含订单项
     * @Description: 更新订单项
     * @return:
     */
    public void updateOrderItems(List<OmsOrderSaveReqDTO> saveReqDTOs) {
        // 订单id不为null就是需要更新的数据
        List<OmsOrderSaveReqDTO> updateSaveReqDTOs = saveReqDTOs.stream()
            .filter(saveReqDTO -> {
                return saveReqDTO.getId() != null;
            })
            .collect(Collectors.toList());

        //组装出orderIdToItemMap key是orderId value是一个Map<String, OmsOrderItemDO>,其中key是shopProductCode
        Map<Long, Map<Long, OmsOrderItemDO>> orderIdToItemMap = getOrderIdToItemMap(updateSaveReqDTOs);

        // key = orderId vale= 已经存在的订单项集合
        Map<Long, List<OmsOrderItemSaveReqDTO>> existOrderItemSaveReqDTOMap = Optional.ofNullable(updateSaveReqDTOs)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(OmsOrderSaveReqDTO::getId, OmsOrderSaveReqDTO -> OmsOrderSaveReqDTO.getOmsOrderItemSaveReqDTOList()));

        List<OmsOrderItemDO> createOrderItems = new ArrayList<>();
        List<OmsOrderItemDO> updateOrderItems = new ArrayList<>();
        List<OmsOrderItemDO> deleteOrderItems = new ArrayList<>();
        getCreateAndUpdateAndDeleteOrderItems(createOrderItems, updateOrderItems, deleteOrderItems, updateSaveReqDTOs, existOrderItemSaveReqDTOMap, orderIdToItemMap);


        if (CollectionUtil.isNotEmpty(deleteOrderItems)) {
            orderItemMapper.deleteByOrderIds(deleteOrderItems.stream().map(OmsOrderItemDO::getOrderId).collect(Collectors.toList()));
        }
        if (CollectionUtil.isNotEmpty(createOrderItems)) {
            orderItemMapper.insertBatch(createOrderItems);
        }
        if (CollectionUtil.isNotEmpty(updateOrderItems)) {
            orderItemMapper.updateBatch(updateOrderItems);
        }
    }

    /**
     * @param createOrderItems 新增的订单项数据集合
     *                         updateOrderItems 更新的订单项数据集合
     *                         deleteOrderItems 需要删除的订单项数据集合
     *                         updateSaveReqDTOs 需要更新的订单数据集合,包含订单项数据
     *                         existOrderItemSaveReqDTOMap key是orderId value是已经存在的订单项集合
     *                         orderIdToItemMap key是orderId value是一个Map<String, OmsOrderItemDO>,其中key是shopProductCode,value是需要更新的订单项数据
     * @Description: 获取需要新增，更新和删除的订单项数据集合
     */
    public void getCreateAndUpdateAndDeleteOrderItems(List<OmsOrderItemDO> createOrderItems,
                                                      List<OmsOrderItemDO> updateOrderItems,
                                                      List<OmsOrderItemDO> deleteOrderItems,
                                                      List<OmsOrderSaveReqDTO> updateSaveReqDTOs,
                                                      Map<Long, List<OmsOrderItemSaveReqDTO>> existOrderItemSaveReqDTOMap,
                                                      Map<Long, Map<Long, OmsOrderItemDO>> orderIdToItemMap) {
        for (OmsOrderSaveReqDTO updateSaveReqDTO : updateSaveReqDTOs) {
            Long orderId = updateSaveReqDTO.getId();
            List<OmsOrderItemSaveReqDTO> saveReqDTOList = existOrderItemSaveReqDTOMap.get(orderId);
            for (OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO : saveReqDTOList) {
                Long shopProductId = omsOrderItemSaveReqDTO.getShopProductId();
                OmsOrderItemDO omsOrderItemDO = orderIdToItemMap.get(orderId).get(shopProductId);
                if (omsOrderItemDO == null) {
                    omsOrderItemSaveReqDTO.setOrderId(orderId);
                    createOrderItems.add(OmsOrderItemConvert.INSTANCE.toOmsOrderItemDO(omsOrderItemSaveReqDTO));
                }

                if (omsOrderItemDO != null) {
                    OmsOrderItemDO updateOrderItem = OmsOrderItemConvert.INSTANCE.toOmsOrderItemDO(omsOrderItemSaveReqDTO);
                    updateOrderItem.setId(omsOrderItemDO.getId());
                    updateOrderItems.add(updateOrderItem);
                    orderIdToItemMap.get(orderId).remove(shopProductId);
                }
            }
        }

        for (Long orderId : orderIdToItemMap.keySet()) {
            Map<Long, OmsOrderItemDO> orderItemDOMap = orderIdToItemMap.get(orderId);
            deleteOrderItems.addAll(orderItemDOMap.values());
        }
    }


    /**
     * 组装出orderIdToItemMap key是orderId value是一个Map<String, OmsOrderItemDO>,其中key是shopProductCode
     *
     * @param updateSaveReqDTOs 需要更新的订单数据集合，其中包含订单项数据
     */
    public Map<Long, Map<Long, OmsOrderItemDO>> getOrderIdToItemMap(List<OmsOrderSaveReqDTO> updateSaveReqDTOs) {
        List<OmsOrderItemDO> existOrderItems = orderItemMapper.selectListByOrderIds(updateSaveReqDTOs.stream()
            .map(OmsOrderSaveReqDTO::getId)
            .collect(Collectors.toList()));

        Map<Long, List<OmsOrderItemDO>> existOrderItemMap = existOrderItems.stream().collect(Collectors.groupingBy(OmsOrderItemDO::getOrderId));

        Map<Long, Map<Long, OmsOrderItemDO>> orderIdToItemMap = new HashMap<>();
        for (Long orderId : existOrderItemMap.keySet()) {
            List<OmsOrderItemDO> omsOrderItemDOList = existOrderItemMap.get(orderId);
            Map<Long, OmsOrderItemDO> orderItemDOMap = new HashMap();
            for (OmsOrderItemDO omsOrderItemDO : omsOrderItemDOList) {
                orderItemDOMap.put(omsOrderItemDO.getShopProductId(), omsOrderItemDO);
            }
            orderIdToItemMap.put(orderId, orderItemDOMap);
        }
        return orderIdToItemMap;
    }


    @Override
    public List<OmsOrderItemDO> getOrderItemListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return orderItemMapper.selectListByOrderIds(orderIds);
    }

}