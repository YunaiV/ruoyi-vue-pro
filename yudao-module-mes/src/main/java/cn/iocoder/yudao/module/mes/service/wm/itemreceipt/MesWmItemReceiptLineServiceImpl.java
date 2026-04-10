package cn.iocoder.yudao.module.mes.service.wm.itemreceipt;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo.MesWmBatchGenerateReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line.MesWmItemReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line.MesWmItemReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemreceipt.MesWmItemReceiptLineMapper;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeLineService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 采购入库单行 Service 实现类
 */
@Service
@Validated
public class MesWmItemReceiptLineServiceImpl implements MesWmItemReceiptLineService {

    @Resource
    private MesWmItemReceiptLineMapper itemReceiptLineMapper;

    @Resource
    @Lazy
    private MesWmItemReceiptService itemReceiptService;
    @Resource
    private MesWmItemReceiptDetailService itemReceiptDetailService;
    @Resource
    @Lazy
    private MesWmArrivalNoticeLineService arrivalNoticeLineService;
    @Resource
    private MesWmBatchService batchService;

    @Override
    public Long createItemReceiptLine(MesWmItemReceiptLineSaveReqVO createReqVO) {
        // 1. 校验父单据存在且为可编辑状态
        MesWmItemReceiptDO receipt = itemReceiptService.validateItemReceiptEditable(createReqVO.getReceiptId());
        // 2. 校验数据
        validateItemReceiptLineSaveData(receipt, createReqVO);

        // 3.1 创建入库单行
        MesWmItemReceiptLineDO line = BeanUtils.toBean(createReqVO, MesWmItemReceiptLineDO.class);
        // 3.2 生成或获取批次
        MesWmBatchDO batch = generateOrGetBatch(receipt, createReqVO);
        if (batch != null) {
            line.setBatchId(batch.getId());
            line.setBatchCode(batch.getCode());
        }
        // 3.3 插入数据库
        itemReceiptLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateItemReceiptLine(MesWmItemReceiptLineSaveReqVO updateReqVO) {
        // 1.1 校验存在
        MesWmItemReceiptLineDO line = validateItemReceiptLineExists(updateReqVO.getId());
        // 1.2 校验父单据存在且为可编辑状态
        MesWmItemReceiptDO receipt = itemReceiptService.validateItemReceiptEditable(line.getReceiptId());
        // 1.3 校验数据
        validateItemReceiptLineSaveData(receipt, updateReqVO);

        // 2.1 更新
        MesWmItemReceiptLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmItemReceiptLineDO.class);
        // 2.2 生成或获取批次
        MesWmBatchDO batch = generateOrGetBatch(receipt, updateReqVO);
        if (batch != null) {
            updateObj.setBatchId(batch.getId()).setBatchCode(batch.getCode());
        }
        // 2.3 更新数据库
        itemReceiptLineMapper.updateById(updateObj);
    }

    private void validateItemReceiptLineSaveData(MesWmItemReceiptDO receipt, MesWmItemReceiptLineSaveReqVO reqVO) {
        // 校验关联到货通知单行
        validateArrivalNoticeLine(receipt, reqVO.getArrivalNoticeLineId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItemReceiptLine(Long id) {
        // 1.1 校验存在
        MesWmItemReceiptLineDO line = validateItemReceiptLineExists(id);
        // 1.2 校验父单据存在且为可编辑状态
        itemReceiptService.validateItemReceiptEditable(line.getReceiptId());

        // 2.1 级联删除明细
        itemReceiptDetailService.deleteItemReceiptDetailByLineId(id);
        // 2.2 删除
        itemReceiptLineMapper.deleteById(id);
    }

    @Override
    public MesWmItemReceiptLineDO getItemReceiptLine(Long id) {
        return itemReceiptLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmItemReceiptLineDO> getItemReceiptLinePage(MesWmItemReceiptLinePageReqVO pageReqVO) {
        // 如果传了 vendorId，先查出该供应商的所有入库单 ID，设置到 receiptIds
        if (pageReqVO.getReceiptId() == null) {
            List<MesWmItemReceiptDO> receipts = itemReceiptService.getItemReceiptListByVendorId(pageReqVO.getVendorId());
            if (CollUtil.isEmpty(receipts)) {
                return PageResult.empty();
            }
            pageReqVO.setReceiptIds(convertList(receipts, MesWmItemReceiptDO::getId));
        }
        // 查询分页
        return itemReceiptLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmItemReceiptLineDO> getItemReceiptLineListByReceiptId(Long receiptId) {
        return itemReceiptLineMapper.selectListByReceiptId(receiptId);
    }

    @Override
    public void deleteItemReceiptLineByReceiptId(Long receiptId) {
        itemReceiptLineMapper.deleteByReceiptId(receiptId);
    }

    /**
     * 校验到货通知单行
     *
     * @param receipt 入库单
     * @param arrivalNoticeLineId 到货通知单行编号
     */
    private void validateArrivalNoticeLine(MesWmItemReceiptDO receipt, Long arrivalNoticeLineId) {
        // 情况一：如果入库单关联了到货通知单，则必须关联到货通知单行
        if (receipt.getNoticeId() != null) {
            if (arrivalNoticeLineId == null) {
                throw exception(WM_ITEM_RECEIPT_LINE_ARRIVAL_NOTICE_LINE_REQUIRED);
            }
            arrivalNoticeLineService.validateArrivalNoticeLineExists(
                    arrivalNoticeLineId, receipt.getNoticeId());
            return;
        }

        // 情况二：如果入库单没有关联到货通知单，则不允许关联到货通知单行
        if (arrivalNoticeLineId != null) {
            throw exception(WM_ITEM_RECEIPT_LINE_ARRIVAL_NOTICE_LINE_NOT_ALLOWED);
        }
    }

    private MesWmItemReceiptLineDO validateItemReceiptLineExists(Long id) {
        MesWmItemReceiptLineDO line = itemReceiptLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_ITEM_RECEIPT_LINE_NOT_EXISTS);
        }
        return line;
    }

    /**
     * 生成或获取批次
     *
     * @param receipt 入库单
     * @param reqVO 入库单行请求VO
     * @return 批次记录（如果物料未启用批次管理则返回 null）
     */
    private MesWmBatchDO generateOrGetBatch(MesWmItemReceiptDO receipt, MesWmItemReceiptLineSaveReqVO reqVO) {
        // 构建批次参数 VO
        MesWmBatchGenerateReqVO batchReqVO = new MesWmBatchGenerateReqVO();
        // 从入库单行获取
        batchReqVO.setItemId(reqVO.getItemId()).setProduceDate(reqVO.getProductionDate())
                .setExpireDate(reqVO.getExpireDate()).setLotNumber(reqVO.getLotNumber());
        // 从父单据获取
        batchReqVO.setVendorId(receipt.getVendorId()).setReceiptDate(receipt.getReceiptDate())
                .setPurchaseOrderCode(receipt.getPurchaseOrderCode());

        // 生成或获取批次
        return batchService.getOrGenerateBatchCode(batchReqVO);
    }

}
