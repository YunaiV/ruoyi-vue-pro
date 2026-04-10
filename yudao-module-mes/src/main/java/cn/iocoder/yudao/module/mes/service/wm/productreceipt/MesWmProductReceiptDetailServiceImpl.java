package cn.iocoder.yudao.module.mes.service.wm.productreceipt;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.detail.MesWmProductReceiptDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productreceipt.MesWmProductReceiptDetailMapper;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 产品收货单明细 Service 实现类
 */
@Service
@Validated
public class MesWmProductReceiptDetailServiceImpl implements MesWmProductReceiptDetailService {

    @Resource
    private MesWmProductReceiptDetailMapper productReceiptDetailMapper;

    @Resource
    @Lazy
    private MesWmProductReceiptService productReceiptService;

    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;

    @Override
    public Long createProductReceiptDetail(MesWmProductReceiptDetailSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateProductReceiptDetailSaveData(createReqVO);

        // 2. 新增
        MesWmProductReceiptDetailDO detail = BeanUtils.toBean(createReqVO, MesWmProductReceiptDetailDO.class);
        productReceiptDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateProductReceiptDetail(MesWmProductReceiptDetailSaveReqVO updateReqVO) {
        // 1.1 校验存在
        MesWmProductReceiptDetailDO detail = validateProductReceiptDetailExists(updateReqVO.getId());
        // 1.2 校验关联数据
        updateReqVO.setReceiptId(detail.getReceiptId());
        validateProductReceiptDetailSaveData(updateReqVO);

        // 2. 更新
        MesWmProductReceiptDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmProductReceiptDetailDO.class);
        productReceiptDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductReceiptDetail(Long id) {
        // 校验存在
        MesWmProductReceiptDetailDO detail = validateProductReceiptDetailExists(id);
        // 校验父单据存在且为可编辑状态
        productReceiptService.validateProductReceiptEditable(detail.getReceiptId());

        // 删除
        productReceiptDetailMapper.deleteById(id);
    }

    @Override
    public MesWmProductReceiptDetailDO getProductReceiptDetail(Long id) {
        return productReceiptDetailMapper.selectById(id);
    }

    @Override
    public List<MesWmProductReceiptDetailDO> getProductReceiptDetailListByRecptId(Long receiptId) {
        return productReceiptDetailMapper.selectListByRecptId(receiptId);
    }

    @Override
    public List<MesWmProductReceiptDetailDO> getProductReceiptDetailListByLineId(Long lineId) {
        return productReceiptDetailMapper.selectListByLineId(lineId);
    }

    @Override
    public void deleteProductReceiptDetailByLineId(Long lineId) {
        productReceiptDetailMapper.deleteByLineId(lineId);
    }

    @Override
    public void deleteProductReceiptDetailByRecptId(Long receiptId) {
        productReceiptDetailMapper.deleteByRecptId(receiptId);
    }

    private MesWmProductReceiptDetailDO validateProductReceiptDetailExists(Long id) {
        MesWmProductReceiptDetailDO detail = productReceiptDetailMapper.selectById(id);
        if (detail == null) {
            throw exception(WM_PRODUCT_RECPT_DETAIL_NOT_EXISTS);
        }
        return detail;
    }

    /**
     * 校验明细保存时的关联数据
     */
    private void validateProductReceiptDetailSaveData(MesWmProductReceiptDetailSaveReqVO reqVO) {
        // 校验父单据存在且为可编辑状态
        productReceiptService.validateProductReceiptEditable(reqVO.getReceiptId());
        // 校验仓库层级关系
        warehouseAreaService.validateWarehouseAreaExists(
                reqVO.getWarehouseId(), reqVO.getLocationId(), reqVO.getAreaId());
    }

}
