package cn.iocoder.yudao.module.mes.service.wm.arrivalnotice;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.MesWmArrivalNoticePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.MesWmArrivalNoticeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.arrivalnotice.MesWmArrivalNoticeMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmArrivalNoticeStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 到货通知单 Service 实现类
 */
@Service
@Validated
@Slf4j
public class MesWmArrivalNoticeServiceImpl implements MesWmArrivalNoticeService {

    @Resource
    private MesWmArrivalNoticeMapper arrivalNoticeMapper;

    @Resource
    private MesWmArrivalNoticeLineService arrivalNoticeLineService;
    @Resource
    private cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService vendorService;

    @Override
    public Long createArrivalNotice(MesWmArrivalNoticeSaveReqVO createReqVO) {
        // 校验数据
        validateArrivalNoticeSaveData(createReqVO);

        // 插入
        MesWmArrivalNoticeDO notice = BeanUtils.toBean(createReqVO, MesWmArrivalNoticeDO.class);
        notice.setStatus(MesWmArrivalNoticeStatusEnum.PREPARE.getStatus());
        arrivalNoticeMapper.insert(notice);
        return notice.getId();
    }

    @Override
    public void updateArrivalNotice(MesWmArrivalNoticeSaveReqVO updateReqVO) {
        // 校验存在 + 草稿状态
        validateArrivalNoticeExistsAndDraft(updateReqVO.getId());
        // 校验数据
        validateArrivalNoticeSaveData(updateReqVO);

        // 更新
        MesWmArrivalNoticeDO updateObj = BeanUtils.toBean(updateReqVO, MesWmArrivalNoticeDO.class);
        arrivalNoticeMapper.updateById(updateObj);
    }

    private void validateArrivalNoticeSaveData(MesWmArrivalNoticeSaveReqVO reqVO) {
        // 校验编码唯一
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验供应商存在且启用
        vendorService.validateVendorExistsAndEnable(reqVO.getVendorId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArrivalNotice(Long id) {
        // 校验存在 + 草稿状态
        validateArrivalNoticeExistsAndDraft(id);

        // 级联删除行
        arrivalNoticeLineService.deleteArrivalNoticeLineByNoticeId(id);
        // 删除
        arrivalNoticeMapper.deleteById(id);
    }

    @Override
    public MesWmArrivalNoticeDO getArrivalNotice(Long id) {
        return arrivalNoticeMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmArrivalNoticeDO> getArrivalNoticePage(MesWmArrivalNoticePageReqVO pageReqVO) {
        return arrivalNoticeMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitArrivalNotice(Long id) {
        // 1.1 校验存在 + 草稿状态
        validateArrivalNoticeExistsAndDraft(id);
        // 1.2 检查是否有行项目
        List<MesWmArrivalNoticeLineDO> lines = arrivalNoticeLineService.getArrivalNoticeLineListByNoticeId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_ARRIVAL_NOTICE_NO_LINE);
        }

        // 2. 检查所有行的 iqcCheckFlag：如果有需要检验的行，进入待质检状态
        boolean needCheck = CollectionUtils.anyMatch(lines,
                line -> Boolean.TRUE.equals(line.getIqcCheckFlag()));
        if (needCheck) {
            // 需要检验，进入待质检
            arrivalNoticeMapper.updateById(new MesWmArrivalNoticeDO()
                    .setId(id).setStatus(MesWmArrivalNoticeStatusEnum.PENDING_QC.getStatus()));
            return;
        }
        // 不需要检验，直接进入待入库
        arrivalNoticeMapper.updateById(new MesWmArrivalNoticeDO()
                .setId(id).setStatus(MesWmArrivalNoticeStatusEnum.PENDING_RECEIPT.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArrivalNoticeWhenIqcFinish(Long id, Long lineId, Long iqcId, BigDecimal qualifiedQuantity) {
        // 1.1 校验到货通知单存在
        MesWmArrivalNoticeDO notice = validateArrivalNoticeExists(id);
        // 1.2 校验状态为待质检
        if (ObjUtil.notEqual(MesWmArrivalNoticeStatusEnum.PENDING_QC.getStatus(), notice.getStatus())) {
            throw exception(WM_ARRIVAL_NOTICE_STATUS_NOT_PENDING_QC);
        }

        // 2. 更新到货通知单行：绑定 iqcId + 合格数量
        arrivalNoticeLineService.updateArrivalNoticeLineWhenIqcFinish(lineId, iqcId, qualifiedQuantity);

        // 3.1 判断是否所有需检行都已完成
        List<MesWmArrivalNoticeLineDO> lines = arrivalNoticeLineService.getArrivalNoticeLineListByNoticeId(id);
        boolean hasUnchecked = CollectionUtils.anyMatch(lines,
                line -> Boolean.TRUE.equals(line.getIqcCheckFlag()) && line.getIqcId() == null);
        if (hasUnchecked) {
            log.info("[updateArrivalNoticeWhenIqcFinish][到货通知单({}) 还有未完成检验的行，暂不更新状态]", id);
            return;
        }
        // 3.2 所有行检验完成，更新主表状态 PENDING_QC → PENDING_RECEIPT
        arrivalNoticeMapper.updateById(new MesWmArrivalNoticeDO()
                .setId(id).setStatus(MesWmArrivalNoticeStatusEnum.PENDING_RECEIPT.getStatus()));
    }

    @Override
    public void finishArrivalNotice(Long id) {
        // 校验存在
        MesWmArrivalNoticeDO notice = validateArrivalNoticeExists(id);
        // 校验状态：只有待入库才允许完成
        if (ObjUtil.notEqual(MesWmArrivalNoticeStatusEnum.PENDING_RECEIPT.getStatus(), notice.getStatus())) {
            throw exception(WM_ARRIVAL_NOTICE_STATUS_NOT_PENDING_RECEIPT);
        }
        // 行级防御校验：确保所有需检行都已完成 IQC
        List<MesWmArrivalNoticeLineDO> lines = arrivalNoticeLineService.getArrivalNoticeLineListByNoticeId(id);
        boolean hasUnchecked = CollectionUtils.anyMatch(lines,
                line -> Boolean.TRUE.equals(line.getIqcCheckFlag()) && line.getIqcId() == null);
        if (hasUnchecked) {
            throw exception(WM_ARRIVAL_NOTICE_IQC_PENDING);
        }

        // 完成
        arrivalNoticeMapper.updateById(new MesWmArrivalNoticeDO()
                .setId(id).setStatus(MesWmArrivalNoticeStatusEnum.FINISHED.getStatus()));
    }

    @Override
    public List<MesWmArrivalNoticeDO> getArrivalNoticeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return arrivalNoticeMapper.selectByIds(ids);
    }

    @Override
    public void validateArrivalNoticeAndLineExists(Long noticeId, Long lineId) {
        // 1. 校验通知单存在
        MesWmArrivalNoticeDO notice = arrivalNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw exception(WM_ARRIVAL_NOTICE_NOT_EXISTS);
        }
        // 2.1 校验行存在
        MesWmArrivalNoticeLineDO line = arrivalNoticeLineService.getArrivalNoticeLine(lineId);
        if (line == null) {
            throw exception(WM_ARRIVAL_NOTICE_LINE_NOT_EXISTS);
        }
        // 2.2 校验行属于该通知单
        if (ObjUtil.notEqual(line.getNoticeId(), noticeId)) {
            throw exception(WM_ARRIVAL_NOTICE_LINE_NOT_EXISTS);
        }
    }

    @Override
    public MesWmArrivalNoticeDO validateArrivalNoticeExists(Long id) {
        MesWmArrivalNoticeDO notice = arrivalNoticeMapper.selectById(id);
        if (notice == null) {
            throw exception(WM_ARRIVAL_NOTICE_NOT_EXISTS);
        }
        return notice;
    }

    @Override
    public MesWmArrivalNoticeDO validateArrivalNoticeExistsAndDraft(Long id) {
        MesWmArrivalNoticeDO notice = validateArrivalNoticeExists(id);
        if (ObjUtil.notEqual(MesWmArrivalNoticeStatusEnum.PREPARE.getStatus(), notice.getStatus())) {
            throw exception(WM_ARRIVAL_NOTICE_STATUS_NOT_PREPARE);
        }
        return notice;
    }

    @Override
    public MesWmArrivalNoticeDO validateArrivalNoticeReadyForReceipt(Long id) {
        // 1. 校验到货通知单存在且状态为待入库
        MesWmArrivalNoticeDO notice = validateArrivalNoticeExists(id);
        if (ObjUtil.notEqual(MesWmArrivalNoticeStatusEnum.PENDING_RECEIPT.getStatus(), notice.getStatus())) {
            throw exception(WM_ARRIVAL_NOTICE_STATUS_NOT_PENDING_RECEIPT);
        }
        // 2. 行级防御校验：确保所有需检行都已完成 IQC
        List<MesWmArrivalNoticeLineDO> lines = arrivalNoticeLineService.getArrivalNoticeLineListByNoticeId(id);
        boolean hasUnchecked = CollectionUtils.anyMatch(lines,
                line -> Boolean.TRUE.equals(line.getIqcCheckFlag()) && line.getIqcId() == null);
        if (hasUnchecked) {
            throw exception(WM_ARRIVAL_NOTICE_IQC_PENDING);
        }
        return notice;
    }

    private void validateCodeUnique(Long id, String code) {
        MesWmArrivalNoticeDO notice = arrivalNoticeMapper.selectByCode(code);
        if (notice == null) {
            return;
        }
        if (ObjUtil.notEqual(id, notice.getId())) {
            throw exception(WM_ARRIVAL_NOTICE_CODE_DUPLICATE);
        }
    }

    @Override
    public Long getArrivalNoticeCountByVendorId(Long vendorId) {
        return arrivalNoticeMapper.selectCountByVendorId(vendorId);
    }

}
