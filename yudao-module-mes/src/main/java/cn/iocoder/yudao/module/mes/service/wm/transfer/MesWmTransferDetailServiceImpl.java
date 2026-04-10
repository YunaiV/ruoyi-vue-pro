package cn.iocoder.yudao.module.mes.service.wm.transfer;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.detail.MesWmTransferDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.transfer.MesWmTransferDetailMapper;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_TRANSFER_DETAIL_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_TRANSFER_DETAIL_QUANTITY_EXCEED;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_TRANSFER_DETAIL_MIXED_GOODS;

/**
 * MES 调拨明细 Service 实现类
 */
@Service
@Validated
public class MesWmTransferDetailServiceImpl implements MesWmTransferDetailService {

    @Resource
    private MesWmTransferDetailMapper transferDetailMapper;

    @Resource
    @Lazy
    private MesWmTransferService transferService;
    @Resource
    @Lazy
    private MesWmTransferLineService transferLineService;

    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;

    @Override
    public Long createTransferDetail(MesWmTransferDetailSaveReqVO createReqVO) {
        // 校验
        validateDetailSaveData(createReqVO, null);

        // 插入
        MesWmTransferDetailDO detail = BeanUtils.toBean(createReqVO, MesWmTransferDetailDO.class);
        transferDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateTransferDetail(MesWmTransferDetailSaveReqVO updateReqVO) {
        // 校验存在
        validateTransferDetailExists(updateReqVO.getId());
        // 校验
        validateDetailSaveData(updateReqVO, updateReqVO.getId());

        // 更新
        MesWmTransferDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmTransferDetailDO.class);
        transferDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteTransferDetail(Long id) {
        // 校验存在
        MesWmTransferDetailDO detail = transferDetailMapper.selectById(id);
        if (detail == null) {
            throw exception(WM_TRANSFER_DETAIL_NOT_EXISTS);
        }
        // 校验父转移单可编辑（通过 line 找到 transfer）
        MesWmTransferLineDO line = transferLineService.validateTransferLineExists(detail.getLineId());
        transferService.validateTransferEditable(line.getTransferId());
        // 删除
        transferDetailMapper.deleteById(id);
    }

    @Override
    public MesWmTransferDetailDO getTransferDetail(Long id) {
        return transferDetailMapper.selectById(id);
    }

    @Override
    public List<MesWmTransferDetailDO> getTransferDetailListByLineId(Long lineId) {
        return transferDetailMapper.selectListByLineId(lineId);
    }

    @Override
    public List<MesWmTransferDetailDO> getTransferDetailListByTransferId(Long transferId) {
        return transferDetailMapper.selectListByTransferId(transferId);
    }

    @Override
    public void deleteTransferDetailByTransferId(Long transferId) {
        transferDetailMapper.deleteByTransferId(transferId);
    }

    @Override
    public void deleteTransferDetailByLineId(Long lineId) {
        transferDetailMapper.deleteByLineId(lineId);
    }

    private void validateTransferDetailExists(Long id) {
        if (transferDetailMapper.selectById(id) == null) {
            throw exception(WM_TRANSFER_DETAIL_NOT_EXISTS);
        }
    }

    /**
     * 校验明细保存数据
     *
     * @param reqVO 保存请求
     * @param excludeDetailId 排除的明细 ID（更新时排除自身，新增时传 null）
     */
    private void validateDetailSaveData(MesWmTransferDetailSaveReqVO reqVO, Long excludeDetailId) {
        // 校验父数据存在
        MesWmTransferLineDO line = transferLineService.validateTransferLineExists(reqVO.getLineId());
        // 校验仓库、库区、库位的关联关系
        warehouseAreaService.validateWarehouseAreaExists(
                reqVO.getToWarehouseId(), reqVO.getToLocationId(), reqVO.getToAreaId());
        // 校验明细总数量不超过行数量
        validateDetailQuantityNotExceed(reqVO.getLineId(), reqVO.getQuantity(), excludeDetailId, line);
        // 校验不能混货：同一个目标仓位下，不能存在不同物料的明细
        validateNoMixedGoods(reqVO.getLineId(), reqVO.getToWarehouseId(),
                reqVO.getToLocationId(), reqVO.getToAreaId(), line.getItemId(), excludeDetailId);
    }

    /**
     * 校验明细总数量不超过行数量
     *
     * @param lineId 行 ID
     * @param newQuantity 本次新增/修改的数量
     * @param excludeDetailId 排除的明细 ID（更新时排除自身，新增时传 null）
     * @param line 调拨单行
     */
    private void validateDetailQuantityNotExceed(Long lineId, BigDecimal newQuantity,
                                                 Long excludeDetailId, MesWmTransferLineDO line) {
        // 计算已有明细总数量（排除自身）
        List<MesWmTransferDetailDO> details = transferDetailMapper.selectListByLineId(lineId);
        BigDecimal existingTotal = CollectionUtils.getSumValue(details,
                detail -> excludeDetailId != null && excludeDetailId.equals(detail.getId())
                        ? BigDecimal.ZERO : detail.getQuantity(),
                BigDecimal::add, BigDecimal.ZERO);
        // 校验：已有 + 本次 <= 行数量
        if (existingTotal.add(newQuantity).compareTo(line.getQuantity()) > 0) {
            throw exception(WM_TRANSFER_DETAIL_QUANTITY_EXCEED);
        }
    }

    /**
     * 校验不能混货：同一转移单下，同一个目标仓位（仓库+库区+库位）只能放同一个物料
     *
     * @param lineId 当前行 ID（用于排除同行明细，因为同行物料相同）
     * @param toWarehouseId 目标仓库 ID
     * @param toLocationId 目标库区 ID
     * @param toAreaId 目标库位 ID
     * @param itemId 当前物料 ID
     * @param excludeDetailId 排除的明细 ID（更新时排除自身）
     */
    private void validateNoMixedGoods(Long lineId, Long toWarehouseId, Long toLocationId,
                                      Long toAreaId, Long itemId, Long excludeDetailId) {
        // 1. 获取当前转移单下的所有明细
        // 说明：因为无法直接从参数拿到 transferId，所以先通过 lineId 查询当前行，进而获取 transferId 和所有明细
        MesWmTransferLineDO currentLine = transferLineService.validateTransferLineExists(lineId);
        List<MesWmTransferDetailDO> allDetails = transferDetailMapper.selectListByTransferId(currentLine.getTransferId());

        // 2. 遍历所有明细，校验同一个目标仓位不能有不同的物料
        for (MesWmTransferDetailDO detail : allDetails) {
            // 2.1 排除当前正在编辑的明细（更新时）
            if (excludeDetailId != null && excludeDetailId.equals(detail.getId())) {
                continue;
            }
            // 2.2 排除同行的明细（同行属于同一个转移单行，物料必定相同，无需校验）
            if (detail.getLineId().equals(lineId)) {
                continue;
            }
            // 2.3 目标仓位不同，则不会混货，直接跳过
            if (ObjUtil.notEqual(detail.getToWarehouseId(), toWarehouseId)
                    || ObjUtil.notEqual(detail.getToLocationId(), toLocationId)
                    || ObjUtil.notEqual(detail.getToAreaId(), toAreaId)) {
                continue;
            }
            // 2.4 重点：如果目标仓位相同，则必须保证物料也相同
            MesWmTransferLineDO otherLine = transferLineService.validateTransferLineExists(detail.getLineId());
            if (ObjUtil.notEqual(otherLine.getItemId(), itemId)) {
                throw exception(WM_TRANSFER_DETAIL_MIXED_GOODS);
            }
        }
    }

}
