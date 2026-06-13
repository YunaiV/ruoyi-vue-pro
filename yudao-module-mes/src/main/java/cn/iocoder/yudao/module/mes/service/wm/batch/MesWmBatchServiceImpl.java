package cn.iocoder.yudao.module.mes.service.wm.batch;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo.MesWmBatchGenerateReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo.MesWmBatchPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemBatchConfigDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.batch.MesWmBatchMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodeRuleCodeEnum;
import cn.iocoder.yudao.module.mes.service.md.autocode.MesMdAutoCodeRecordService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemBatchConfigService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * 批次管理 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class MesWmBatchServiceImpl implements MesWmBatchService {

    /**
     * 批次追溯最大递归深度，防止极端场景下性能问题
     */
    private static final int MAX_TRACE_DEPTH = 20;

    @Resource
    private MesWmBatchMapper batchMapper;

    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdItemBatchConfigService itemBatchConfigService;
    @Resource
    private MesMdAutoCodeRecordService autoCodeRecordService;
    @Resource
    private MesWmBarcodeService barcodeService;

    @Override
    public PageResult<MesWmBatchDO> getBatchPage(MesWmBatchPageReqVO pageReqVO) {
        return batchMapper.selectPage(pageReqVO);
    }

    @Override
    public MesWmBatchDO getBatch(Long id) {
        return batchMapper.selectById(id);
    }

    @Override
    public MesWmBatchDO getBatchByCode(String code) {
        return batchMapper.selectByCode(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MesWmBatchDO getOrGenerateBatchCode(MesWmBatchGenerateReqVO reqVO) {
        // 1.1 查询物料信息，检查是否启用批次管理
        MesMdItemDO item = itemService.validateItemExists(reqVO.getItemId());
        if (Boolean.FALSE.equals(item.getBatchFlag())) {
            // 未启用批次管理，返回 null
            return null;
        }
        // 1.2 查询物料批次配置
        MesMdItemBatchConfigDO config = itemBatchConfigService.validateItemBatchConfigExists(reqVO.getItemId());

        // 2. 根据配置校验必填参数并清空不需要的参数
        MesWmBatchDO batch = MesWmBatchDO.builder().itemId(reqVO.getItemId()).build();
        // 生产日期
        if (Boolean.TRUE.equals(config.getProduceDateFlag())) {
            if (ObjUtil.isNull(reqVO.getProduceDate())) {
                throw exception(WM_BATCH_PRODUCE_DATE_REQUIRED);
            }
            batch.setProduceDate(reqVO.getProduceDate());
        }
        // 入库日期
        if (Boolean.TRUE.equals(config.getReceiptDateFlag())) {
            if (ObjUtil.isNull(reqVO.getReceiptDate())) {
                throw exception(WM_BATCH_RECEIPT_DATE_REQUIRED);
            }
            batch.setReceiptDate(reqVO.getReceiptDate());
        }
        // 有效期
        if (Boolean.TRUE.equals(config.getExpireDateFlag())) {
            if (ObjUtil.isNull(reqVO.getExpireDate())) {
                throw exception(WM_BATCH_EXPIRE_DATE_REQUIRED);
            }
            batch.setExpireDate(reqVO.getExpireDate());
        }
        // 供应商
        if (Boolean.TRUE.equals(config.getVendorFlag())) {
            if (ObjUtil.isNull(reqVO.getVendorId())) {
                throw exception(WM_BATCH_VENDOR_REQUIRED);
            }
            batch.setVendorId(reqVO.getVendorId());
        }
        // 客户
        if (Boolean.TRUE.equals(config.getClientFlag())) {
            if (ObjUtil.isNull(reqVO.getClientId())) {
                throw exception(WM_BATCH_CLIENT_REQUIRED);
            }
            batch.setClientId(reqVO.getClientId());
        }
        // 采购订单编号
        if (Boolean.TRUE.equals(config.getPurchaseOrderCodeFlag())) {
            if (ObjUtil.isNull(reqVO.getPurchaseOrderCode())) {
                throw exception(WM_BATCH_PURCHASE_ORDER_CODE_REQUIRED);
            }
            batch.setPurchaseOrderCode(reqVO.getPurchaseOrderCode());
        }
        // 销售订单编号
        if (Boolean.TRUE.equals(config.getSalesOrderCodeFlag())) {
            if (ObjUtil.isNull(reqVO.getSalesOrderCode())) {
                throw exception(WM_BATCH_CUSTOMER_ORDER_CODE_REQUIRED);
            }
            batch.setSalesOrderCode(reqVO.getSalesOrderCode());
        }
        // 生产工单
        if (Boolean.TRUE.equals(config.getWorkOrderFlag())) {
            if (ObjUtil.isNull(reqVO.getWorkOrderId())) {
                throw exception(WM_BATCH_WORK_ORDER_REQUIRED);
            }
            batch.setWorkOrderId(reqVO.getWorkOrderId());
        }
        // 生产任务
        if (Boolean.TRUE.equals(config.getTaskFlag())) {
            if (ObjUtil.isNull(reqVO.getTaskId())) {
                throw exception(WM_BATCH_TASK_REQUIRED);
            }
            batch.setTaskId(reqVO.getTaskId());
        }
        // 工作站
        if (Boolean.TRUE.equals(config.getWorkstationFlag())) {
            if (ObjUtil.isNull(reqVO.getWorkstationId())) {
                throw exception(WM_BATCH_WORKSTATION_REQUIRED);
            }
            batch.setWorkstationId(reqVO.getWorkstationId());
        }
        // 工具
        if (Boolean.TRUE.equals(config.getToolFlag())) {
            if (ObjUtil.isNull(reqVO.getToolId())) {
                throw exception(WM_BATCH_TOOL_REQUIRED);
            }
            batch.setToolId(reqVO.getToolId());
        }
        // 模具
        if (Boolean.TRUE.equals(config.getMoldFlag())) {
            if (ObjUtil.isNull(reqVO.getMoldId())) {
                throw exception(WM_BATCH_MOLD_REQUIRED);
            }
            batch.setMoldId(reqVO.getMoldId());
        }
        // 生产批号
        if (Boolean.TRUE.equals(config.getLotNumberFlag())) {
            if (ObjUtil.isNull(reqVO.getLotNumber())) {
                throw exception(WM_BATCH_LOT_NUMBER_REQUIRED);
            }
            batch.setLotNumber(reqVO.getLotNumber());
        }
        // 质量状态
        if (Boolean.TRUE.equals(config.getQualityStatusFlag())) {
            if (ObjUtil.isNull(reqVO.getQualityStatus())) {
                throw exception(WM_BATCH_QUALITY_STATUS_REQUIRED);
            }
            batch.setQualityStatus(reqVO.getQualityStatus());
        }

        // 3.1 情况一：查询是否存在匹配的批次
        MesWmBatchDO existingBatch = batchMapper.selectFirst(batch);
        if (existingBatch != null) {
            return existingBatch;
        }

        // 3.2 情况二：生成新批次
        String batchCode = autoCodeRecordService.generateAutoCode(MesMdAutoCodeRuleCodeEnum.WM_BATCH_CODE.getCode());
        batch.setCode(batchCode);
        batchMapper.insert(batch);

        // 4. 生成条码
        barcodeService.autoGenerateBarcode(MesBizTypeConstants.WM_BATCH, batch.getId(), batch.getCode(), item.getName());
        return batch;
    }

    @Override
    public List<MesWmBatchDO> getForwardBatchList(String code) {
        return getForwardBatchList(code, new HashSet<>(), 0);
    }

    private List<MesWmBatchDO> getForwardBatchList(String code, Set<String> visited, int depth) {
        if (code == null || !visited.add(code) || depth >= MAX_TRACE_DEPTH) {
            return new ArrayList<>();
        }
        List<MesWmBatchDO> list = batchMapper.selectListByForward(code);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        // 继续递归查询下游批次
        List<MesWmBatchDO> results = new ArrayList<>(list);
        for (MesWmBatchDO batch : list) {
            results.addAll(getForwardBatchList(batch.getCode(), visited, depth + 1));
        }
        return results;
    }

    @Override
    public List<MesWmBatchDO> getBackwardBatchList(String code) {
        return getBackwardBatchList(code, new HashSet<>(), 0);
    }

    private List<MesWmBatchDO> getBackwardBatchList(String code, Set<String> visited, int depth) {
        if (code == null || !visited.add(code) || depth >= MAX_TRACE_DEPTH) {
            return new ArrayList<>();
        }
        List<MesWmBatchDO> list = batchMapper.selectListByBackward(code);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        // 继续递归查询上游批次
        List<MesWmBatchDO> results = new ArrayList<>(list);
        for (MesWmBatchDO batch : list) {
            results.addAll(getBackwardBatchList(batch.getCode(), visited, depth + 1));
        }
        return results;
    }

    @Override
    public MesWmBatchDO validateBatchExists(Long batchId, Long itemId) {
        MesWmBatchDO batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw exception(WM_BATCH_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(batch.getItemId(), itemId)) {
            throw exception(WM_BATCH_ITEM_MISMATCH);
        }
        return batch;
    }

    @Override
    public MesWmBatchDO validateBatchExists(Long batchId, Long itemId, Long clientId, Long vendorId) {
        MesWmBatchDO batch = validateBatchExists(batchId, itemId);
        if (clientId != null && ObjUtil.notEqual(batch.getClientId(), clientId)) {
            throw exception(WM_BATCH_CLIENT_MISMATCH);
        }
        if (vendorId != null && ObjUtil.notEqual(batch.getVendorId(), vendorId)) {
            throw exception(WM_BATCH_VENDOR_MISMATCH);
        }
        return batch;
    }

    @Override
    public Long getBatchCountByToolId(Long toolId) {
        return batchMapper.selectCountByToolId(toolId);
    }

}
