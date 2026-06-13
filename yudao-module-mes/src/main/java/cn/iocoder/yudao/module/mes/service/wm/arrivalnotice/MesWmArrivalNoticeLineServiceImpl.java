package cn.iocoder.yudao.module.mes.service.wm.arrivalnotice;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line.MesWmArrivalNoticeLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line.MesWmArrivalNoticeLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.arrivalnotice.MesWmArrivalNoticeLineMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 到货通知单行 Service 实现类
 */
@Service
@Validated
public class MesWmArrivalNoticeLineServiceImpl implements MesWmArrivalNoticeLineService {

    @Resource
    private MesWmArrivalNoticeLineMapper arrivalNoticeLineMapper;

    @Resource
    @Lazy
    private MesWmArrivalNoticeService arrivalNoticeService;
    @Resource
    private MesMdItemService itemService;

    @Override
    public Long createArrivalNoticeLine(MesWmArrivalNoticeLineSaveReqVO createReqVO) {
        // 校验数据
        validateArrivalNoticeLineSaveData(createReqVO);

        // 插入
        MesWmArrivalNoticeLineDO line = BeanUtils.toBean(createReqVO, MesWmArrivalNoticeLineDO.class);
        initQualifiedQuantityIfNoIqc(line);
        arrivalNoticeLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateArrivalNoticeLine(MesWmArrivalNoticeLineSaveReqVO updateReqVO) {
        // 校验存在
        MesWmArrivalNoticeLineDO line = validateArrivalNoticeLineExists(updateReqVO.getId());
        // 校验数据
        updateReqVO.setNoticeId(line.getNoticeId());
        validateArrivalNoticeLineSaveData(updateReqVO);

        // 更新
        MesWmArrivalNoticeLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmArrivalNoticeLineDO.class);
        initQualifiedQuantityIfNoIqc(updateObj);
        arrivalNoticeLineMapper.updateById(updateObj);
    }

    private void validateArrivalNoticeLineSaveData(MesWmArrivalNoticeLineSaveReqVO reqVO) {
        // 校验父单据存在且为草稿状态
        arrivalNoticeService.validateArrivalNoticeExistsAndDraft(reqVO.getNoticeId());
        // 校验物料存在
        itemService.validateItemExistsAndEnable(reqVO.getItemId());
    }

    /**
     * 初始化合格品数量：
     * 1. 如果不需要 IQC 检验，则合格品数量直接 = 接收数量；
     * 2. 否则，合格品数量由对应的 IQC 检验单负责更新（见 {@link #updateArrivalNoticeLineWhenIqcFinish}）
     */
    private void initQualifiedQuantityIfNoIqc(MesWmArrivalNoticeLineDO line) {
        if (BooleanUtil.isFalse(line.getIqcCheckFlag())) {
            line.setQualifiedQuantity(line.getArrivalQuantity());
        }
    }

    @Override
    public void deleteArrivalNoticeLine(Long id) {
        // 校验存在
        MesWmArrivalNoticeLineDO line = validateArrivalNoticeLineExists(id);
        // 校验父单据存在且为草稿状态
        arrivalNoticeService.validateArrivalNoticeExistsAndDraft(line.getNoticeId());

        // 删除
        arrivalNoticeLineMapper.deleteById(id);
    }

    @Override
    public MesWmArrivalNoticeLineDO getArrivalNoticeLine(Long id) {
        return arrivalNoticeLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmArrivalNoticeLineDO> getArrivalNoticeLinePage(MesWmArrivalNoticeLinePageReqVO pageReqVO) {
        return arrivalNoticeLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmArrivalNoticeLineDO> getArrivalNoticeLineListByNoticeId(Long noticeId) {
        return arrivalNoticeLineMapper.selectListByNoticeId(noticeId);
    }

    @Override
    public void deleteArrivalNoticeLineByNoticeId(Long noticeId) {
        arrivalNoticeLineMapper.deleteByNoticeId(noticeId);
    }

    @Override
    public void updateArrivalNoticeLineWhenIqcFinish(Long lineId, Long iqcId, BigDecimal qualifiedQuantity) {
        // 校验行存在
        validateArrivalNoticeLineExists(lineId);
        // 更新
        arrivalNoticeLineMapper.updateById(new MesWmArrivalNoticeLineDO()
                .setId(lineId).setIqcId(iqcId).setQualifiedQuantity(qualifiedQuantity));
    }

    @Override
    public MesWmArrivalNoticeLineDO validateArrivalNoticeLineExists(Long id) {
        MesWmArrivalNoticeLineDO line = arrivalNoticeLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_ARRIVAL_NOTICE_LINE_NOT_EXISTS);
        }
        return line;
    }

    @Override
    public MesWmArrivalNoticeLineDO validateArrivalNoticeLineExists(Long lineId, Long noticeId) {
        MesWmArrivalNoticeLineDO line = validateArrivalNoticeLineExists(lineId);
        // 进一步校验行的 noticeId 与传入的 noticeId 是否匹配
        if (noticeId != null && ObjUtil.notEqual(line.getNoticeId(), noticeId)) {
            throw exception(WM_ARRIVAL_NOTICE_LINE_NOT_MATCH);
        }
        return line;
    }

}
