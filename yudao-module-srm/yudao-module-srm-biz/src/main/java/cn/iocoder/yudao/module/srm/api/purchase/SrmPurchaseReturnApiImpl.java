package cn.iocoder.yudao.module.srm.api.purchase;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseReturnDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseReturnItemDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.req.SrmReturnSaveItemReqDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.req.SrmReturnSaveReqDTO;
import cn.iocoder.yudao.module.srm.config.machine.outItem.SrmPurchaseOutItemCountContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmOutboundStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseReturnService;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_RETURN_ITEM_OUT_STORAGE_STATE_MACHINE_NAME;

/**
 * 采购退货单 API 实现类
 */
@Service
@Validated
@Slf4j
public class SrmPurchaseReturnApiImpl implements SrmPurchaseReturnApi {

    @Autowired
    @Lazy
    private SrmPurchaseReturnService purchaseReturnService;
    @Resource(name = PURCHASE_RETURN_ITEM_OUT_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutItemCountContext> srmOutboundStateMachine;

    @Override
    public List<SrmPurchaseReturnDTO> getPurchaseReturnList(List<Long> ids) {
        // 1. 获取采购退货单列表
        List<SrmPurchaseReturnDO> returnOrders = purchaseReturnService.getPurchaseReturnList(ids);
        if (returnOrders.isEmpty()) {
            return List.of();
        }

        // 2. 获取退货明细列表
        Map<Long, List<SrmPurchaseReturnItemDO>> returnItemMap = purchaseReturnService.getPurchaseReturnItemListByReturnIds(
                        returnOrders.stream().map(SrmPurchaseReturnDO::getId).collect(Collectors.toList()))
                .stream().collect(Collectors.groupingBy(SrmPurchaseReturnItemDO::getReturnId));

        // 3. 转换为 DTO 对象
        return returnOrders.stream().map(returnOrder -> {
            SrmPurchaseReturnDTO dto = new SrmPurchaseReturnDTO();
            // 3.1 设置基本信息
            dto.setId(returnOrder.getId());
            dto.setReturnNo(returnOrder.getCode());
            dto.setSupplierId(returnOrder.getSupplierId());
            dto.setReturnDate(returnOrder.getReturnTime());
            dto.setStatus(returnOrder.getAuditStatus());
            dto.setTotalAmount(returnOrder.getTotalPrice());
            dto.setTotalQuantity(returnOrder.getTotalCount());
            dto.setRemark(returnOrder.getRemark());
            dto.setCreateTime(returnOrder.getCreateTime());
            dto.setUpdateTime(returnOrder.getUpdateTime());

            // 3.2 设置退货明细
            List<SrmPurchaseReturnItemDO> returnItems = returnItemMap.getOrDefault(returnOrder.getId(), List.of());
            dto.setItems(returnItems.stream().map(this::convertReturnItem).collect(Collectors.toList()));
            
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 转换退货明细
     *
     * @param item 退货明细 DO
     * @return 退货明细 DTO
     */
    private SrmPurchaseReturnItemDTO convertReturnItem(SrmPurchaseReturnItemDO item) {
        if (item == null) {
            return null;
        }
        SrmPurchaseReturnItemDTO dto = new SrmPurchaseReturnItemDTO();
        dto.setId(item.getId());
        dto.setMaterialId(item.getProductId());
        dto.setMaterialCode(item.getProductName());
        dto.setMaterialName(item.getProductName());
        dto.setQuantity(item.getQty());
        dto.setPrice(item.getProductPrice());
        dto.setAmount(item.getTotalPrice());
        dto.setUnit(item.getProductUnitName());
        dto.setRemark(item.getRemark());
        dto.setTaxRate(item.getTaxRate());
        dto.setTaxAmount(item.getTax());
        dto.setGrossPrice(item.getGrossPrice());
        dto.setGrossTotalPrice(item.getTotalPrice());
        dto.setWarehouseId(item.getWarehouseId());
        dto.setProductCode(item.getProductCode());
        dto.setDeclaredType(item.getDeclaredType());
        dto.setContainerRate(item.getContainerRate());
        dto.setApplicantId(item.getApplicantId());
        dto.setApplicationDeptId(item.getApplicationDeptId());
        return dto;
    }


    /**
     * 状态机-变动退货项-退货数量
     * <p>
     * 出库单审核后回调
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void updatePurchaseReturnItemQty(@Validated SrmReturnSaveReqDTO reqDTO) {
        log.debug("采购退货单回调，SrmReturnSaveReqDTO: {}", reqDTO);
        //校验
        if (!Objects.equals(reqDTO.getUpstreamType(), BillType.SRM_PURCHASE_RETURN.getValue())) {
            throw new IllegalArgumentException(StrUtil.format("出库单审核回调SrmOutboundReqDTO类型错误，上游类型({})不是退货单", Objects.requireNonNull(BillType.parse(reqDTO.getUpstreamType())).getLabel()));
        }
        //校验item存在
        List<Long> itemIds = reqDTO.getItems().stream().map(SrmReturnSaveItemReqDTO::getUpstreamId).collect(Collectors.toList());
        List<SrmPurchaseReturnItemDO> srmPurchaseReturnItemDOS = purchaseReturnService.validatePurchaseReturnItemExists(itemIds);

        //消费
        reqDTO.getItems().forEach(item -> {
            SrmPurchaseOutItemCountContext build = SrmPurchaseOutItemCountContext.builder()
                .outItemId(item.getUpstreamId())
                .outCount(BigDecimal.valueOf(item.getActualQty()))
                .build();
            srmOutboundStateMachine.fireEvent(SrmOutboundStatus.NONE_OUTBOUND, SrmEventEnum.OUT_STORAGE_ADJUSTMENT, build);
        });
    }
}