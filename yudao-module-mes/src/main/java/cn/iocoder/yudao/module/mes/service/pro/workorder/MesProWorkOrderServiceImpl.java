package cn.iocoder.yudao.module.mes.service.pro.workorder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.MesProWorkOrderPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.MesProWorkOrderSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemBatchConfigDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.workorder.MesProWorkOrderMapper;
import cn.iocoder.yudao.module.mes.enums.pro.MesProWorkOrderStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemBatchConfigService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.pro.task.MesProTaskService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 生产工单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProWorkOrderServiceImpl implements MesProWorkOrderService {

    @Resource
    private MesProWorkOrderMapper workOrderMapper;

    @Resource
    private MesProWorkOrderBomService workOrderBomService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdItemBatchConfigService itemBatchConfigService;
    @Resource
    private MesWmBarcodeService barcodeService;
    @Resource
    private MesProTaskService taskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkOrder(MesProWorkOrderSaveReqVO createReqVO) {
        // 1. 校验数据
        validateWorkOrderSaveData(null, createReqVO);

        // 2.1 设置默认值
        if (createReqVO.getParentId() == null) {
            createReqVO.setParentId(MesProWorkOrderDO.PARENT_ID_NULL);
        }
        // 2.2 插入工单
        MesProWorkOrderDO workOrder = BeanUtils.toBean(createReqVO, MesProWorkOrderDO.class);
        workOrder.setStatus(MesProWorkOrderStatusEnum.PREPARE.getStatus());
        workOrderMapper.insert(workOrder);

        // 3. 自动生成 BOM：根据产品 BOM 生成工单 BOM
        workOrderBomService.generateWorkOrderBom(workOrder.getId(), createReqVO, false);

        // 4. 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.WORKORDER.getValue(),
                workOrder.getId(), workOrder.getCode(), workOrder.getName());
        return workOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkOrder(MesProWorkOrderSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 只有草稿状态才能编辑
        MesProWorkOrderDO oldWorkOrder = validateWorkOrderExists(updateReqVO.getId());
        if (ObjUtil.notEqual(oldWorkOrder.getStatus(), MesProWorkOrderStatusEnum.PREPARE.getStatus())) {
            throw exception(PRO_WORK_ORDER_NOT_PREPARE);
        }
        // 1.2 校验数据
        validateWorkOrderSaveData(updateReqVO.getId(), updateReqVO);

        // 2. 判断产品或数量是否变更，如果变更则重新生成 BOM（updated=true 会先清理旧数据）
        boolean productChanged = ObjUtil.notEqual(oldWorkOrder.getProductId(), updateReqVO.getProductId());
        boolean quantityChanged = oldWorkOrder.getQuantity().compareTo(updateReqVO.getQuantity()) != 0;
        if (productChanged || quantityChanged) {
            workOrderBomService.generateWorkOrderBom(updateReqVO.getId(), updateReqVO, true);
        }

        // 3. 更新
        MesProWorkOrderDO updateObj = BeanUtils.toBean(updateReqVO, MesProWorkOrderDO.class);
        workOrderMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkOrder(Long id) {
        // 1.1 校验存在
        MesProWorkOrderDO workOrder = validateWorkOrderExists(id);
        // 1.2 只能删除草稿状态的工单
        if (ObjUtil.notEqual(workOrder.getStatus(), MesProWorkOrderStatusEnum.PREPARE.getStatus())) {
            throw exception(PRO_WORK_ORDER_NOT_PREPARE);
        }
        // 1.3 校验是否有子工单
        Long childCount = workOrderMapper.selectCount(MesProWorkOrderDO::getParentId, id);
        if (childCount > 0) {
            throw exception(PRO_WORK_ORDER_HAS_CHILDREN);
        }

        // 2. 删除工单 + BOM
        workOrderMapper.deleteById(id);
        workOrderBomService.deleteWorkOrderBomByWorkOrderId(id);
    }

    @Override
    public MesProWorkOrderDO validateWorkOrderExists(Long id) {
        MesProWorkOrderDO workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw exception(PRO_WORK_ORDER_NOT_EXISTS);
        }
        return workOrder;
    }

    @Override
    public MesProWorkOrderDO getWorkOrder(Long id) {
        return workOrderMapper.selectById(id);
    }

    @Override
    public MesProWorkOrderDO getWorkOrder(String code) {
        return workOrderMapper.selectByCode(code);
    }

    @Override
    public PageResult<MesProWorkOrderDO> getWorkOrderPage(MesProWorkOrderPageReqVO pageReqVO) {
        return workOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesProWorkOrderDO> getWorkOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return workOrderMapper.selectByIds(ids);
    }

    @Override
    public MesProWorkOrderDO validateWorkOrderConfirmed(Long id) {
        MesProWorkOrderDO workOrder = validateWorkOrderExists(id);
        if (ObjUtil.notEqual(workOrder.getStatus(), MesProWorkOrderStatusEnum.CONFIRMED.getStatus())) {
            throw exception(PRO_WORK_ORDER_NOT_CONFIRMED);
        }
        return workOrder;
    }

    @Override
    public void confirmWorkOrder(Long id) {
        // 1.1 校验存在
        MesProWorkOrderDO workOrder = validateWorkOrderExists(id);
        // 1.2 只有草稿状态才能确认
        if (ObjUtil.notEqual(workOrder.getStatus(), MesProWorkOrderStatusEnum.PREPARE.getStatus())) {
            throw exception(PRO_WORK_ORDER_NOT_PREPARE);
        }

        // 2. 更新状态为已确认
        workOrderMapper.updateById(new MesProWorkOrderDO().setId(id)
                .setStatus(MesProWorkOrderStatusEnum.CONFIRMED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishWorkOrder(Long id) {
        // 1. 校验存在 + 只有已确认状态才能完成
        MesProWorkOrderDO workOrder = validateWorkOrderExists(id);
        if (ObjUtil.notEqual(workOrder.getStatus(), MesProWorkOrderStatusEnum.CONFIRMED.getStatus())) {
            throw exception(PRO_WORK_ORDER_NOT_CONFIRMED);
        }

        // 2. 级联完成所有关联任务
        taskService.finishTaskByOrderId(id);

        // 3. 更新工单状态为已完成
        workOrderMapper.updateById(new MesProWorkOrderDO().setId(id)
                .setStatus(MesProWorkOrderStatusEnum.FINISHED.getStatus())
                .setFinishDate(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWorkOrder(Long id) {
        // 1. 校验存在 + 只有已确认状态才能取消
        MesProWorkOrderDO workOrder = validateWorkOrderExists(id);
        if (ObjUtil.notEqual(workOrder.getStatus(), MesProWorkOrderStatusEnum.CONFIRMED.getStatus())) {
            throw exception(PRO_WORK_ORDER_NOT_CONFIRMED);
        }

        // 2. 级联取消所有关联任务
        taskService.cancelTaskByOrderId(id);

        // 3. 更新工单状态为已取消
        workOrderMapper.updateById(new MesProWorkOrderDO().setId(id)
                .setStatus(MesProWorkOrderStatusEnum.CANCELED.getStatus())
                .setCancelDate(LocalDateTime.now()));
    }

    // ==================== 校验方法 ====================

    private void validateWorkOrderSaveData(Long id, MesProWorkOrderSaveReqVO reqVO) {
        // 1. 校验编码唯一
        validateWorkOrderCodeUnique(id, reqVO.getCode());
        // 2. 校验产品存在
        itemService.validateItemExists(reqVO.getProductId());
        // 3. 校验批次配置（如果产品有 clientFlag=true，则 clientId 必填）
        MesMdItemBatchConfigDO batchConfig = itemBatchConfigService.getItemBatchConfigByItemId(reqVO.getProductId());
        if (batchConfig != null && Boolean.TRUE.equals(batchConfig.getClientFlag()) && reqVO.getClientId() == null) {
            throw exception(MD_CLIENT_NOT_EXISTS);
        }
    }

    private void validateWorkOrderCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        MesProWorkOrderDO workOrder = workOrderMapper.selectByCode(code);
        if (workOrder == null) {
            return;
        }
        if (ObjUtil.notEqual(workOrder.getId(), id)) {
            throw exception(PRO_WORK_ORDER_CODE_DUPLICATE);
        }
    }

    @Override
    public void updateProducedQuantity(Long id, BigDecimal incrQuantityProduced) {
        // 校验工单存在
        validateWorkOrderExists(id);
        // 更新数量
        workOrderMapper.updateProducedQuantity(id, incrQuantityProduced);
    }

}
