package cn.iocoder.yudao.module.mes.service.wm.productreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.line.MesWmProductReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.line.MesWmProductReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productreceipt.MesWmProductReceiptLineMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_PRODUCT_RECPT_LINE_NOT_EXISTS;

/**
 * MES 产品收货单行 Service 实现类
 */
@Service
@Validated
public class MesWmProductReceiptLineServiceImpl implements MesWmProductReceiptLineService {

    @Resource
    private MesWmProductReceiptLineMapper productReceiptLineMapper;

    @Resource
    @Lazy
    private MesWmProductReceiptService productReceiptService;
    @Resource
    private MesWmProductReceiptDetailService productReceiptDetailService;
    @Resource
    private MesMdItemService itemService;

    @Override
    public Long createProductReceiptLine(MesWmProductReceiptLineSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateProductReceiptLineSaveData(createReqVO);

        // 2. 新增
        MesWmProductReceiptLineDO line = BeanUtils.toBean(createReqVO, MesWmProductReceiptLineDO.class);
        productReceiptLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateProductReceiptLine(MesWmProductReceiptLineSaveReqVO updateReqVO) {
        // 1.1 校验存在
        MesWmProductReceiptLineDO line = validateProductReceiptLineExists(updateReqVO.getId());
        // 1.2 校验关联数据
        updateReqVO.setReceiptId(line.getReceiptId());
        validateProductReceiptLineSaveData(updateReqVO);

        // 2. 更新
        MesWmProductReceiptLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmProductReceiptLineDO.class);
        productReceiptLineMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductReceiptLine(Long id) {
        // 校验存在
        MesWmProductReceiptLineDO line = validateProductReceiptLineExists(id);
        // 校验父单据存在且为可编辑状态
        productReceiptService.validateProductReceiptEditable(line.getReceiptId());

        // 级联删除明细
        productReceiptDetailService.deleteProductReceiptDetailByLineId(id);
        // 删除
        productReceiptLineMapper.deleteById(id);
    }

    @Override
    public MesWmProductReceiptLineDO getProductReceiptLine(Long id) {
        return productReceiptLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmProductReceiptLineDO> getProductReceiptLinePage(MesWmProductReceiptLinePageReqVO pageReqVO) {
        return productReceiptLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmProductReceiptLineDO> getProductReceiptLineListByRecptId(Long receiptId) {
        return productReceiptLineMapper.selectListByRecptId(receiptId);
    }

    @Override
    public void deleteProductReceiptLineByRecptId(Long receiptId) {
        productReceiptLineMapper.deleteByRecptId(receiptId);
    }

    private MesWmProductReceiptLineDO validateProductReceiptLineExists(Long id) {
        MesWmProductReceiptLineDO line = productReceiptLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_PRODUCT_RECPT_LINE_NOT_EXISTS);
        }
        return line;
    }

    /**
     * 校验行保存时的关联数据
     */
    private void validateProductReceiptLineSaveData(MesWmProductReceiptLineSaveReqVO reqVO) {
        // 校验父单据存在且为可编辑状态
        productReceiptService.validateProductReceiptEditable(reqVO.getReceiptId());
        // 校验物料存在
        itemService.validateItemExistsAndEnable(reqVO.getItemId());
    }

}
