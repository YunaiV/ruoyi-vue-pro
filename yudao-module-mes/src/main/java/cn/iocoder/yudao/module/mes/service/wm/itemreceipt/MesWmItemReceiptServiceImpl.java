package cn.iocoder.yudao.module.mes.service.wm.itemreceipt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.MesWmItemReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.MesWmItemReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemreceipt.MesWmItemReceiptMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmItemReceiptStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcService;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 采购入库单 Service 实现类
 */
@Service
@Validated
public class MesWmItemReceiptServiceImpl implements MesWmItemReceiptService {

    @Resource
    private MesWmItemReceiptMapper itemReceiptMapper;

    @Resource
    private MesWmItemReceiptLineService itemReceiptLineService;
    @Resource
    private MesWmItemReceiptDetailService itemReceiptDetailService;
    @Resource
    @Lazy
    private MesWmArrivalNoticeService arrivalNoticeService;
    @Resource
    private MesMdVendorService vendorService;
    @Resource
    @Lazy
    private MesQcIqcService iqcService;
    @Resource
    private MesWmTransactionService wmTransactionService;

    @Override
    public Long createItemReceipt(MesWmItemReceiptSaveReqVO createReqVO) {
        // 校验数据
        validateItemReceiptSaveData(createReqVO);

        // 插入
        MesWmItemReceiptDO receipt = BeanUtils.toBean(createReqVO, MesWmItemReceiptDO.class);
        receipt.setStatus(MesWmItemReceiptStatusEnum.PREPARE.getStatus());
        itemReceiptMapper.insert(receipt);
        return receipt.getId();
    }

    @Override
    public void updateItemReceipt(MesWmItemReceiptSaveReqVO updateReqVO) {
        // 校验存在 + 草稿状态
        validateItemReceiptExistsAndDraft(updateReqVO.getId());
        // 校验数据
        validateItemReceiptSaveData(updateReqVO);

        // 更新
        MesWmItemReceiptDO updateObj = BeanUtils.toBean(updateReqVO, MesWmItemReceiptDO.class);
        itemReceiptMapper.updateById(updateObj);
    }

    private void validateItemReceiptSaveData(MesWmItemReceiptSaveReqVO reqVO) {
        // 校验编码唯一
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验供应商存在
        vendorService.validateVendorExistsAndEnable(reqVO.getVendorId());
        // 校验到货通知单存在
        if (reqVO.getNoticeId() != null) {
            MesWmArrivalNoticeDO notice = arrivalNoticeService.validateArrivalNoticeReadyForReceipt(reqVO.getNoticeId());
            if (ObjUtil.notEqual(notice.getVendorId(), reqVO.getVendorId())) {
                throw exception(WM_ARRIVAL_NOTICE_VENDOR_MISMATCH);
            }
        }
        // 校验来料检验单存在
        if (reqVO.getIqcId() != null) {
            iqcService.validateIqcExists(reqVO.getIqcId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItemReceipt(Long id) {
        // 校验存在 + 草稿状态
        validateItemReceiptExistsAndDraft(id);

        // 级联删除明细和行
        itemReceiptDetailService.deleteItemReceiptDetailByReceiptId(id);
        itemReceiptLineService.deleteItemReceiptLineByReceiptId(id);
        // 删除
        itemReceiptMapper.deleteById(id);
    }

    @Override
    public MesWmItemReceiptDO getItemReceipt(Long id) {
        return itemReceiptMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmItemReceiptDO> getItemReceiptPage(MesWmItemReceiptPageReqVO pageReqVO) {
        return itemReceiptMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitItemReceipt(Long id) {
        // 校验存在 + 草稿状态
        validateItemReceiptExistsAndDraft(id);
        // 校验至少有一条行
        List<MesWmItemReceiptLineDO> lines = itemReceiptLineService.getItemReceiptLineListByReceiptId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_ITEM_RECEIPT_NO_LINE);
        }

        // 提交（草稿 → 待上架）
        itemReceiptMapper.updateById(new MesWmItemReceiptDO()
                .setId(id).setStatus(MesWmItemReceiptStatusEnum.APPROVING.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockItemReceipt(Long id) {
        // 校验存在
        MesWmItemReceiptDO receipt = validateItemReceiptExists(id);
        if (ObjUtil.notEqual(MesWmItemReceiptStatusEnum.APPROVING.getStatus(), receipt.getStatus())) {
            throw exception(WM_ITEM_RECEIPT_STATUS_ERROR);
        }
        // 校验每行明细数量之和是否等于行入库数量
        List<MesWmItemReceiptLineDO> lines = itemReceiptLineService.getItemReceiptLineListByReceiptId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_ITEM_RECEIPT_NO_LINE);
        }
        for (MesWmItemReceiptLineDO line : lines) {
            List<MesWmItemReceiptDetailDO> details = itemReceiptDetailService.getItemReceiptDetailListByLineId(line.getId());
            BigDecimal totalDetailQty = CollectionUtils.getSumValue(details,
                    MesWmItemReceiptDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
            if (line.getReceivedQuantity() == null || totalDetailQty.compareTo(line.getReceivedQuantity()) != 0) {
                throw exception(WM_ITEM_RECEIPT_DETAIL_QUANTITY_MISMATCH);
            }
        }

        // 执行上架（待上架 → 待入库）
        itemReceiptMapper.updateById(new MesWmItemReceiptDO()
                .setId(id).setStatus(MesWmItemReceiptStatusEnum.APPROVED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishItemReceipt(Long id) {
        // 1. 校验存在
        MesWmItemReceiptDO receipt = validateItemReceiptExists(id);
        if (ObjUtil.notEqual(MesWmItemReceiptStatusEnum.APPROVED.getStatus(), receipt.getStatus())) {
            throw exception(WM_ITEM_RECEIPT_STATUS_ERROR);
        }

        // 2. 遍历所有明细，创建库存事务（增加库存 + 记录流水）
        createTransactionList(receipt);

        // 3. 更新入库单状态
        itemReceiptMapper.updateById(new MesWmItemReceiptDO()
                .setId(id).setStatus(MesWmItemReceiptStatusEnum.FINISHED.getStatus()));

        // 4. 更新关联的到货通知单状态
        if (receipt.getNoticeId() != null) {
            arrivalNoticeService.finishArrivalNotice(receipt.getNoticeId());
        }
    }

    private void createTransactionList(MesWmItemReceiptDO receipt) {
        List<MesWmItemReceiptDetailDO> details = itemReceiptDetailService.getItemReceiptDetailListByReceiptId(receipt.getId());
        wmTransactionService.createTransactionList(convertList(details, detail -> new MesWmTransactionSaveReqDTO()
                .setType(MesWmTransactionTypeEnum.IN.getType()).setItemId(detail.getItemId())
                .setQuantity(detail.getQuantity()) // 入库数量为正数
                .setBatchId(detail.getBatchId())
                .setWarehouseId(detail.getWarehouseId()).setLocationId(detail.getLocationId()).setAreaId(detail.getAreaId())
                .setVendorId(receipt.getVendorId()).setReceiptTime(receipt.getReceiptDate())
                .setBizType(MesBizTypeConstants.WM_ITEM_RECEIPT_IN).setBizId(receipt.getId())
                .setBizCode(receipt.getCode()).setBizLineId(detail.getLineId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelItemReceipt(Long id) {
        // 校验存在
        MesWmItemReceiptDO receipt = validateItemReceiptExists(id);
        // 已完成和已取消不允许取消
        if (ObjectUtils.equalsAny(receipt.getStatus(),
                MesWmItemReceiptStatusEnum.FINISHED.getStatus(),
                MesWmItemReceiptStatusEnum.CANCELED.getStatus())) {
            throw exception(WM_ITEM_RECEIPT_CANCEL_NOT_ALLOWED);
        }
        // 取消
        itemReceiptMapper.updateById(new MesWmItemReceiptDO()
                .setId(id).setStatus(MesWmItemReceiptStatusEnum.CANCELED.getStatus()));
    }

    @Override
    public MesWmItemReceiptDO validateItemReceiptEditable(Long id) {
        MesWmItemReceiptDO receipt = validateItemReceiptExists(id);
        if (ObjUtil.notEqual(receipt.getStatus(), MesWmItemReceiptStatusEnum.PREPARE.getStatus())
                && ObjUtil.notEqual(receipt.getStatus(), MesWmItemReceiptStatusEnum.APPROVING.getStatus())) {
            throw exception(WM_ITEM_RECEIPT_STATUS_NOT_PREPARE);
        }
        return receipt;
    }

    private MesWmItemReceiptDO validateItemReceiptExists(Long id) {
        MesWmItemReceiptDO receipt = itemReceiptMapper.selectById(id);
        if (receipt == null) {
            throw exception(WM_ITEM_RECEIPT_NOT_EXISTS);
        }
        return receipt;
    }

    /**
     * 校验采购入库单存在且为草稿状态
     */
    private MesWmItemReceiptDO validateItemReceiptExistsAndDraft(Long id) {
        MesWmItemReceiptDO receipt = validateItemReceiptExists(id);
        if (ObjUtil.notEqual(MesWmItemReceiptStatusEnum.PREPARE.getStatus(), receipt.getStatus())) {
            throw exception(WM_ITEM_RECEIPT_STATUS_NOT_PREPARE);
        }
        return receipt;
    }

    private void validateCodeUnique(Long id, String code) {
        MesWmItemReceiptDO receipt = itemReceiptMapper.selectByCode(code);
        if (receipt == null) {
            return;
        }
        if (ObjUtil.notEqual(id, receipt.getId())) {
            throw exception(WM_ITEM_RECEIPT_CODE_DUPLICATE);
        }
    }

    @Override
    public Long getItemReceiptCountByVendorId(Long vendorId) {
        return itemReceiptMapper.selectCountByVendorId(vendorId);
    }

    @Override
    public List<MesWmItemReceiptDO> getItemReceiptListByVendorId(Long vendorId) {
        return itemReceiptMapper.selectListByVendorId(vendorId);
    }

    @Override
    public List<MesWmItemReceiptDO> getItemReceiptList(Collection<Long> ids) {
        return itemReceiptMapper.selectByIds(ids);
    }

}
