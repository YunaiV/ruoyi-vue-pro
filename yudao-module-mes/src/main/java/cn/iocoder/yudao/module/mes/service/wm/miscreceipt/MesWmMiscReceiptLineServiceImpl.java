package cn.iocoder.yudao.module.mes.service.wm.miscreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.miscreceipt.MesWmMiscReceiptLineMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_MISC_RECEIPT_LINE_NOT_EXISTS;

/**
 * MES 杂项入库单行 Service 实现类
 */
@Service
@Validated
public class MesWmMiscReceiptLineServiceImpl implements MesWmMiscReceiptLineService {

    @Resource
    private MesWmMiscReceiptLineMapper miscReceiptLineMapper;

    @Resource
    @Lazy
    private MesWmMiscReceiptService miscReceiptService;
    @Resource
    @Lazy
    private MesWmMiscReceiptDetailService miscReceiptDetailService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMiscReceiptLine(MesWmMiscReceiptLineSaveReqVO createReqVO) {
        // 1. 校验
        validateLineSaveData(createReqVO);

        // 2. 新增行
        MesWmMiscReceiptLineDO line = BeanUtils.toBean(createReqVO, MesWmMiscReceiptLineDO.class);
        miscReceiptLineMapper.insert(line);

        // 3. 自动创建明细
        createReqVO.setId(line.getId());
        miscReceiptDetailService.createMiscReceiptDetail(createReqVO);
        return line.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMiscReceiptLine(MesWmMiscReceiptLineSaveReqVO updateReqVO) {
        // 1. 校验
        MesWmMiscReceiptLineDO line = validateMiscReceiptLineExists(updateReqVO.getId());
        updateReqVO.setReceiptId(line.getReceiptId());

        // 2. 更新行
        MesWmMiscReceiptLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmMiscReceiptLineDO.class);
        miscReceiptLineMapper.updateById(updateObj);

        // 3. 更新明细（基于行信息）
        miscReceiptDetailService.updateMiscReceiptDetail(updateReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMiscReceiptLine(Long id) {
        // 校验存在
        MesWmMiscReceiptLineDO line = validateMiscReceiptLineExists(id);
        // 校验父单据存在且为可编辑状态
        miscReceiptService.validateMiscReceiptEditable(line.getReceiptId());

        // 删除行
        miscReceiptLineMapper.deleteById(id);
        // 删除明细
        miscReceiptDetailService.deleteMiscReceiptDetailByLineId(id);
    }

    @Override
    public MesWmMiscReceiptLineDO getMiscReceiptLine(Long id) {
        return miscReceiptLineMapper.selectById(id);
    }

    @Override
    public List<MesWmMiscReceiptLineDO> getMiscReceiptLineListByReceiptId(Long receiptId) {
        return miscReceiptLineMapper.selectListByReceiptId(receiptId);
    }

    @Override
    public PageResult<MesWmMiscReceiptLineDO> getMiscReceiptLinePage(MesWmMiscReceiptLinePageReqVO pageReqVO) {
        return miscReceiptLineMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByReceiptId(Long receiptId) {
        // 先删除明细
        miscReceiptDetailService.deleteMiscReceiptDetailByReceiptId(receiptId);
        // 再删除行
        miscReceiptLineMapper.deleteByReceiptId(receiptId);
    }

    @Override
    public MesWmMiscReceiptLineDO validateMiscReceiptLineExists(Long id) {
        MesWmMiscReceiptLineDO line = miscReceiptLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_MISC_RECEIPT_LINE_NOT_EXISTS);
        }
        return line;
    }

    /**
     * 校验行保存数据（物料存在、仓库层级关系）
     */
    private void validateLineSaveData(MesWmMiscReceiptLineSaveReqVO reqVO) {
        // 校验父单据存在且为可编辑状态
        miscReceiptService.validateMiscReceiptEditable(reqVO.getReceiptId());
        // 校验物料存在
        itemService.validateItemExists(reqVO.getItemId());
        // 校验仓库层级关系（仓库 - 库位 - 库区）
        warehouseAreaService.validateWarehouseAreaExists(reqVO.getWarehouseId(),
                reqVO.getLocationId(), reqVO.getAreaId());
    }

}
