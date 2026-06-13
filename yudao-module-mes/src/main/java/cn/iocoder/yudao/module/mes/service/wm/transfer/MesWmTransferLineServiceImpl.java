package cn.iocoder.yudao.module.mes.service.wm.transfer;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.line.MesWmTransferLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.transfer.MesWmTransferLineMapper;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_MATERIAL_STOCK_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_TRANSFER_LINE_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_TRANSFER_LINE_QUANTITY_EXCEED_STOCK;

/**
 * MES 转移单行 Service 实现类
 */
@Service
@Validated
public class MesWmTransferLineServiceImpl implements MesWmTransferLineService {

    @Resource
    private MesWmTransferLineMapper transferLineMapper;

    @Resource
    @Lazy
    private MesWmTransferService transferService;
    @Resource
    @Lazy
    private MesWmTransferDetailService transferDetailService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;
    @Resource
    private MesWmMaterialStockService materialStockService;

    @Override
    public Long createTransferLine(MesWmTransferLineSaveReqVO createReqVO) {
        // 校验
        validateLineSaveData(createReqVO);

        // 插入
        MesWmTransferLineDO line = BeanUtils.toBean(createReqVO, MesWmTransferLineDO.class);
        transferLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateTransferLine(MesWmTransferLineSaveReqVO updateReqVO) {
        // 校验存在
        validateTransferLineExists(updateReqVO.getId());
        // 校验
        validateLineSaveData(updateReqVO);

        // 更新
        MesWmTransferLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmTransferLineDO.class);
        transferLineMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransferLine(Long id) {
        // 校验存在
        MesWmTransferLineDO line = validateTransferLineExists(id);
        // 校验父数据可编辑
        transferService.validateTransferEditable(line.getTransferId());

        // 级联删除该行下所有明细
        transferDetailService.deleteTransferDetailByLineId(id);
        // 删除行
        transferLineMapper.deleteById(id);
    }

    @Override
    public MesWmTransferLineDO getTransferLine(Long id) {
        return transferLineMapper.selectById(id);
    }

    @Override
    public List<MesWmTransferLineDO> getTransferLineListByTransferId(Long transferId) {
        return transferLineMapper.selectListByTransferId(transferId);
    }

    @Override
    public void deleteTransferLineByTransferId(Long transferId) {
        transferLineMapper.deleteByTransferId(transferId);
    }

    @Override
    public MesWmTransferLineDO validateTransferLineExists(Long id) {
        MesWmTransferLineDO line = transferLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_TRANSFER_LINE_NOT_EXISTS);
        }
        return line;
    }

    private void validateLineSaveData(MesWmTransferLineSaveReqVO reqVO) {
        // 校验父数据可编辑
        transferService.validateTransferEditable(reqVO.getTransferId());
        // 校验产品存在
        itemService.validateItemExistsAndEnable(reqVO.getItemId());
        // 校验来源仓库、库区、库位的关联关系
        warehouseAreaService.validateWarehouseAreaExists(reqVO.getFromWarehouseId(),
                reqVO.getFromLocationId(), reqVO.getFromAreaId());
        // 校验库存记录存在，且转移数量不超过库存数量
        MesWmMaterialStockDO stock = materialStockService.getMaterialStock(reqVO.getMaterialStockId());
        if (stock == null) {
            throw exception(WM_MATERIAL_STOCK_NOT_EXISTS);
        }
        if (stock.getQuantity() != null && reqVO.getQuantity().compareTo(stock.getQuantity()) > 0) {
            throw exception(WM_TRANSFER_LINE_QUANTITY_EXCEED_STOCK);
        }
    }

}
