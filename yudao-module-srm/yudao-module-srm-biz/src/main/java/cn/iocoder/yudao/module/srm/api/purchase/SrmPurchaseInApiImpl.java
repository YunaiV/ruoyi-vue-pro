package cn.iocoder.yudao.module.srm.api.purchase;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseInDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseInItemDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.req.SrmPurchaseInSaveItemReqDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.req.SrmPurchaseInSaveReqDTO;
import cn.iocoder.yudao.module.srm.config.machine.inItem.SrmPurchaseInItemCountContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseInService;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_IN_ITEM_STORAGE_STATE_MACHINE;

/**
 * 采购入库单 API 实现类
 */
@Service
@Validated
public class SrmPurchaseInApiImpl implements SrmPurchaseInApi {

    @Autowired
    @Lazy
    private SrmPurchaseInService purchaseInService;
    @Resource(name = PURCHASE_IN_ITEM_STORAGE_STATE_MACHINE)
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseInItemCountContext> purchaseInItemStorageStateMachine;

    @Override
    public List<SrmPurchaseInDTO> getPurchaseInList(List<Long> ids) {
        // 1. 获取采购入库单列表
        List<SrmPurchaseInDO> inOrders = purchaseInService.getPurchaseInList(ids);
        if (inOrders.isEmpty()) {
            return List.of();
        }

        // 2. 获取入库明细列表
        Map<Long, List<SrmPurchaseInItemDO>> inItemMap = purchaseInService.getPurchaseInItemListByInIds(
                inOrders.stream().map(SrmPurchaseInDO::getId).collect(Collectors.toList()))
            .stream().collect(Collectors.groupingBy(SrmPurchaseInItemDO::getArriveId));

        // 3. 转换为 DTO 对象
        return inOrders.stream().map(inOrder -> {
            SrmPurchaseInDTO dto = BeanUtils.toBean(inOrder, SrmPurchaseInDTO.class);
            // 设置入库明细
            List<SrmPurchaseInItemDO> inItems = inItemMap.getOrDefault(inOrder.getId(), List.of());
            dto.setItems(inItems.stream().map(this::convertInItem).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 状态机-变动入库项-入库数量
     * <p>
     * 入库单审核后，回调
     *
     * @param reqDTO 入库单DTO
     */
    @Override
    public void updatePurchaseInItemQty(SrmPurchaseInSaveReqDTO reqDTO) {
        //校验
        if (!Objects.equals(reqDTO.getUpstreamType(), BillType.SRM_PURCHASE_IN.getValue())) {
            throw new IllegalArgumentException(StrUtil.format("入库单审核回调SrmInboundReqDTO，上游类型{}不是到货单", Objects.requireNonNull(BillType.parse(reqDTO.getUpstreamType())).getLabel()));
        }
        //校验item存在
        purchaseInService.validatePurchaseInItemExists(reqDTO.getItemList().stream().map(SrmPurchaseInSaveItemReqDTO::getUpstreamId).collect(Collectors.toList()));
        reqDTO.getItemList().forEach(item -> {
            //消费
            SrmPurchaseInItemCountContext build = SrmPurchaseInItemCountContext.builder()
                .arriveItemId(item.getUpstreamId())
                .inCount(BigDecimal.valueOf(item.getActualQty()))
                .build();
            purchaseInItemStorageStateMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STOCK_ADJUSTMENT, build);
        });
    }

    /**
     * 转换入库明细
     *
     * @param item 入库明细 DO
     * @return 入库明细 DTO
     */
    private SrmPurchaseInItemDTO convertInItem(SrmPurchaseInItemDO item) {
        if (item == null) {
            return null;
        }
        SrmPurchaseInItemDTO dto = BeanUtils.toBean(item, SrmPurchaseInItemDTO.class);
        // 特殊字段映射
        dto.setProductCode(item.getProductCode());
        dto.setMaterialId(item.getProductId());
//        dto.setMaterialCode(item.getProductCode());
        dto.setMaterialName(item.getProductName());
        dto.setUnit(item.getProductUnitName());
        dto.setQuantity(item.getQty());
        dto.setPrice(item.getProductPrice());
        dto.setAmount(item.getTotalPrice());
        dto.setTaxRate(item.getTaxRate());
        dto.setTaxAmount(item.getTax());
        dto.setGrossPrice(item.getGrossPrice());
        dto.setGrossTotalPrice(item.getTotalProductPrice());
        return dto;
    }
} 