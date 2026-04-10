package cn.iocoder.yudao.module.mes.service.wm.itemreceipt;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.detail.MesWmItemReceiptDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemreceipt.MesWmItemReceiptDetailMapper;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 采购入库明细 Service 实现类
 */
@Service
@Validated
public class MesWmItemReceiptDetailServiceImpl implements MesWmItemReceiptDetailService {

    @Resource
    private MesWmItemReceiptDetailMapper itemReceiptDetailMapper;

    @Resource
    @Lazy
    private MesWmItemReceiptService itemReceiptService;

    @Resource
    @Lazy
    private MesWmItemReceiptLineService itemReceiptLineService;

    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;
    @Resource
    @Lazy
    private MesWmMaterialStockService materialStockService;

    @Override
    public Long createItemReceiptDetail(MesWmItemReceiptDetailSaveReqVO createReqVO) {
        // 1. 校验数据
        itemReceiptService.validateItemReceiptEditable(createReqVO.getReceiptId());
        validateItemReceiptDetailSaveData(createReqVO);

        // 2. 插入
        MesWmItemReceiptDetailDO detail = BeanUtils.toBean(createReqVO, MesWmItemReceiptDetailDO.class);
        itemReceiptDetailMapper.insert(detail);

        // 3. 插入后校验数量是否超出
        validateDetailQuantityNotExceed(createReqVO.getLineId());
        return detail.getId();
    }

    @Override
    public void updateItemReceiptDetail(MesWmItemReceiptDetailSaveReqVO updateReqVO) {
        // 1.1 校验存在
        MesWmItemReceiptDetailDO detail = validateItemReceiptDetailExists(updateReqVO.getId());
        // 1.2 校验数据
        updateReqVO.setReceiptId(detail.getReceiptId());
        validateItemReceiptDetailSaveData(updateReqVO);

        // 2. 更新
        MesWmItemReceiptDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmItemReceiptDetailDO.class);
        itemReceiptDetailMapper.updateById(updateObj);

        // 3. 更新后校验数量是否超出
        validateDetailQuantityNotExceed(detail.getLineId());
    }

    private void validateItemReceiptDetailSaveData(MesWmItemReceiptDetailSaveReqVO reqVO) {
        // 校验父单据存在且为可编辑状态
        itemReceiptService.validateItemReceiptEditable(reqVO.getReceiptId());
        // 校验库区关系
        warehouseAreaService.validateWarehouseAreaExists(reqVO.getWarehouseId(), reqVO.getLocationId(), reqVO.getAreaId());
        // 校验库位物料/批次混放规则
        materialStockService.checkAreaMixingRule(reqVO.getAreaId(), reqVO.getItemId(), reqVO.getBatchId());
    }

    @Override
    public void deleteItemReceiptDetail(Long id) {
        // 1.1 校验存在
        MesWmItemReceiptDetailDO detail = validateItemReceiptDetailExists(id);
        // 1.2 校验父单据存在且为可编辑状态
        itemReceiptService.validateItemReceiptEditable(detail.getReceiptId());

        // 删除
        itemReceiptDetailMapper.deleteById(id);
    }

    @Override
    public MesWmItemReceiptDetailDO getItemReceiptDetail(Long id) {
        return itemReceiptDetailMapper.selectById(id);
    }

    @Override
    public List<MesWmItemReceiptDetailDO> getItemReceiptDetailListByReceiptId(Long receiptId) {
        return itemReceiptDetailMapper.selectListByReceiptId(receiptId);
    }

    @Override
    public List<MesWmItemReceiptDetailDO> getItemReceiptDetailListByLineId(Long lineId) {
        return itemReceiptDetailMapper.selectListByLineId(lineId);
    }

    @Override
    public void deleteItemReceiptDetailByLineId(Long lineId) {
        itemReceiptDetailMapper.deleteByLineId(lineId);
    }

    @Override
    public void deleteItemReceiptDetailByReceiptId(Long receiptId) {
        itemReceiptDetailMapper.deleteByReceiptId(receiptId);
    }

    private MesWmItemReceiptDetailDO validateItemReceiptDetailExists(Long id) {
        MesWmItemReceiptDetailDO detail = itemReceiptDetailMapper.selectById(id);
        if (detail == null) {
            throw exception(WM_ITEM_RECEIPT_DETAIL_NOT_EXISTS);
        }
        return detail;
    }

    /**
     * 校验行下所有明细的数量之和不超过行的入库数量
     *
     * @param lineId 行编号
     */
    private void validateDetailQuantityNotExceed(Long lineId) {
        MesWmItemReceiptLineDO line = itemReceiptLineService.getItemReceiptLine(lineId);
        if (line == null || line.getReceivedQuantity() == null) {
            return;
        }
        List<MesWmItemReceiptDetailDO> details = itemReceiptDetailMapper.selectListByLineId(lineId);
        BigDecimal totalDetailQty = CollectionUtils.getSumValue(details,
                MesWmItemReceiptDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
        if (totalDetailQty.compareTo(line.getReceivedQuantity()) > 0) {
            throw exception(WM_ITEM_RECEIPT_DETAIL_QUANTITY_EXCEED);
        }
    }

}
