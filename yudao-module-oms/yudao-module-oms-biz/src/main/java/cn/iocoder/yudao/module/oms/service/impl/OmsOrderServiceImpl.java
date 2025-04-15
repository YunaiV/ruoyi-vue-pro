package cn.iocoder.yudao.module.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderItemSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.order.vo.OmsOrderSaveReqVO;
import cn.iocoder.yudao.module.oms.convert.OmsOrderConvert;
import cn.iocoder.yudao.module.oms.convert.OmsOrderItemConvert;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsOrderItemMapper;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsOrderMapper;
import cn.iocoder.yudao.module.oms.service.OmsOrderItemService;
import cn.iocoder.yudao.module.oms.service.OmsOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.*;
import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;
import static com.baomidou.mybatisplus.extension.toolkit.Db.updateBatchById;

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

    @Resource
    private OmsOrderMapper orderMapper;
    @Resource
    private OmsOrderItemMapper orderItemMapper;
    @Resource
    private OmsOrderItemService omsOrderItemService;


    @Override
    public Long createOrder(OmsOrderSaveReqVO createReqVO) {
        // 插入
        OmsOrderDO order = BeanUtils.toBean(createReqVO, OmsOrderDO.class);
        orderMapper.insert(order);
        // 返回
        return order.getId();
    }

    @Override
    public void updateOrder(OmsOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateOrderExists(updateReqVO.getId());
        // 更新
        OmsOrderDO updateObj = BeanUtils.toBean(updateReqVO, OmsOrderDO.class);
        orderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        // 校验存在
        validateOrderExists(id);
        // 删除
        orderMapper.deleteById(id);

        // 删除子表
        deleteOrderItemByOrderId(id);
    }

    private void validateOrderExists(Long id) {
        if (orderMapper.selectById(id) == null) {
            throw exception(OMS_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public OmsOrderDO getOrder(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public PageResult<OmsOrderDO> getOrderPage(OmsOrderPageReqVO pageReqVO) {
        return orderMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（OMS订单项） ====================

    @Override
    public PageResult<OmsOrderItemDO> getOrderItemPage(PageParam pageReqVO, Long orderId) {
        return orderItemMapper.selectPage(pageReqVO, orderId);
    }

    @Override
    public Long createOrderItem(OmsOrderItemDO orderItem) {
        orderItemMapper.insert(orderItem);
        return orderItem.getId();
    }

    @Override
    public void updateOrderItem(OmsOrderItemDO orderItem) {
        // 校验存在
        validateOrderItemExists(orderItem.getId());
        // 更新
        orderItem.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
        orderItemMapper.updateById(orderItem);
    }

    @Override
    public void deleteOrderItem(Long id) {
        // 校验存在
        validateOrderItemExists(id);
        // 删除
        orderItemMapper.deleteById(id);
    }

    @Override
    public OmsOrderItemDO getOrderItem(Long id) {
        return orderItemMapper.selectById(id);
    }

    @Override
    public void createOrUpdateOrderByPlatform(List<OmsOrderSaveReqDTO> saveReqDTOs) {
        if (CollectionUtils.isEmpty(saveReqDTOs)) {
            throw exception(OMS_SYNC_ORDER_INFO_LACK);
        }

        List<OmsOrderDO> orders = OmsOrderConvert.INSTANCE.toOmsOrderDOs(saveReqDTOs);

        List<OmsOrderDO> createOrders = new ArrayList<>();
        List<OmsOrderDO> updateOrders = new ArrayList<>();

        List<OmsOrderDO> existOrders = getByPlatformCode(saveReqDTOs.get(0).getPlatformCode());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDO -> omsOrderDO.getSourceNo(), omsOrderDO -> omsOrderDO));

        orders.forEach(order -> {
            //用创建者区分是否是同步过来的数据还是运营新增的数据
            order.setCreator(CREATOR);
            OmsOrderDO existOrder = existOrderMap.get(order.getSourceNo());
            if (existOrder != null) {
                order.setId(existOrder.getId());
                updateOrders.add(order);
            } else {
                // 新增
                createOrders.add(order);
            }
        });

        //新增订单时
        if (CollectionUtil.isNotEmpty(createOrders)) {
            saveBatch(createOrders);
            createOrderItems(saveReqDTOs, createOrders);
        }

        if (CollectionUtil.isNotEmpty(updateOrders)) {
            updateBatchById(updateOrders);
            updateOrderItems(saveReqDTOs, existOrderMap);
        }

        log.info("sync order success,salesPlatformCode:{},orderCount:{}", saveReqDTOs.get(0).getPlatformCode(), saveReqDTOs.size());
    }

    private void validateOrderItemExists(Long id) {
        if (orderItemMapper.selectById(id) == null) {
            throw exception(OMS_ORDER_ITEM_NOT_EXISTS);
        }
    }

    private void deleteOrderItemByOrderId(Long orderId) {
        orderItemMapper.deleteByOrderId(orderId);
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
        List<OmsOrderDO> existOrders = getByPlatformCode(saveReqDTOs.get(0).getPlatformCode());
        Set<String> sourceNos = createOrders.stream().map(OmsOrderDO::getSourceNo).collect(Collectors.toSet());
        // 过滤出刚新增的订单
        Map<String, OmsOrderDO> createdOrders = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .filter(omsOrderDO -> sourceNos.contains(omsOrderDO.getSourceNo()))
            .collect(Collectors.toMap(omsOrderDO -> omsOrderDO.getSourceNo(), omsOrderDO -> omsOrderDO));

        //取出所有订单项数据，并为其设置订单项的订单ID属性
        List<OmsOrderItemDO> omsOrderItemDOs = saveReqDTOs.stream()
            .flatMap(saveReqDTO -> {
                Long orderId = createdOrders.get(saveReqDTO.getSourceNo()).getId();
                return saveReqDTO.getOmsOrderItemSaveReqDTOList().stream()
                    .map(itemReqDTO -> {
                        OmsOrderItemDO itemDO = OmsOrderItemConvert.INSTANCE.toOmsOrderItemDO(itemReqDTO);
                        itemDO.setOrderId(orderId);
                        return itemDO;
                    });
            })
            .collect(Collectors.toList());
        omsOrderItemService.saveOmsOrderItemDOList(omsOrderItemDOs);
    }


    /**
     * @param saveReqDTOs   从平台传过来的订单数据
     * @param existOrderMap 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
     * @Description: 更新订单项目 先删除后新增
     */
    public void updateOrderItems(List<OmsOrderSaveReqDTO> saveReqDTOs, Map<String, OmsOrderDO> existOrderMap) {
        List<Long> deleteOrderIds = new ArrayList<>();
        List<OmsOrderItemDO> orderItems = new ArrayList<>();

        for (OmsOrderSaveReqDTO saveReqDTO : saveReqDTOs) {
            Long orderId = existOrderMap.get(saveReqDTO.getSourceNo()).getId();
            for (OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO : saveReqDTO.getOmsOrderItemSaveReqDTOList()) {
                omsOrderItemSaveReqDTO.setOrderId(orderId);
                OmsOrderItemDO omsOrderItemDO = OmsOrderItemConvert.INSTANCE.toOmsOrderItemDO(omsOrderItemSaveReqDTO);
                deleteOrderIds.add(orderId);
                orderItems.add(omsOrderItemDO);
            }
        }
        omsOrderItemService.deleteByOrderIds(deleteOrderIds);
        omsOrderItemService.saveOmsOrderItemDOList(orderItems);
    }


    @Override
    public List<OmsOrderItemDO> getSaleOrderItemListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return orderItemMapper.selectListByOrderIds(orderIds);
    }

}