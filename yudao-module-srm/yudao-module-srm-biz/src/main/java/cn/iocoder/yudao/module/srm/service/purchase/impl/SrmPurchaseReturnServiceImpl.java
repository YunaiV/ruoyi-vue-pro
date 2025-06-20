package cn.iocoder.yudao.module.srm.service.purchase.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.config.machine.outItem.SrmPurchaseOutItemCountContext;
import cn.iocoder.yudao.module.srm.config.machine.outItem.SrmPurchaseOutMachineContext;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.srm.convert.purchase.SrmPurchaseReturnConvert;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.*;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.*;
import cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOutboundStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmReturnStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseReturnService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.srm.service.purchase.refund.SrmPurchaseReturnBO;
import cn.iocoder.yudao.module.srm.service.purchase.refund.SrmPurchaseReturnItemBO;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.api.outbound.WmsOutboundApi;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundImportReqDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundItemSaveReqDTO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.srm.enums.SrmEventEnum.RETURN_CANCEL;
import static cn.iocoder.yudao.module.srm.enums.SrmEventEnum.RETURN_COMPLETE;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;
import static jodd.util.StringUtil.truncate;

/**
 * ERP 采购退货 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SrmPurchaseReturnServiceImpl implements SrmPurchaseReturnService {

    private final SrmPurchaseReturnMapper purchaseReturnMapper;
    private final SrmPurchaseReturnItemMapper purchaseReturnItemMapper;
    private final SrmPurchaseInItemMapper inItemMapper;
    private final SrmPurchaseOrderItemMapper orderItemMapper;
    private final SrmPurchaseInMapper inMapper;
    private final SrmPurchaseOrderMapper orderMapper;
    private final SrmNoRedisDAO noRedisDAO;
    private final ErpProductApi erpProductApi;
    private final SrmSupplierService supplierService;

    @Resource(name = PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME)
    StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseReturnAuditReqVO> auditStatusMachine;
    @Resource(name = PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME)
    StateMachine<SrmReturnStatus, SrmEventEnum, SrmPurchaseReturnDO> refundStateMachine;
    @Resource(name = PURCHASE_RETURN_OUT_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutMachineContext> outStorageStateMachine;
    @Resource(name = PURCHASE_RETURN_ITEM_OUT_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutItemCountContext> itemOutStorageStateMachine;
    @Autowired
    private WmsOutboundApi wmsOutboundApi;

    /**
     * 安全处理 BigDecimal 值，避免空指针
     */
    private static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 设置退货单号
     *
     * @param vo 退货单创建请求 VO
     */
    private void voSetNo(SrmPurchaseReturnSaveReqVO vo) {
        // 生成单据编号
        if (vo.getCode() != null) {
            // 手动输入编号
            ThrowUtil.ifThrow(purchaseReturnMapper.selectByNo(vo.getCode()) != null, PURCHASE_RETURN_NO_EXISTS, vo.getCode());
            noRedisDAO.setManualSerial(SrmNoRedisDAO.PURCHASE_RETURN_NO_PREFIX, vo.getCode());
        } else {
            // 自动生成编号
            vo.setCode(noRedisDAO.generate(SrmNoRedisDAO.PURCHASE_RETURN_NO_PREFIX, PURCHASE_RETURN_NO_OUT_OF_BOUNDS));
            // 校验编号是否已存在
            ThrowUtil.ifThrow(purchaseReturnMapper.selectByNo(vo.getCode()) != null, PURCHASE_RETURN_NO_EXISTS);
        }
    }

    /**
     * 计算总体积和总重量
     *
     * @param purchaseReturn 退货单
     * @param items          退货项列表
     */
    private void calculateTotalVolumeAndWeight(SrmPurchaseReturnDO purchaseReturn, List<SrmPurchaseReturnItemDO> items) {
        // 1. 获取所有产品信息
        Set<Long> productIds = convertSet(items, SrmPurchaseReturnItemDO::getProductId);
        Map<Long, ErpProductDTO> productMap = convertMap(erpProductApi.listProductDTOs(productIds.stream().toList()), ErpProductDTO::getId);

        // 2. 计算总体积和总重量
        BigDecimal totalVolume = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (SrmPurchaseReturnItemDO item : items) {
            ErpProductDTO product = productMap.get(item.getProductId());
            if (product == null) {
                continue;
            }
            // 计算单个产品的体积和重量
            // 计算单个产品的总体积 = (长 * 宽 * 高) * 数量（单位：立方毫米）
            if (product.getLength() != null && product.getWidth() != null && product.getHeight() != null) {
                BigDecimal itemVolume = new BigDecimal(product.getLength())
                    .multiply(new BigDecimal(product.getWidth()))
                    .multiply(new BigDecimal(product.getHeight()));
                totalVolume = totalVolume.add(
                    MoneyUtils.priceMultiply(itemVolume, item.getQty())
                );
            }
            // 计算单个产品的总重量 = 产品重量 * 数量
            if (product.getWeight() != null) {
                totalWeight = totalWeight.add(
                    MoneyUtils.priceMultiply(product.getWeight(), item.getQty())
                );
            }
        }
        purchaseReturn.setTotalVolume(totalVolume);
        purchaseReturn.setTotalWeight(totalWeight);
    }

    /**
     * 校验退货项对应的入库单Id是否相同
     */
    private void checkInItemId(List<SrmPurchaseReturnItemDO> item) {
        Set<Long> arriveItemIds = item.stream().map(SrmPurchaseReturnItemDO::getArriveItemId).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(arriveItemIds)) {
            List<SrmPurchaseInItemDO> inItemDOS = inItemMapper.selectByIds(arriveItemIds);
            //判断inItemDOS所有的inId是否相同
            if (CollUtil.isNotEmpty(inItemDOS)) {
                Set<Long> inIds = inItemDOS.stream().map(SrmPurchaseInItemDO::getArriveId).collect(Collectors.toSet());
                if (CollUtil.isNotEmpty(inIds) && inIds.size() > 1) {
                    throw exception(PURCHASE_RETURN_IN_ITEM_IN_ID_NOT_SAME);
                }
            }
        }
    }

    /**
     * 校验退货数量不能超过入库数量
     *
     * @param items 退货项列表
     */
    private void checkReturnQtyNotExceedInQty(List<SrmPurchaseReturnSaveReqVO.Item> items) {
        for (SrmPurchaseReturnSaveReqVO.Item item : items) {
            SrmPurchaseInItemDO inItem = inItemMapper.selectById(item.getArriveItemId());
            if (inItem == null) {
                throw exception(PURCHASE_IN_ITEM_NOT_EXISTS, item.getArriveItemId());
            }
            if (item.getQty().compareTo(inItem.getActualQty()) > 0) {
                throw exception(PURCHASE_RETURN_QTY_EXCEED_IN_QTY, item.getQty(), inItem.getActualQty());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseReturn(SrmPurchaseReturnSaveReqVO vo) {
        // 1.1 校验入库单已审核，已开启
        validReqItemsAuditStatus(vo);
        // 1.2 校验退货项的有效性
        List<SrmPurchaseReturnItemDO> item = validatePurchaseReturnItems(vo.getItems());
        // 1.2.1 校验退货项是否同一个入库单
        checkInItemId(item);
        // 1.2.2 校验退货项对应的入库单供应商是否一致
        checkInSupplierConsistent(item);
        // 1.2.3 校验退货数量不能超过入库数量
        checkReturnQtyNotExceedInQty(vo.getItems());

        // 1.3 校验结算账户
//        erpAccountApi.validateAccount(vo.getAccountId());
        // 1.4 生成退货单号，并校验唯一性
        voSetNo(vo);

        //兜底设置退货时间
        vo.setReturnTime(vo.getReturnTime() == null ? LocalDateTime.now() : vo.getReturnTime());

        // 2.1 插入退货
        SrmPurchaseReturnDO purchaseReturn = BeanUtils.toBean(vo, SrmPurchaseReturnDO.class, in -> in.setCode(vo.getCode()));
        // 2.2 计算总价、总体积、总重量
        calculateTotalPrice(purchaseReturn, item);
        calculateTotalVolumeAndWeight(purchaseReturn, item);
        purchaseReturnMapper.insert(purchaseReturn);

        // 2.3 插入退货项
        item.forEach(o -> o.setReturnId(purchaseReturn.getId()));
        purchaseReturnItemMapper.insertBatch(item);

        // 3. 更新主子表状态
        initMasterStatus(purchaseReturn);
        //更新行状态
        initSlaveStatus(item);
        return purchaseReturn.getId();
    }

    private void initSlaveStatus(List<SrmPurchaseReturnItemDO> item) {
        for (SrmPurchaseReturnItemDO returnItemDO : item) {
            itemOutStorageStateMachine.fireEvent(SrmOutboundStatus.NONE_OUTBOUND, SrmEventEnum.OUT_STORAGE_INIT, SrmPurchaseOutItemCountContext.builder().outItemId(returnItemDO.getId()).build());
        }

    }

    /**
     * 校验申请项里面是否符合状态要求
     *
     * @param vo vo
     */
    private void validReqItemsAuditStatus(SrmPurchaseReturnSaveReqVO vo) {
        for (SrmPurchaseReturnSaveReqVO.Item item : vo.getItems()) {
            Long arriveItemId = item.getArriveItemId();
            Optional.ofNullable(inItemMapper.selectById(arriveItemId)).ifPresent(inItemDO -> {
                SrmPurchaseInDO srmPurchaseInDO = inMapper.selectById(inItemDO.getArriveId());
                //非已审核状态
                ThrowUtil.ifThrow(!srmPurchaseInDO.getAuditStatus().equals(SrmAuditStatus.APPROVED.getCode()), (PURCHASE_IN_ITEM_NOT_AUDIT));
                //非开启状态
            });
        }
    }

//    private void linkSlaveStatus(List<SrmPurchaseReturnItemDO> items) {
//        //订单入库状态机
//        for (SrmPurchaseReturnItemDO item : items) {
//            Optional.ofNullable(inItemMapper.selectById(item.getInItemId())).ifPresent(o -> {
//                syncCountLogic(o, item.getQty());
//            });
//        }
//    }

    private void initMasterStatus(SrmPurchaseReturnDO purchaseReturn) {
        auditStatusMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, SrmPurchaseReturnAuditReqVO.builder().ids(Collections.singletonList(purchaseReturn.getId())).build());
        refundStateMachine.fireEvent(SrmReturnStatus.NOT_RETURN, SrmEventEnum.RETURN_INIT, purchaseReturn);
        //出库状态
        outStorageStateMachine.fireEvent(SrmOutboundStatus.NONE_OUTBOUND, SrmEventEnum.OUT_STORAGE_INIT, new SrmPurchaseOutMachineContext().setReturnId(purchaseReturn.getId()));
    }

    /**
     * 校验退货项对应的入库单供应商是否一致
     *
     * @param items 退货项列表
     */
    private void checkInSupplierConsistent(List<SrmPurchaseReturnItemDO> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        // 1. 获取所有入库项ID
        Set<Long> arriveItemIds = items.stream().map(SrmPurchaseReturnItemDO::getArriveItemId).collect(Collectors.toSet());
        // 2. 获取入库项信息
        List<SrmPurchaseInItemDO> inItems = inItemMapper.selectByIds(arriveItemIds);
        if (CollUtil.isEmpty(inItems)) {
            return;
        }
        // 3. 获取入库单ID
        Set<Long> inIds = inItems.stream().map(SrmPurchaseInItemDO::getArriveId).collect(Collectors.toSet());
        // 4. 获取入库单信息
        List<SrmPurchaseInDO> inList = inMapper.selectByIds(inIds);
        if (CollUtil.isEmpty(inList)) {
            return;
        }
        // 5. 校验供应商是否一致
        Map<Long, SrmPurchaseInDO> inMap = convertMap(inList, SrmPurchaseInDO::getId);
        Map<Long, Long> inItemToInMap = convertMap(inItems, SrmPurchaseInItemDO::getId, SrmPurchaseInItemDO::getArriveId);

        // 获取所有供应商信息
        Set<Long> supplierIds = inList.stream().map(SrmPurchaseInDO::getSupplierId).collect(Collectors.toSet());
        Map<Long, SrmSupplierDO> supplierMap = supplierService.getSupplierMap(supplierIds);

        // 获取第一个入库单的供应商作为基准
        Long firstSupplierId = inList.get(0).getSupplierId();
        SrmSupplierDO firstSupplier = supplierMap.get(firstSupplierId);
        if (firstSupplier == null) {
            throw exception(SUPPLIER_NOT_EXISTS, firstSupplierId);
        }

        // 遍历所有入库项，检查供应商是否一致
        for (SrmPurchaseReturnItemDO item : items) {
            Long inId = inItemToInMap.get(item.getArriveItemId());
            SrmPurchaseInDO inDO = inMap.get(inId);
            if (!firstSupplierId.equals(inDO.getSupplierId())) {
                SrmSupplierDO supplier = supplierMap.get(inDO.getSupplierId());
                if (supplier == null) {
                    throw exception(SUPPLIER_NOT_EXISTS, inDO.getSupplierId());
                }
                throw exception(PURCHASE_RETURN_IN_SUPPLIER_NOT_SAME,
                    firstSupplier.getName(), // 第一个供应商名称
                    inDO.getCode(), // 不一致的入库单编号
                    supplier.getName()); // 不一致的供应商名称
            }
        }
    }


    private Boolean validAudit(Long returnId) {
        // 1.1 校验已审核
        SrmPurchaseReturnDO purchaseReturn = validatePurchaseReturnExists(returnId);
        return SrmAuditStatus.APPROVED.getCode().equals(purchaseReturn.getAuditStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseReturn(SrmPurchaseReturnSaveReqVO vo) {
        // 1.1 校验已审核
        ThrowUtil.ifThrow(validAudit(vo.getId()), PURCHASE_RETURN_UPDATE_FAIL_APPROVE);
        // 1.2 校验入库单已审核，已开启
        validReqItemsAuditStatus(vo);
        // 1.3 校验结算账户
//        erpAccountApi.validateAccount(vo.getAccountId());
        // 1.4 校验退货项的有效性
        List<SrmPurchaseReturnItemDO> purchaseReturnItems = validatePurchaseReturnItems(vo.getItems());
        // 1.5 校验退货项是否同一个入库单
        checkInItemId(purchaseReturnItems);
        // 1.6 校验退货项对应的入库单供应商是否一致
        checkInSupplierConsistent(purchaseReturnItems);
        // 1.7 校验退货数量不能超过入库数量
        checkReturnQtyNotExceedInQty(vo.getItems());
        // 1.8 校验单号
        SrmPurchaseReturnDO oldReturn = validatePurchaseReturnExists(vo.getId());
        if (vo.getCode() != null && !vo.getCode().equals(oldReturn.getCode())) {
            voSetNo(vo);
        }

        // 2.1 更新退货
        SrmPurchaseReturnDO updateObj = BeanUtils.toBean(vo, SrmPurchaseReturnDO.class);
        calculateTotalPrice(updateObj, purchaseReturnItems);
        calculateTotalVolumeAndWeight(updateObj, purchaseReturnItems);
        purchaseReturnMapper.updateById(updateObj);
        // 2.2 更新退货项
        updatePurchaseReturnItemList(vo.getId(), purchaseReturnItems);
    }

    private void calculateTotalPrice(SrmPurchaseReturnDO purchaseReturn, List<SrmPurchaseReturnItemDO> purchaseReturnItems) {
        // 1. 计算总数量、总价等
        purchaseReturn.setTotalCount(getSumValue(purchaseReturnItems, SrmPurchaseReturnItemDO::getQty, BigDecimal::add, BigDecimal.ZERO));
        purchaseReturn.setTotalProductPrice(getSumValue(purchaseReturnItems, SrmPurchaseReturnItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseReturn.setTotalGrossPrice(getSumValue(purchaseReturnItems, SrmPurchaseReturnItemDO::getTax, BigDecimal::add, BigDecimal.ZERO));
        purchaseReturn.setTotalPrice(safe(purchaseReturn.getTotalProductPrice()).add(safe(purchaseReturn.getTotalGrossPrice())));

        // 2. 计算优惠价格
        if (purchaseReturn.getDiscountPercent() == null) {
            purchaseReturn.setDiscountPercent(BigDecimal.ZERO);
        }
        purchaseReturn.setDiscountPrice(MoneyUtils.priceMultiplyPercent(purchaseReturn.getTotalPrice(), purchaseReturn.getDiscountPercent()));
        purchaseReturn.setTotalPrice(safe(purchaseReturn.getTotalPrice()).subtract(safe(purchaseReturn.getDiscountPrice())).add(safe(purchaseReturn.getOtherPrice())));
    }

    @Override
    public void updatePurchaseReturnRefundPrice(Long id, BigDecimal refundPrice) {
        SrmPurchaseReturnDO purchaseReturn = purchaseReturnMapper.selectById(id);
        if (purchaseReturn.getRefundPrice().equals(refundPrice)) {
            return;
        }
        if (refundPrice.compareTo(purchaseReturn.getTotalPrice()) > 0) {
            throw exception(PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED, refundPrice, purchaseReturn.getTotalPrice());
        }
        purchaseReturnMapper.updateById(new SrmPurchaseReturnDO().setId(id).setRefundPrice(refundPrice));
    }

    private List<SrmPurchaseReturnItemDO> validatePurchaseReturnItems(List<SrmPurchaseReturnSaveReqVO.Item> list) {
        // 1. 校验入库项存在，并获取入库项信息
        Set<Long> arriveItemIdSet = list.stream().map(SrmPurchaseReturnSaveReqVO.Item::getArriveItemId).collect(Collectors.toSet());
        Map<Long, SrmPurchaseInItemDO> inItemMap = new HashMap<>();
        for (Long inItemId : arriveItemIdSet) {
            SrmPurchaseInItemDO inItemDO = inItemMapper.selectById(inItemId);
            ThrowUtil.ifThrow(inItemDO == null, PURCHASE_IN_ITEM_NOT_EXISTS, inItemId);
            inItemMap.put(inItemId, inItemDO);
        }

        // 2. 获取所有产品信息，用于后续计算
        Set<Long> productIds = convertSet(inItemMap.values(), SrmPurchaseInItemDO::getProductId);
        Map<Long, ErpProductDTO> productMap = convertMap(erpProductApi.listProductDTOs(productIds.stream().toList()), ErpProductDTO::getId);

        // 3. 获取入库单信息，用于填充 arrive_code
        Set<Long> inIds = convertSet(inItemMap.values(), SrmPurchaseInItemDO::getArriveId);
        Map<Long, SrmPurchaseInDO> inMap = convertMap(inMapper.selectByIds(inIds), SrmPurchaseInDO::getId);

        // 4. 转化为 SrmPurchaseReturnItemDO 列表，并从入库项复制信息
        return convertList(list, o -> {
            // 4.1 从入库项复制基础信息
            SrmPurchaseInItemDO inItem = inItemMap.get(o.getArriveItemId()); //入库项
            SrmPurchaseReturnItemDO item = new SrmPurchaseReturnItemDO(); // 创建新对象
            // 手动复制需要的字段，避免复制 id
            item.setProductId(inItem.getProductId())
                .setProductName(inItem.getProductName())
                .setProductPrice(inItem.getProductPrice())
                .setTaxRate(inItem.getTaxRate())
                .setProductUnitId(inItem.getProductUnitId())
                .setProductUnitName(inItem.getProductUnitName())
                .setWarehouseId(inItem.getWarehouseId())
                .setProductCode(inItem.getProductCode())
                .setDeclaredType(inItem.getDeclaredType())
                .setDeclaredTypeEn(inItem.getDeclaredTypeEn())
                .setContainerRate(inItem.getContainerRate())
                .setGrossPrice(inItem.getGrossPrice())
                .setActualQty(inItem.getActualQty())
                .setApplicationDeptId(inItem.getApplicationDeptId()) //申请部门ID
                .setApplicantId(inItem.getApplicantId()) //申请人ID
                .setActualQty(inItem.getActualQty()) // 实际入库数量(业务上:到货明细入库数量不再变更)
            ;

            // 4.2 设置退货项特有信息
            item.setArriveItemId(o.getArriveItemId()) // 入库项ID
                .setQty(o.getQty()) // 退货数量
                .setRemark(o.getRemark()); // 备注
            // 4.3 设置入库单编号
            SrmPurchaseInDO inDO = inMap.get(inItem.getArriveId());
            if (inDO != null) {
                item.setArriveCode(inDO.getCode());
            }

            // 4.4 设置产品单位（从入库项中获取）
            item.setProductUnitId(inItem.getProductUnitId());

            // 4.5 计算总价和税额
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getQty()));
            if (item.getTotalPrice() != null && item.getTaxRate() != null) {
                item.setTax(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxRate()));
            }
            return item;
        });
    }

    private void updatePurchaseReturnItemList(Long id, List<SrmPurchaseReturnItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SrmPurchaseReturnItemDO> oldList = purchaseReturnItemMapper.selectListByReturnId(id);
        List<List<SrmPurchaseReturnItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setReturnId(id));
            purchaseReturnItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            purchaseReturnItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            purchaseReturnItemMapper.deleteByIds(convertList(diffList.get(2), SrmPurchaseReturnItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseReturn(List<Long> ids) {
        List<SrmPurchaseReturnDO> purchaseReturns = purchaseReturnMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(purchaseReturns)) {
            return;
        }
        //1. 校验已审核
        purchaseReturns.forEach(purchaseReturn -> ThrowUtil.ifThrow(validAudit(purchaseReturn.getId()), PURCHASE_RETURN_DELETE_FAIL_APPROVE));

        // 2. 遍历删除，并记录操作日志
        purchaseReturns.forEach(purchaseReturn -> {
            // 2.1 删除订单
            purchaseReturnMapper.deleteById(purchaseReturn.getId());
            // 2.2 删除订单项
            purchaseReturnItemMapper.deleteByReturnId(purchaseReturn.getId());
        });

    }

    private SrmPurchaseReturnDO validatePurchaseReturnExists(Long id) {
        SrmPurchaseReturnDO purchaseReturn = purchaseReturnMapper.selectById(id);
        if (purchaseReturn == null) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        return purchaseReturn;
    }

    @Override
    public SrmPurchaseReturnDO getPurchaseReturn(Long id) {
        return purchaseReturnMapper.selectById(id);
    }

    @Override
    public SrmPurchaseReturnBO getPurchaseBOReturn(Long id) {
        //主表
        SrmPurchaseReturnDO srmPurchaseReturnDO = purchaseReturnMapper.selectById(id);
        //子表
        List<SrmPurchaseReturnItemDO> srmPurchaseReturnItemDOS = purchaseReturnItemMapper.selectListByReturnId(id);
        return BeanUtils.toBean(srmPurchaseReturnDO, SrmPurchaseReturnBO.class, peek -> peek.setSrmPurchaseReturnItemDOs(srmPurchaseReturnItemDOS));
    }

    @Override
    public List<SrmPurchaseReturnDO> getPurchaseReturnList(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return purchaseReturnMapper.selectListByIds(ids);
    }

    @Override
    public SrmPurchaseReturnDO validatePurchaseReturn(Long id) {
        SrmPurchaseReturnDO purchaseReturn = getPurchaseReturn(id);
        if (ObjectUtil.notEqual(purchaseReturn.getAuditStatus(), SrmAuditStatus.APPROVED.getCode())) {
            throw exception(PURCHASE_RETURN_NOT_APPROVE);
        }
        return purchaseReturn;
    }


    @Override
    public PageResult<SrmPurchaseReturnBO> getPurchaseReturnBOPage(SrmPurchaseReturnPageReqVO pageReqVO) {
        // 1. 查询分页数据
        PageResult<SrmPurchaseReturnItemBO> page = purchaseReturnItemMapper.selectBOPage(pageReqVO);

        // 2. 转换为目标BO
        return SrmPurchaseReturnConvert.INSTANCE.convertPage(page);
    }

    // ==================== 采购退货项 ====================

    @Override
    public List<SrmPurchaseReturnItemDO> getPurchaseReturnItemListByReturnId(Long returnId) {
        return purchaseReturnItemMapper.selectListByReturnId(returnId);
    }

    @Override
    public List<SrmPurchaseReturnItemDO> getPurchaseReturnItemListByReturnIds(Collection<Long> returnIds) {
        if (CollUtil.isEmpty(returnIds)) {
            return Collections.emptyList();
        }
        return purchaseReturnItemMapper.selectListByReturnIds(returnIds);
    }

    @Override
    public void submitAudit(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        //校验 1. 批量查询订单信息
        List<SrmPurchaseReturnDO> dos = purchaseReturnMapper.selectByIds(ids);
        if (CollUtil.isEmpty(dos)) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        // 2. 触发事件
        dos.forEach(returnDO -> auditStatusMachine.fireEvent(SrmAuditStatus.fromCode(returnDO.getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, SrmPurchaseReturnAuditReqVO.builder().ids(Collections.singletonList(returnDO.getId())).build()));
    }

    /**
     * 状态判断+作废采购退货单关联的WMS出库单
     *
     * @param returnId 采购退货单ID
     */
    private void abandonWmsOutbound(Long returnId) {
        List<WmsOutboundDTO> dtoList = Optional.ofNullable(wmsOutboundApi.getOutboundList(BillType.SRM_PURCHASE_RETURN.getValue(), returnId))
            .orElse(Collections.emptyList());
        if (CollUtil.isEmpty(dtoList)) {
            return;
        }
        //  收集所有非草稿
        List<String> nonDraftCodes = dtoList.stream()
            .filter(dto -> !Objects.equals(dto.getAuditStatus(), WmsOutboundAuditStatus.DRAFT.getValue()))
            .map(WmsOutboundDTO::getCode)
            .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(nonDraftCodes)) {
            throw exception(PURCHASE_RETURN_WMS_OUTBOUND_NOT_CAN_ABANDON, String.join(",", nonDraftCodes));
        }

        // 作废出库单
        dtoList.forEach(dto -> wmsOutboundApi.abandonOutbound(dto.getId(), "采购退货反审核"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(SrmPurchaseReturnAuditReqVO req) {
        // 查询退货单信息
        req.getIds().stream().findFirst().ifPresent(id -> {
            SrmPurchaseReturnDO purchaseReturnDO = purchaseReturnMapper.selectById(id);
            if (purchaseReturnDO == null) {
                throw exception(PURCHASE_RETURN_NOT_EXISTS);
            }
            // 获取当前订单状态
            SrmAuditStatus currentStatus = SrmAuditStatus.fromCode(purchaseReturnDO.getAuditStatus());
            if (Boolean.TRUE.equals(req.getReviewed())) {
                List<SrmPurchaseReturnItemDO> returnItemDOS = purchaseReturnItemMapper.selectListByReturnId(id);
                // 审核操作
                if (req.getPass()) {
                    log.debug("退货单通过审核，ID: {}", purchaseReturnDO.getId());
                    auditStatusMachine.fireEvent(currentStatus, SrmEventEnum.AGREE, req);
                    //创建 WMS 出库单(草稿)
                    try {
                        this.createWmsOutbound(purchaseReturnDO, returnItemDOS);
                    } catch (Exception e) {
                        log.error("创建WMS出库单失败", e);
                        throw exception(PURCHASE_RETURN_PROCESS_FAIL_WMS_OUTBOUND_EXISTS, truncate(e.getMessage(), 200));
                    }
                } else {
                    //审核不通过
                    log.debug("退货单拒绝审核，ID: {}", purchaseReturnDO.getId());
                    auditStatusMachine.fireEvent(currentStatus, SrmEventEnum.REJECT, req);
                }
            } else {
                // 反审核
                log.debug("退货单撤回审核，ID: {}", purchaseReturnDO.getId());
                auditStatusMachine.fireEvent(currentStatus, SrmEventEnum.WITHDRAW_REVIEW, req);
                // 审核状态判断+作废WMS出库单
                abandonWmsOutbound(purchaseReturnDO.getId());
            }
        });
    }

    @Override
    public void refund(SrmPurchaseReturnAuditReqVO vo) {
        // 校验，已审核才可以退款
        vo.getIds().stream().distinct().forEach(id -> {
            SrmPurchaseReturnDO returnDO = purchaseReturnMapper.selectById(id);
            if (returnDO == null) {
                throw exception(PURCHASE_RETURN_NOT_EXISTS);
            }
            if (!SrmAuditStatus.APPROVED.getCode().equals(returnDO.getAuditStatus())) {
                throw exception(PURCHASE_RETURN_PROCESS_FAIL_PAYMENT_STATUS);
            }
        });
        SrmEventEnum eventEnum = null;
        if (vo.getPass()) {
            eventEnum = RETURN_COMPLETE;
        } else {
            eventEnum = RETURN_CANCEL;
        }
        SrmEventEnum finalEventEnum = eventEnum;
        vo.getIds().stream().distinct().forEach(id -> {
            SrmPurchaseReturnDO orderDO = purchaseReturnMapper.selectById(id);
            //校验
            refundStateMachine.fireEvent(SrmReturnStatus.fromCode(orderDO.getRefundStatus()), finalEventEnum, orderDO);
        });
    }

    /**
     * 创建 WMS 出库单
     *
     * @param purchaseReturn 采购退货单
     * @param returnItems    采购退货单明细
     */
    private void createWmsOutbound(SrmPurchaseReturnDO purchaseReturn, List<SrmPurchaseReturnItemDO> returnItems) {
        // 1. 按仓库分组退货项
        Map<Long, List<SrmPurchaseReturnItemDO>> warehouseItemsMap = returnItems.stream()
            .collect(Collectors.groupingBy(item -> {
                // 获取入库项
                SrmPurchaseInItemDO inItem = inItemMapper.selectById(item.getArriveItemId());
                if (inItem == null || inItem.getWarehouseId() == null) {
                    throw exception(PURCHASE_RETURN_PROCESS_FAIL_WAREHOUSE_ID_DONT_EXISTS);
                }
                return inItem.getWarehouseId();
            }));

        // 2. 为每个仓库创建出库单
        warehouseItemsMap.forEach((warehouseId, items) -> {
            // 构建出库单基本信息
            WmsOutboundImportReqDTO importReqDTO = buildOutboundBaseInfo(purchaseReturn);
            importReqDTO.setWarehouseId(warehouseId); // 设置仓库ID
            // 构建出库单明细信息，将相同仓库的退货项合并到一个出库单中
            List<WmsOutboundItemSaveReqDTO> saveReqDTOS = buildOutboundItems(items);
            //根据主单的公司ID复制给明细行
            saveReqDTOS.forEach(item -> item.setCompanyId(importReqDTO.getCompanyId()));
            importReqDTO.setItemList(saveReqDTOS);
            importReqDTO.setCompanyId(null);
            // 生成出库单
            wmsOutboundApi.generateOutbound(importReqDTO);
        });
    }

    /**
     * 构建出库单基本信息
     *
     * @param purchaseReturn 采购退货单
     * @return 出库单基本信息
     */
    private WmsOutboundImportReqDTO buildOutboundBaseInfo(SrmPurchaseReturnDO purchaseReturn) {
        WmsOutboundImportReqDTO importReqDTO = new WmsOutboundImportReqDTO();
        importReqDTO.setType(cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundType.ORDER.getValue()); // 订单出库
        importReqDTO.setUpstreamId(purchaseReturn.getId()); // 来源单据ID
        importReqDTO.setUpstreamCode(purchaseReturn.getCode()); // 来源单据编码
        importReqDTO.setUpstreamType(BillType.SRM_PURCHASE_RETURN.getValue()); // 来源单据类型
        //甲方(财务公司ID)，取第一个退货项 -> 到货项 -> 订单项 -> 订单(purchaseCompanyId)
        // 获取甲方(财务公司ID)，从退货项追溯到订单的财务公司ID
        List<SrmPurchaseReturnItemDO> returnItems = purchaseReturnItemMapper.selectListByReturnId(purchaseReturn.getId());
        if (CollUtil.isNotEmpty(returnItems)) {
            // 获取第一个退货项
            SrmPurchaseReturnItemDO firstItem = returnItems.get(0);
            // 获取对应的入库项
            SrmPurchaseInItemDO inItem = inItemMapper.selectById(firstItem.getArriveItemId());
            if (inItem != null) {
                // 获取对应的订单项
                SrmPurchaseOrderItemDO orderItem = orderItemMapper.selectById(inItem.getOrderItemId());
                if (orderItem != null) {
                    // 获取订单
                    SrmPurchaseOrderDO order = orderMapper.selectById(orderItem.getOrderId());
                    if (order != null) {
                        // 获取订单的财务公司ID
                        importReqDTO.setCompanyId(order.getPurchaseCompanyId());
                    }
                }
            }
        }

        importReqDTO.setRemark(purchaseReturn.getRemark()); // 备注
        importReqDTO.setOutboundTime(purchaseReturn.getReturnTime()); // 出库时间
        return importReqDTO;
    }

    /**
     * 构建出库单明细信息
     *
     * @param returnItems 采购退货单明细
     * @return 出库单明细列表
     */
    private List<WmsOutboundItemSaveReqDTO> buildOutboundItems(List<SrmPurchaseReturnItemDO> returnItems) {
        return returnItems.stream().map(item -> {
            WmsOutboundItemSaveReqDTO itemDTO = new WmsOutboundItemSaveReqDTO();
            itemDTO.setProductId(item.getProductId()); // 产品ID
            itemDTO.setPlanQty(item.getQty().intValue()); // 计划出库量
            itemDTO.setActualQty(item.getQty().intValue()); // 实际出库量
            itemDTO.setRemark(item.getRemark()); // 备注
            itemDTO.setUpstreamId(item.getId()); // 来源明细行ID
            itemDTO.setDeptId(item.getApplicationDeptId()); //归属部门ID
            // company 复制主单的
            return itemDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SrmPurchaseReturnItemDO> validatePurchaseReturnItemExists(List<Long> ids) {
        HashSet<Long> set = new HashSet<>(ids);
        if (CollUtil.isEmpty(set)) {
            return Collections.emptyList();
        }
        List<SrmPurchaseReturnItemDO> items = purchaseReturnItemMapper.selectListByIds(set.stream().toList());
        //检验是否和ids数量一致，报错未对应退货项
        if (items.size() != set.size()) {
            throw exception(PURCHASE_RETURN_ITEM_NOT_EXISTS, CollUtil.subtract(ids, CollUtil.newArrayList(items.stream().map(SrmPurchaseReturnItemDO::getId).collect(Collectors.toSet()))));
        }
        return items;
    }
}