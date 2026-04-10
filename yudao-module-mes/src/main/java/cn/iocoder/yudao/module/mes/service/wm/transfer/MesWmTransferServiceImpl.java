package cn.iocoder.yudao.module.mes.service.wm.transfer;

import cn.hutool.core.collection.CollUtil;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.MesWmTransferPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.MesWmTransferSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.transfer.MesWmTransferMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransferStatusEnum;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
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
 * MES 转移单 Service 实现类
 */
@Service
@Validated
public class MesWmTransferServiceImpl implements MesWmTransferService {

    @Resource
    private MesWmTransferMapper transferMapper;

    @Resource
    private MesWmTransferLineService transferLineService;
    @Resource
    private MesWmTransferDetailService transferDetailService;
    @Resource
    private MesWmMaterialStockService materialStockService;
    @Resource
    private MesWmTransactionService wmTransactionService;

    @Override
    public Long createTransfer(MesWmTransferSaveReqVO createReqVO) {
        // 校验编码唯一
        validateCodeUnique(null, createReqVO.getCode());

        // 插入
        MesWmTransferDO transfer = BeanUtils.toBean(createReqVO, MesWmTransferDO.class);
        transfer.setStatus(MesWmTransferStatusEnum.PREPARE.getStatus());
        transferMapper.insert(transfer);
        return transfer.getId();
    }

    @Override
    public void updateTransfer(MesWmTransferSaveReqVO updateReqVO) {
        // 校验存在 + 草稿状态
        validateTransferExistsAndDraft(updateReqVO.getId());
        // 校验编码唯一
        validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        MesWmTransferDO updateObj = BeanUtils.toBean(updateReqVO, MesWmTransferDO.class);
        transferMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransfer(Long id) {
        // 校验存在 + 草稿状态
        validateTransferExistsAndDraft(id);

        // 级联删除明细和行
        transferDetailService.deleteTransferDetailByTransferId(id);
        transferLineService.deleteTransferLineByTransferId(id);
        // 删除
        transferMapper.deleteById(id);
    }

    @Override
    public MesWmTransferDO getTransfer(Long id) {
        return transferMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmTransferDO> getTransferPage(MesWmTransferPageReqVO pageReqVO) {
        return transferMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTransfer(Long id) {
        // 校验存在 + 草稿状态
        MesWmTransferDO transfer = validateTransferExistsAndDraft(id);
        List<MesWmTransferLineDO> lines = transferLineService.getTransferLineListByTransferId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_TRANSFER_NO_LINE);
        }

        // 配送模式下，提交后如果未确认，则进入待确认状态；否则直接进入待上架状态
        if (Boolean.TRUE.equals(transfer.getDeliveryFlag()) && !Boolean.TRUE.equals(transfer.getConfirmFlag())) {
            transfer.setStatus(MesWmTransferStatusEnum.UNCONFIRMED.getStatus()).setConfirmFlag(false);
            // DONE @AI：冻结库存属于主单状态流转的一部分，保留在主单 service 统一处理
            freezeTransferLineStocks(id, true);
        } else {
            transfer.setStatus(MesWmTransferStatusEnum.APPROVING.getStatus());
        }
        transferMapper.updateById(transfer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmTransfer(Long id) {
        // 校验存在 + 待确认状态
        MesWmTransferDO transfer = validateTransferExistsAndConfirm(id);

        // 更新状态：待确认 -> 待上架
        transfer.setStatus(MesWmTransferStatusEnum.APPROVING.getStatus());
        transfer.setConfirmFlag(true);
        transferMapper.updateById(transfer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockTransfer(Long id) {
        // 1.1 校验存在
        MesWmTransferDO transfer = validateTransferExistsAndApproving(id);
        List<MesWmTransferLineDO> lines = transferLineService.getTransferLineListByTransferId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_TRANSFER_NO_LINE);
        }
        // 1.2 检查每个行的明细数量是否完成上架
        for (MesWmTransferLineDO line : lines) {
            List<MesWmTransferDetailDO> details = transferDetailService.getTransferDetailListByLineId(line.getId());
            BigDecimal totalDetailQty = CollectionUtils.getSumValue(details,
                    MesWmTransferDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
            if (line.getQuantity() != null && totalDetailQty.compareTo(line.getQuantity()) != 0) {
                throw exception(WM_TRANSFER_DETAIL_QUANTITY_MISMATCH);
            }
        }

        // 2. 更新状态：待上架 -> 待执行
        transfer.setStatus(MesWmTransferStatusEnum.APPROVED.getStatus());
        transferMapper.updateById(transfer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishTransfer(Long id) {
        // 1. 校验存在 + 待执行状态
        MesWmTransferDO transfer = validateTransferExistsAndApproved(id);
        List<MesWmTransferLineDO> lines = transferLineService.getTransferLineListByTransferId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_TRANSFER_NO_LINE);
        }

        // 2. 创建库存事务（调拨移出 + 调拨移入）
        createTransferTransactions(transfer, lines);

        // 3. 更新状态：待执行 -> 已完成
        transfer.setStatus(MesWmTransferStatusEnum.FINISHED.getStatus());
        transferMapper.updateById(transfer);

        // 4. 配送模式下，执行完成后解除来源库存冻结
        if (Boolean.TRUE.equals(transfer.getDeliveryFlag())) {
            freezeTransferLineStocks(id, false);
        }
    }

    /**
     * 创建调拨事务：基于 detail (明细) 的视角，不是纯粹的 line（行）的视角。
     * 因为明细才代表了实际调往各个目标仓位的动作，所以每条明細产生一笔出库（MOVE_OUT）和一笔入库（MOVE_IN）。
     */
    private void createTransferTransactions(MesWmTransferDO transfer, List<MesWmTransferLineDO> lines) {
        // 1. 预加载所有明细，按 lineId 分组，避免 N+1 查询
        List<MesWmTransferDetailDO> allDetails = transferDetailService.getTransferDetailListByTransferId(transfer.getId());
        Map<Long, List<MesWmTransferDetailDO>> detailMap = CollectionUtils.convertMultiMap(
                allDetails, MesWmTransferDetailDO::getLineId);
        // 2. 遍历行和对应的明细，创建事务
        for (MesWmTransferLineDO line : lines) {
            List<MesWmTransferDetailDO> details = detailMap.getOrDefault(line.getId(), Collections.emptyList());
            for (MesWmTransferDetailDO detail : details) {
                // 2.1 先执行出库：调拨移出（从源仓库扣减库存），出库数量基于当前明细的数量
                Long outTransactionId = wmTransactionService.createTransaction(new MesWmTransactionSaveReqDTO()
                        .setType(MesWmTransactionTypeEnum.MOVE_OUT.getType()).setItemId(line.getItemId())
                        .setQuantity(detail.getQuantity().negate()) // 库存减少（按 detail 的数量出）
                        .setBatchId(line.getBatchId())
                        .setWarehouseId(line.getFromWarehouseId()).setLocationId(line.getFromLocationId()).setAreaId(line.getFromAreaId())
                        .setBizType(MesBizTypeConstants.WM_TRANSFER_OUT).setBizId(transfer.getId())
                        .setBizCode(transfer.getCode()).setBizLineId(line.getId()));
                // 2.2 再执行入库：调拨移入（向目标仓库增加库存），入库数量等于相同明细的数量
                wmTransactionService.createTransaction(new MesWmTransactionSaveReqDTO()
                        .setType(MesWmTransactionTypeEnum.MOVE_IN.getType()).setItemId(line.getItemId()) // 物料信息来自 line
                        .setQuantity(detail.getQuantity()) // 库存增加（按 detail 的数量入）
                        .setBatchId(line.getBatchId()) // 批次信息来自 line
                        .setWarehouseId(detail.getToWarehouseId()).setLocationId(detail.getToLocationId()).setAreaId(detail.getToAreaId())
                        .setBizType(MesBizTypeConstants.WM_TRANSFER_IN).setBizId(transfer.getId())
                        .setBizCode(transfer.getCode()).setBizLineId(line.getId())
                        .setRelatedTransactionId(outTransactionId)); // 关联本次的具体出库事务
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTransfer(Long id) {
        // 校验存在 + 非已完成/已取消状态
        MesWmTransferDO transfer = validateTransferExistsAndNotFinished(id);

        // 配送模式下，取消时需解除冻结
        if (Boolean.TRUE.equals(transfer.getDeliveryFlag())) {
            freezeTransferLineStocks(id, false);
        }

        // 更新状态 -> 已取消
        transfer.setStatus(MesWmTransferStatusEnum.CANCELED.getStatus());
        transferMapper.updateById(transfer);
    }

    @Override
    public MesWmTransferDO validateTransferEditable(Long id) {
        MesWmTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(WM_TRANSFER_NOT_EXISTS);
        }
        if (!MesWmTransferStatusEnum.PREPARE.getStatus().equals(transfer.getStatus())) {
            throw exception(WM_TRANSFER_NOT_EDITABLE);
        }
        return transfer;
    }

    // ==================== 私有方法 ====================

    private void validateCodeUnique(Long id, String code) {
        MesWmTransferDO transfer = transferMapper.selectByCode(code);
        if (transfer == null) {
            return;
        }
        // 如果 id 为空，说明是新增，存在同名则报错
        if (id == null) {
            throw exception(WM_TRANSFER_CODE_DUPLICATE);
        }
        // 如果 id 不为空，说明是修改，存在同名且不是自己则报错
        if (!transfer.getId().equals(id)) {
            throw exception(WM_TRANSFER_CODE_DUPLICATE);
        }
    }

    private MesWmTransferDO validateTransferExistsAndDraft(Long id) {
        MesWmTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(WM_TRANSFER_NOT_EXISTS);
        }
        if (!MesWmTransferStatusEnum.PREPARE.getStatus().equals(transfer.getStatus())) {
            throw exception(WM_TRANSFER_NOT_DRAFT);
        }
        return transfer;
    }

    private MesWmTransferDO validateTransferExistsAndConfirm(Long id) {
        MesWmTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(WM_TRANSFER_NOT_EXISTS);
        }
        if (!MesWmTransferStatusEnum.UNCONFIRMED.getStatus().equals(transfer.getStatus())) {
            throw exception(WM_TRANSFER_NOT_CONFIRMED);
        }
        return transfer;
    }

    private MesWmTransferDO validateTransferExistsAndApproving(Long id) {
        MesWmTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(WM_TRANSFER_NOT_EXISTS);
        }
        if (!MesWmTransferStatusEnum.APPROVING.getStatus().equals(transfer.getStatus())) {
            throw exception(WM_TRANSFER_NOT_APPROVING);
        }
        return transfer;
    }

    private MesWmTransferDO validateTransferExistsAndApproved(Long id) {
        MesWmTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(WM_TRANSFER_NOT_EXISTS);
        }
        if (!MesWmTransferStatusEnum.APPROVED.getStatus().equals(transfer.getStatus())) {
            throw exception(WM_TRANSFER_NOT_APPROVED);
        }
        return transfer;
    }

    private MesWmTransferDO validateTransferExistsAndNotFinished(Long id) {
        MesWmTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(WM_TRANSFER_NOT_EXISTS);
        }
        if (MesWmTransferStatusEnum.FINISHED.getStatus().equals(transfer.getStatus())
                || MesWmTransferStatusEnum.CANCELED.getStatus().equals(transfer.getStatus())) {
            throw exception(WM_TRANSFER_ALREADY_FINISHED);
        }
        return transfer;
    }

    private void freezeTransferLineStocks(Long transferId, boolean frozen) {
        List<MesWmTransferLineDO> lines = transferLineService.getTransferLineListByTransferId(transferId);
        if (CollUtil.isEmpty(lines)) {
            return;
        }
        List<Long> materialStockIds = convertList(lines,
                MesWmTransferLineDO::getMaterialStockId, line -> line.getMaterialStockId() != null);
        materialStockService.updateMaterialStockFrozen(materialStockIds, frozen);
    }

}
