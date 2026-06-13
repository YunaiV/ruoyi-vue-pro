package cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.detail.MesWmOutsourceReceiptDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.outsourcereceipt.MesWmOutsourceReceiptDetailMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_OUTSOURCE_RECEIPT_DETAIL_NOT_EXISTS;

/**
 * MES 委外收货明细 Service 实现类
 */
@Service
@Validated
public class MesWmOutsourceReceiptDetailServiceImpl implements MesWmOutsourceReceiptDetailService {

    @Resource
    private MesWmOutsourceReceiptDetailMapper detailMapper;

    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmWarehouseAreaService areaService;

    @Override
    public Long createOutsourceReceiptDetail(MesWmOutsourceReceiptDetailSaveReqVO createReqVO) {
        // 校验数据
        validateDetailSaveData(createReqVO);

        // 插入
        MesWmOutsourceReceiptDetailDO detail = BeanUtils.toBean(createReqVO, MesWmOutsourceReceiptDetailDO.class);
        detailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateOutsourceReceiptDetail(MesWmOutsourceReceiptDetailSaveReqVO updateReqVO) {
        // 校验存在
        validateOutsourceReceiptDetailExists(updateReqVO.getId());
        // 校验数据
        validateDetailSaveData(updateReqVO);

        // 更新
        MesWmOutsourceReceiptDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmOutsourceReceiptDetailDO.class);
        detailMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutsourceReceiptDetail(Long id) {
        // 校验存在
        validateOutsourceReceiptDetailExists(id);
        // 删除
        detailMapper.deleteById(id);
    }

    @Override
    public MesWmOutsourceReceiptDetailDO getOutsourceReceiptDetail(Long id) {
        return detailMapper.selectById(id);
    }

    @Override
    public List<MesWmOutsourceReceiptDetailDO> getOutsourceReceiptDetailListByReceiptId(Long receiptId) {
        return detailMapper.selectListByReceiptId(receiptId);
    }

    @Override
    public List<MesWmOutsourceReceiptDetailDO> getOutsourceReceiptDetailListByLineId(Long lineId) {
        return detailMapper.selectListByLineId(lineId);
    }

    @Override
    public void deleteOutsourceReceiptDetailByReceiptId(Long receiptId) {
        detailMapper.deleteByReceiptId(receiptId);
    }

    @Override
    public void deleteOutsourceReceiptDetailByLineId(Long lineId) {
        detailMapper.deleteByLineId(lineId);
    }

    /**
     * 校验保存时的关联数据
     */
    private void validateDetailSaveData(MesWmOutsourceReceiptDetailSaveReqVO reqVO) {
        // 校验物料存在
        itemService.validateItemExistsAndEnable(reqVO.getItemId());
        // 校验仓库、库区、库位的层级关系
        areaService.validateWarehouseAreaExists(reqVO.getWarehouseId(), reqVO.getLocationId(), reqVO.getAreaId());
    }

    private void validateOutsourceReceiptDetailExists(Long id) {
        if (detailMapper.selectById(id) == null) {
            throw exception(WM_OUTSOURCE_RECEIPT_DETAIL_NOT_EXISTS);
        }
    }

}
