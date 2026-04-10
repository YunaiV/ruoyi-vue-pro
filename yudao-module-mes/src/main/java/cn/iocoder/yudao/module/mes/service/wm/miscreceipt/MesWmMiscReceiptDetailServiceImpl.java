package cn.iocoder.yudao.module.mes.service.wm.miscreceipt;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.miscreceipt.MesWmMiscReceiptDetailMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_MISC_RECEIPT_DETAIL_NOT_EXISTS;

/**
 * MES 杂项入库明细 Service 实现类
 */
@Service
@Validated
public class MesWmMiscReceiptDetailServiceImpl implements MesWmMiscReceiptDetailService {

    @Resource
    private MesWmMiscReceiptDetailMapper receiptDetailMapper;

    @Override
    public Long createMiscReceiptDetail(MesWmMiscReceiptLineSaveReqVO createReqVO) {
        MesWmMiscReceiptDetailDO detail = BeanUtils.toBean(createReqVO, MesWmMiscReceiptDetailDO.class);
        detail.setId(null); // 清空 id，让数据库自动生成
        detail.setLineId(createReqVO.getId()); // VO 的 id 就是 lineId
        receiptDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateMiscReceiptDetail(MesWmMiscReceiptLineSaveReqVO updateReqVO) {
        // 基于 lineId 查询唯一的 detail 记录（VO 的 id 字段是 lineId）
        List<MesWmMiscReceiptDetailDO> details = receiptDetailMapper.selectListByLineId(updateReqVO.getId());
        if (CollUtil.isEmpty(details)) {
            return;
        }

        // 更新 detail（应该只有一条记录）
        MesWmMiscReceiptDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmMiscReceiptDetailDO.class);
        updateObj.setId(details.get(0).getId()); // 使用查询到的 detail id
        updateObj.setLineId(updateReqVO.getId()); // VO 的 id 就是 lineId
        receiptDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteMiscReceiptDetailByReceiptId(Long receiptId) {
        receiptDetailMapper.deleteByReceiptId(receiptId);
    }

    @Override
    public void deleteMiscReceiptDetailByLineId(Long lineId) {
        receiptDetailMapper.deleteByLineId(lineId);
    }

    @Override
    public void validateMiscReceiptDetailExists(Long id) {
        if (receiptDetailMapper.selectById(id) == null) {
            throw exception(WM_MISC_RECEIPT_DETAIL_NOT_EXISTS);
        }
    }

}
