package cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt;

import cn.hutool.core.collection.CollUtil;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.MesWmOutsourceReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.MesWmOutsourceReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.outsourcereceipt.MesWmOutsourceReceiptMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmOutsourceReceiptStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 外协入库单 Service 实现类
 */
@Service
@Validated
public class MesWmOutsourceReceiptServiceImpl implements MesWmOutsourceReceiptService {

    @Resource
    private MesWmOutsourceReceiptMapper outsourceReceiptMapper;

    @Resource
    private MesWmOutsourceReceiptLineService outsourceReceiptLineService;
    @Resource
    private MesWmOutsourceReceiptDetailService outsourceReceiptDetailService;
    @Resource
    private MesWmTransactionService wmTransactionService;
    @Resource
    private MesMdVendorService vendorService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesProWorkOrderService workOrderService;

    @Override
    public Long createOutsourceReceipt(MesWmOutsourceReceiptSaveReqVO createReqVO) {
        // 校验编码唯一
        validateCodeUnique(null, createReqVO.getCode());
        // 校验供应商存在
        vendorService.validateVendorExists(createReqVO.getVendorId());
        // 校验外协工单存在
        if (createReqVO.getWorkOrderId() != null) {
            workOrderService.validateWorkOrderExists(createReqVO.getWorkOrderId());
        }

        // 插入
        MesWmOutsourceReceiptDO receipt = BeanUtils.toBean(createReqVO, MesWmOutsourceReceiptDO.class);
        receipt.setStatus(MesWmOutsourceReceiptStatusEnum.PREPARE.getStatus());
        outsourceReceiptMapper.insert(receipt);
        return receipt.getId();
    }

    @Override
    public void updateOutsourceReceipt(MesWmOutsourceReceiptSaveReqVO updateReqVO) {
        // 校验存在 + 草稿状态
        validateOutsourceReceiptExistsAndDraft(updateReqVO.getId());
        // 校验编码唯一
        validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());
        // 校验供应商存在
        vendorService.validateVendorExists(updateReqVO.getVendorId());
        // 校验外协工单存在
        if (updateReqVO.getWorkOrderId() != null) {
            workOrderService.validateWorkOrderExists(updateReqVO.getWorkOrderId());
        }

        // 更新
        MesWmOutsourceReceiptDO updateObj = BeanUtils.toBean(updateReqVO, MesWmOutsourceReceiptDO.class);
        outsourceReceiptMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOutsourceReceipt(Long id) {
        // 校验存在 + 草稿状态
        validateOutsourceReceiptExistsAndDraft(id);

        // 级联删除明细和行
        outsourceReceiptDetailService.deleteOutsourceReceiptDetailByReceiptId(id);
        outsourceReceiptLineService.deleteOutsourceReceiptLineByReceiptId(id);
        // 删除
        outsourceReceiptMapper.deleteById(id);
    }

    @Override
    public MesWmOutsourceReceiptDO getOutsourceReceipt(Long id) {
        return outsourceReceiptMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmOutsourceReceiptDO> getOutsourceReceiptPage(MesWmOutsourceReceiptPageReqVO pageReqVO) {
        List<Long> workOrderIds = null;
        if (StrUtil.isNotBlank(pageReqVO.getWorkOrderCode())) {
            MesProWorkOrderDO workOrder = workOrderService.getWorkOrder(pageReqVO.getWorkOrderCode());
            if (workOrder == null) {
                return PageResult.empty();
            }
            workOrderIds = java.util.Collections.singletonList(workOrder.getId());
        }
        return outsourceReceiptMapper.selectPage(pageReqVO, workOrderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOutsourceReceipt(Long id) {
        // 校验存在 + 草稿状态
        validateOutsourceReceiptExistsAndDraft(id);
        // 校验至少有一条行
        List<MesWmOutsourceReceiptLineDO> lines = outsourceReceiptLineService.getOutsourceReceiptLineListByReceiptId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_OUTSOURCE_RECEIPT_NO_LINE);
        }

        // 检查是否有待检验的行
        boolean hasPendingQc = CollUtil.contains(lines,
                line -> MesWmQualityStatusEnum.PENDING.getStatus().equals(line.getQualityStatus()));
        // 根据质检状态，路由到待检验或待上架
        Integer targetStatus = hasPendingQc ? MesWmOutsourceReceiptStatusEnum.CONFIRMED.getStatus()
                : MesWmOutsourceReceiptStatusEnum.APPROVING.getStatus();
        outsourceReceiptMapper.updateById(new MesWmOutsourceReceiptDO()
                .setId(id).setStatus(targetStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockOutsourceReceipt(Long id) {
        // 1.1 校验存在 + 待上架状态
        MesWmOutsourceReceiptDO receipt = validateOutsourceReceiptExists(id);
        if (ObjUtil.notEqual(MesWmOutsourceReceiptStatusEnum.APPROVING.getStatus(), receipt.getStatus())) {
            throw exception(WM_OUTSOURCE_RECEIPT_STATUS_ERROR);
        }
        // 1.2 检查每个行的明细数量是否完成上架
        List<MesWmOutsourceReceiptLineDO> lines = outsourceReceiptLineService.getOutsourceReceiptLineListByReceiptId(id);
        if (CollUtil.isNotEmpty(lines)) {
            // 批量查询所有明细
            List<MesWmOutsourceReceiptDetailDO> allDetails = outsourceReceiptDetailService.getOutsourceReceiptDetailListByReceiptId(id);
            Map<Long, List<MesWmOutsourceReceiptDetailDO>> detailMap = CollectionUtils.convertMultiMap(
                    allDetails, MesWmOutsourceReceiptDetailDO::getLineId);
            // 检查每行的明细数量
            for (MesWmOutsourceReceiptLineDO line : lines) {
                List<MesWmOutsourceReceiptDetailDO> details = detailMap.getOrDefault(line.getId(), Collections.emptyList());
                BigDecimal totalDetailQuantity = CollectionUtils.getSumValue(details,
                        MesWmOutsourceReceiptDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
                // 对比行数量与明细总数量，不满足直接抛出
                if (line.getQuantity().compareTo(totalDetailQuantity) > 0) {
                    MesMdItemDO item = itemService.validateItemExists(line.getItemId());
                    throw exception(WM_OUTSOURCE_RECEIPT_DETAIL_QUANTITY_MISMATCH,
                            item.getCode() + " " + item.getName() + " 未完成上架");
                }
            }
        }

        // 2. 入库上架（待上架 → 已审批）
        outsourceReceiptMapper.updateById(new MesWmOutsourceReceiptDO()
                .setId(id).setStatus(MesWmOutsourceReceiptStatusEnum.APPROVED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishOutsourceReceipt(Long id) {
        // 1.1 校验存在
        MesWmOutsourceReceiptDO receipt = validateOutsourceReceiptExists(id);
        if (ObjUtil.notEqual(MesWmOutsourceReceiptStatusEnum.APPROVED.getStatus(), receipt.getStatus())) {
            throw exception(WM_OUTSOURCE_RECEIPT_STATUS_ERROR);
        }
        // 1.2 校验至少有一条行
        List<MesWmOutsourceReceiptLineDO> lines = outsourceReceiptLineService.getOutsourceReceiptLineListByReceiptId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_OUTSOURCE_RECEIPT_NO_LINE);
        }

        // 3. 遍历所有明细，创建库存事务（增加库存 + 记录流水）
        createTransactionList(receipt);

        // 4. 更新外协工单的已生产数量（只统计合格的目标产品）
        if (receipt.getWorkOrderId() != null) {
            MesProWorkOrderDO workOrder = workOrderService.getWorkOrder(receipt.getWorkOrderId());
            if (workOrder != null) {
                BigDecimal totalQuantity = lines.stream()
                        .filter(line -> ObjUtil.equal(line.getItemId(), workOrder.getProductId())) // 只统计目标产品
                        .filter(line -> ObjUtil.notEqual(line.getQualityStatus(), MesWmQualityStatusEnum.FAIL.getStatus())) // 排除不合格品
                        .map(MesWmOutsourceReceiptLineDO::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    workOrderService.updateProducedQuantity(receipt.getWorkOrderId(), totalQuantity);
                }
            }
        }

        // 5. 更新入库单状态
        outsourceReceiptMapper.updateById(new MesWmOutsourceReceiptDO()
                .setId(id).setStatus(MesWmOutsourceReceiptStatusEnum.FINISHED.getStatus()));
    }

    private void createTransactionList(MesWmOutsourceReceiptDO receipt) {
        List<MesWmOutsourceReceiptDetailDO> details = outsourceReceiptDetailService.getOutsourceReceiptDetailListByReceiptId(receipt.getId());
        wmTransactionService.createTransactionList(convertList(details, detail -> new MesWmTransactionSaveReqDTO()
                .setType(MesWmTransactionTypeEnum.IN.getType()).setItemId(detail.getItemId())
                .setQuantity(detail.getQuantity()) // 入库数量为正数
                .setBatchId(detail.getBatchId())
                .setWarehouseId(detail.getWarehouseId()).setLocationId(detail.getLocationId()).setAreaId(detail.getAreaId())
                .setVendorId(receipt.getVendorId()).setReceiptTime(receipt.getReceiptDate())
                .setBizType(MesBizTypeConstants.WM_OUTSOURCE_RECPT).setBizId(receipt.getId())
                .setBizCode(receipt.getCode()).setBizLineId(detail.getLineId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOutsourceReceipt(Long id) {
        // 校验存在
        MesWmOutsourceReceiptDO receipt = validateOutsourceReceiptExists(id);
        // 已完成和已取消不允许取消
        if (ObjectUtils.equalsAny(receipt.getStatus(),
                MesWmOutsourceReceiptStatusEnum.FINISHED.getStatus(),
                MesWmOutsourceReceiptStatusEnum.CANCELED.getStatus())) {
            throw exception(WM_OUTSOURCE_RECEIPT_CANCEL_NOT_ALLOWED);
        }
        // 取消
        outsourceReceiptMapper.updateById(new MesWmOutsourceReceiptDO()
                .setId(id).setStatus(MesWmOutsourceReceiptStatusEnum.CANCELED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOutsourceReceiptWhenIqcFinish(Long receiptId, Long lineId, Long iqcId,
                                                    BigDecimal qualifiedQuantity, BigDecimal unqualifiedQuantity) {
        // 1.1 校验入库单存在
        MesWmOutsourceReceiptDO receipt = validateOutsourceReceiptExists(receiptId);
        // 1.2 校验状态为待检验
        if (ObjUtil.notEqual(MesWmOutsourceReceiptStatusEnum.CONFIRMED.getStatus(), receipt.getStatus())) {
            throw exception(WM_OUTSOURCE_RECEIPT_STATUS_ERROR);
        }
        // 1.3 校验行存在
        MesWmOutsourceReceiptLineDO line = outsourceReceiptLineService.getOutsourceReceiptLine(lineId);
        if (line == null) {
            throw exception(WM_OUTSOURCE_RECEIPT_LINE_NOT_EXISTS);
        }

        // 2.1 合格品处理：更新原行（new 一个 DO，避免覆盖更新）
        if (qualifiedQuantity != null && qualifiedQuantity.compareTo(BigDecimal.ZERO) > 0) {
            outsourceReceiptLineService.updateOutsourceReceiptLineDO(new MesWmOutsourceReceiptLineDO()
                    .setId(lineId).setQualityStatus(MesWmQualityStatusEnum.PASS.getStatus())
                    .setIqcId(iqcId).setQuantity(qualifiedQuantity));
        }
        // 2.2 不合格品处理：拷贝原行全部属性，新增一行
        if (unqualifiedQuantity != null && unqualifiedQuantity.compareTo(BigDecimal.ZERO) > 0) {
            MesWmOutsourceReceiptLineDO unqualifiedLine = BeanUtils.toBean(line, MesWmOutsourceReceiptLineDO.class)
                    .setId(null).setQualityStatus(MesWmQualityStatusEnum.FAIL.getStatus())
                    .setIqcId(iqcId).setQuantity(unqualifiedQuantity);
            outsourceReceiptLineService.createOutsourceReceiptLineDO(unqualifiedLine);
        }

        // 3. 直接更新主表状态为"待上架"
        outsourceReceiptMapper.updateById(new MesWmOutsourceReceiptDO()
                .setId(receiptId).setStatus(MesWmOutsourceReceiptStatusEnum.APPROVING.getStatus()));
    }

    private MesWmOutsourceReceiptDO validateOutsourceReceiptExists(Long id) {
        MesWmOutsourceReceiptDO receipt = outsourceReceiptMapper.selectById(id);
        if (receipt == null) {
            throw exception(WM_OUTSOURCE_RECEIPT_NOT_EXISTS);
        }
        return receipt;
    }

    @Override
    public void validateOutsourceReceiptAndLineExists(Long receiptId, Long lineId) {
        // 1. 校验入库单存在
        validateOutsourceReceiptExists(receiptId);
        // 2. 校验行存在且属于该入库单
        MesWmOutsourceReceiptLineDO line = outsourceReceiptLineService.getOutsourceReceiptLine(lineId);
        if (line == null) {
            throw exception(WM_OUTSOURCE_RECEIPT_LINE_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(receiptId, line.getReceiptId())) {
            throw exception(WM_OUTSOURCE_RECEIPT_LINE_NOT_EXISTS);
        }
    }

    /**
     * 校验外协入库单存在且为草稿状态
     */
    private MesWmOutsourceReceiptDO validateOutsourceReceiptExistsAndDraft(Long id) {
        MesWmOutsourceReceiptDO receipt = validateOutsourceReceiptExists(id);
        if (ObjUtil.notEqual(MesWmOutsourceReceiptStatusEnum.PREPARE.getStatus(), receipt.getStatus())) {
            throw exception(WM_OUTSOURCE_RECEIPT_STATUS_NOT_PREPARE);
        }
        return receipt;
    }

    private void validateCodeUnique(Long id, String code) {
        MesWmOutsourceReceiptDO receipt = outsourceReceiptMapper.selectByCode(code);
        if (receipt == null) {
            return;
        }
        if (ObjUtil.notEqual(id, receipt.getId())) {
            throw exception(WM_OUTSOURCE_RECEIPT_CODE_DUPLICATE);
        }
    }

}
