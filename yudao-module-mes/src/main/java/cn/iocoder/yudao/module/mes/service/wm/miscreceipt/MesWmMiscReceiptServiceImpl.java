package cn.iocoder.yudao.module.mes.service.wm.miscreceipt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.MesWmMiscReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.MesWmMiscReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.miscreceipt.MesWmMiscReceiptMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmMiscReceiptStatusEnum;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 杂项入库单 Service 实现类
 */
@Service
@Validated
@Slf4j
public class MesWmMiscReceiptServiceImpl implements MesWmMiscReceiptService {

    @Resource
    private MesWmMiscReceiptMapper miscReceiptMapper;

    @Resource
    @Lazy
    private MesWmMiscReceiptLineService miscReceiptLineService;
    @Resource
    private MesWmTransactionService wmTransactionService;

    @Override
    public Long createMiscReceipt(MesWmMiscReceiptSaveReqVO createReqVO) {
        // 校验编码唯一
        validateCodeUnique(null, createReqVO.getCode());

        // 插入
        MesWmMiscReceiptDO receipt = BeanUtils.toBean(createReqVO, MesWmMiscReceiptDO.class);
        receipt.setStatus(MesWmMiscReceiptStatusEnum.PREPARE.getStatus());
        miscReceiptMapper.insert(receipt);
        return receipt.getId();
    }

    @Override
    public void updateMiscReceipt(MesWmMiscReceiptSaveReqVO updateReqVO) {
        // 校验存在 + 草稿状态
        validateMiscReceiptExistsAndPrepare(updateReqVO.getId());
        // 校验编码唯一
        validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        MesWmMiscReceiptDO updateObj = BeanUtils.toBean(updateReqVO, MesWmMiscReceiptDO.class);
        miscReceiptMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMiscReceipt(Long id) {
        // 校验存在 + 草稿状态
        validateMiscReceiptExistsAndPrepare(id);

        // 级联删除行和明细
        miscReceiptLineService.deleteByReceiptId(id);
        // 删除主表
        miscReceiptMapper.deleteById(id);
    }

    @Override
    public MesWmMiscReceiptDO getMiscReceipt(Long id) {
        return miscReceiptMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmMiscReceiptDO> getMiscReceiptPage(MesWmMiscReceiptPageReqVO pageReqVO) {
        return miscReceiptMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMiscReceipt(Long id) {
        // 校验存在 + 草稿状态
        validateMiscReceiptExistsAndPrepare(id);
        // 校验至少有一条行
        List<MesWmMiscReceiptLineDO> lines = miscReceiptLineService.getMiscReceiptLineListByReceiptId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_MISC_RECEIPT_NO_LINE);
        }

        // 提交审批（草稿 → 已审批）
        miscReceiptMapper.updateById(new MesWmMiscReceiptDO()
                .setId(id).setStatus(MesWmMiscReceiptStatusEnum.APPROVED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishMiscReceipt(Long id) {
        // 校验存在
        MesWmMiscReceiptDO receipt = validateMiscReceiptExists(id);
        if (ObjUtil.notEqual(MesWmMiscReceiptStatusEnum.APPROVED.getStatus(), receipt.getStatus())) {
            throw exception(WM_MISC_RECEIPT_STATUS_NOT_APPROVED);
        }

        // 2. 遍历所有行，创建库存事务（增加库存 + 记录流水）
        createTransactionList(receipt);

        // 3. 执行入库（已审批 → 已完成）
        miscReceiptMapper.updateById(new MesWmMiscReceiptDO()
                .setId(id).setStatus(MesWmMiscReceiptStatusEnum.FINISHED.getStatus()));
    }

    private void createTransactionList(MesWmMiscReceiptDO receipt) {
        List<MesWmMiscReceiptLineDO> lines = miscReceiptLineService.getMiscReceiptLineListByReceiptId(receipt.getId());
        wmTransactionService.createTransactionList(convertList(lines, line -> new MesWmTransactionSaveReqDTO()
                .setType(MesWmTransactionTypeEnum.IN.getType()).setItemId(line.getItemId())
                .setQuantity(line.getQuantity()) // 入库数量为正数
                .setWarehouseId(line.getWarehouseId()).setLocationId(line.getLocationId()).setAreaId(line.getAreaId())
                .setBizType(MesBizTypeConstants.WM_MISC_RECPT).setBizId(receipt.getId()).setBizCode(receipt.getCode()).setBizLineId(line.getId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMiscReceipt(Long id) {
        // 校验存在
        MesWmMiscReceiptDO receipt = validateMiscReceiptExists(id);

        // 已完成和已取消状态不允许取消
        if (ObjUtil.equal(MesWmMiscReceiptStatusEnum.FINISHED.getStatus(), receipt.getStatus())
                || ObjUtil.equal(MesWmMiscReceiptStatusEnum.CANCELED.getStatus(), receipt.getStatus())) {
            throw exception(WM_MISC_RECEIPT_CANCEL_NOT_ALLOWED);
        }

        // 取消
        miscReceiptMapper.updateById(new MesWmMiscReceiptDO()
                .setId(id).setStatus(MesWmMiscReceiptStatusEnum.CANCELED.getStatus()));
    }

    @Override
    public MesWmMiscReceiptDO validateMiscReceiptExists(Long id) {
        MesWmMiscReceiptDO receipt = miscReceiptMapper.selectById(id);
        if (receipt == null) {
            throw exception(WM_MISC_RECEIPT_NOT_EXISTS);
        }
        return receipt;
    }

    private void validateCodeUnique(Long id, String code) {
        MesWmMiscReceiptDO receipt = miscReceiptMapper.selectByCode(code);
        if (receipt == null) {
            return;
        }
        if (id == null || ObjUtil.notEqual(id, receipt.getId())) {
            throw exception(WM_MISC_RECEIPT_CODE_DUPLICATE);
        }
    }

    private MesWmMiscReceiptDO validateMiscReceiptExistsAndPrepare(Long id) {
        MesWmMiscReceiptDO receipt = validateMiscReceiptExists(id);
        if (ObjUtil.notEqual(MesWmMiscReceiptStatusEnum.PREPARE.getStatus(), receipt.getStatus())) {
            throw exception(WM_MISC_RECEIPT_STATUS_NOT_PREPARE);
        }
        return receipt;
    }

    /**
     * 校验入库单是否可编辑（存在且为草稿状态）
     *
     * @param id 入库单ID
     */
    public void validateMiscReceiptEditable(Long id) {
        validateMiscReceiptExistsAndPrepare(id);
    }

}
