package cn.iocoder.yudao.module.mes.service.wm.productreceipt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.MesWmProductReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.MesWmProductReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productreceipt.MesWmProductReceiptMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmProductReceiptStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 产品收货单 Service 实现类
 */
@Service
@Validated
public class MesWmProductReceiptServiceImpl implements MesWmProductReceiptService {

    @Resource
    private MesWmProductReceiptMapper productReceiptMapper;

    @Resource
    private MesWmProductReceiptLineService productReceiptLineService;

    @Resource
    private MesWmProductReceiptDetailService productReceiptDetailService;

    @Resource
    private MesWmTransactionService wmTransactionService;

    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesWmWarehouseService warehouseService;
    @Resource
    private MesWmWarehouseLocationService locationService;
    @Resource
    private MesWmWarehouseAreaService areaService;

    @Override
    public Long createProductReceipt(MesWmProductReceiptSaveReqVO createReqVO) {
        // 1. 校验关联数据
        MesProWorkOrderDO workOrder = validateProductReceiptSaveData(createReqVO);

        // 2. 插入
        MesWmProductReceiptDO receipt = BeanUtils.toBean(createReqVO, MesWmProductReceiptDO.class);
        if (workOrder != null) {
            receipt.setItemId(workOrder.getProductId());
        }
        receipt.setStatus(MesWmProductReceiptStatusEnum.PREPARE.getStatus());
        productReceiptMapper.insert(receipt);
        return receipt.getId();
    }

    @Override
    public void updateProductReceipt(MesWmProductReceiptSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 草稿状态
        validateProductReceiptExistsAndDraft(updateReqVO.getId());
        // 1.2 校验关联数据
        MesProWorkOrderDO workOrder = validateProductReceiptSaveData(updateReqVO);

        // 2. 更新
        MesWmProductReceiptDO updateObj = BeanUtils.toBean(updateReqVO, MesWmProductReceiptDO.class);
        if (workOrder != null) {
            updateObj.setItemId(workOrder.getProductId());
        }
        productReceiptMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductReceipt(Long id) {
        // 校验存在 + 草稿状态
        validateProductReceiptExistsAndDraft(id);

        // 级联删除明细和行
        productReceiptDetailService.deleteProductReceiptDetailByRecptId(id);
        productReceiptLineService.deleteProductReceiptLineByRecptId(id);
        // 删除
        productReceiptMapper.deleteById(id);
    }

    @Override
    public MesWmProductReceiptDO getProductReceipt(Long id) {
        return productReceiptMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmProductReceiptDO> getProductReceiptPage(MesWmProductReceiptPageReqVO pageReqVO) {
        return productReceiptMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitProductReceipt(Long id) {
        // 校验存在 + 草稿状态
        validateProductReceiptExistsAndDraft(id);
        // 校验至少有一条行
        List<MesWmProductReceiptLineDO> lines = productReceiptLineService.getProductReceiptLineListByRecptId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_PRODUCT_RECPT_NO_LINE);
        }

        // 提交（草稿 → 待上架）
        productReceiptMapper.updateById(new MesWmProductReceiptDO()
                .setId(id).setStatus(MesWmProductReceiptStatusEnum.APPROVING.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockProductReceipt(Long id) {
        // 校验存在
        MesWmProductReceiptDO receipt = validateProductReceiptExists(id);
        if (ObjUtil.notEqual(MesWmProductReceiptStatusEnum.APPROVING.getStatus(), receipt.getStatus())) {
            throw exception(WM_PRODUCT_RECPT_STATUS_ERROR);
        }

        // 执行上架（待上架 → 待入库）
        productReceiptMapper.updateById(new MesWmProductReceiptDO()
                .setId(id).setStatus(MesWmProductReceiptStatusEnum.APPROVED.getStatus()));
    }

    @Override
    public Boolean checkProductReceiptQuantity(Long id) {
        List<MesWmProductReceiptLineDO> lines = productReceiptLineService.getProductReceiptLineListByRecptId(id);
        for (MesWmProductReceiptLineDO line : lines) {
            List<MesWmProductReceiptDetailDO> details = productReceiptDetailService.getProductReceiptDetailListByLineId(line.getId());
            BigDecimal totalDetailQty = CollectionUtils.getSumValue(details,
                    MesWmProductReceiptDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
            if (line.getQuantity() != null && totalDetailQty.compareTo(line.getQuantity()) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishProductReceipt(Long id) {
        // 校验存在
        MesWmProductReceiptDO receipt = validateProductReceiptExists(id);
        if (ObjUtil.notEqual(MesWmProductReceiptStatusEnum.APPROVED.getStatus(), receipt.getStatus())) {
            throw exception(WM_PRODUCT_RECPT_STATUS_ERROR);
        }
        // 校验明细非空
        List<MesWmProductReceiptDetailDO> details = productReceiptDetailService.getProductReceiptDetailListByRecptId(id);
        if (CollUtil.isEmpty(details)) {
            throw exception(WM_PRODUCT_RECPT_NO_DETAIL);
        }

        // 创建库存事务
        createTransactionList(receipt, details);

        // 更新收货单状态
        productReceiptMapper.updateById(new MesWmProductReceiptDO()
                .setId(id).setStatus(MesWmProductReceiptStatusEnum.FINISHED.getStatus()));
    }

    private void createTransactionList(MesWmProductReceiptDO receipt, List<MesWmProductReceiptDetailDO> details) {
        // 1. 查询虚拟线边库
        MesWmWarehouseDO virtualWarehouse = warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);
        MesWmWarehouseLocationDO virtualLocation = locationService.getWarehouseLocationByCode(MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION);
        MesWmWarehouseAreaDO virtualArea = areaService.getWarehouseAreaByCode(MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA);

        // 2. 遍历明细，每条明细产生 OUT（虚拟线边库扣减）+ IN（实际仓库增加）
        for (MesWmProductReceiptDetailDO detail : details) {
            // 2.1 先从虚拟线边库出库（库存减少）
            Long outTransactionId = wmTransactionService.createTransaction(new MesWmTransactionSaveReqDTO()
                    .setType(MesWmTransactionTypeEnum.OUT.getType()).setItemId(detail.getItemId())
                    .setQuantity(detail.getQuantity().negate()) // 库存减少
                    .setBatchId(detail.getBatchId())
                    .setWarehouseId(virtualWarehouse.getId()).setLocationId(virtualLocation.getId()).setAreaId(virtualArea.getId())
                    .setCheckFlag(false) // 线边库允许负库存
                    .setBizType(MesBizTypeConstants.WM_PRODUCT_RECPT).setBizId(receipt.getId())
                    .setBizCode(receipt.getCode()).setBizLineId(detail.getLineId()));
            // 2.2 再入实际仓库（库存增加）
            wmTransactionService.createTransaction(new MesWmTransactionSaveReqDTO()
                    .setType(MesWmTransactionTypeEnum.IN.getType()).setItemId(detail.getItemId())
                    .setQuantity(detail.getQuantity()) // 库存增加
                    .setBatchId(detail.getBatchId())
                    .setWarehouseId(detail.getWarehouseId()).setLocationId(detail.getLocationId()).setAreaId(detail.getAreaId())
                    .setBizType(MesBizTypeConstants.WM_PRODUCT_RECPT).setBizId(receipt.getId())
                    .setBizCode(receipt.getCode()).setBizLineId(detail.getLineId())
                    .setRelatedTransactionId(outTransactionId)); // 关联出库事务
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProductReceipt(Long id) {
        // 校验存在
        MesWmProductReceiptDO receipt = validateProductReceiptExists(id);
        // 已完成和已取消不允许取消
        if (ObjectUtils.equalsAny(receipt.getStatus(),
                MesWmProductReceiptStatusEnum.FINISHED.getStatus(),
                MesWmProductReceiptStatusEnum.CANCELED.getStatus())) {
            throw exception(WM_PRODUCT_RECPT_CANCEL_NOT_ALLOWED);
        }

        // 取消
        productReceiptMapper.updateById(new MesWmProductReceiptDO()
                .setId(id).setStatus(MesWmProductReceiptStatusEnum.CANCELED.getStatus()));
    }

    @Override
    public MesWmProductReceiptDO validateProductReceiptEditable(Long id) {
        MesWmProductReceiptDO receipt = validateProductReceiptExists(id);
        if (ObjUtil.notEqual(receipt.getStatus(), MesWmProductReceiptStatusEnum.PREPARE.getStatus())
                && ObjUtil.notEqual(receipt.getStatus(), MesWmProductReceiptStatusEnum.APPROVING.getStatus())) {
            throw exception(WM_PRODUCT_RECPT_STATUS_NOT_PREPARE);
        }
        return receipt;
    }

    private MesWmProductReceiptDO validateProductReceiptExists(Long id) {
        MesWmProductReceiptDO receipt = productReceiptMapper.selectById(id);
        if (receipt == null) {
            throw exception(WM_PRODUCT_RECPT_NOT_EXISTS);
        }
        return receipt;
    }

    /**
     * 校验产品收货单存在且为草稿状态
     */
    private MesWmProductReceiptDO validateProductReceiptExistsAndDraft(Long id) {
        MesWmProductReceiptDO receipt = validateProductReceiptExists(id);
        if (ObjUtil.notEqual(MesWmProductReceiptStatusEnum.PREPARE.getStatus(), receipt.getStatus())) {
            throw exception(WM_PRODUCT_RECPT_STATUS_NOT_PREPARE);
        }
        return receipt;
    }
    /**
     * 校验保存时的关联数据
     *
     * @return 工单对象（可能为 null）
     */
    private MesProWorkOrderDO validateProductReceiptSaveData(MesWmProductReceiptSaveReqVO reqVO) {
        // 校验编码唯一
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验工单存在
        return reqVO.getWorkOrderId() != null ?
                workOrderService.validateWorkOrderExists(reqVO.getWorkOrderId()) : null;
    }

    private void validateCodeUnique(Long id, String code) {
        MesWmProductReceiptDO receipt = productReceiptMapper.selectByCode(code);
        if (receipt == null) {
            return;
        }
        if (ObjUtil.notEqual(id, receipt.getId())) {
            throw exception(WM_PRODUCT_RECPT_CODE_DUPLICATE);
        }
    }

}
