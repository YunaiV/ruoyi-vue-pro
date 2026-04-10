package cn.iocoder.yudao.module.mes.service.wm.returnissue;

import cn.hutool.core.collection.CollUtil;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.MesWmReturnIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.MesWmReturnIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.returnissue.MesWmReturnIssueMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnIssueStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 生产退料单 Service 实现类
 */
@Service
@Validated
public class MesWmReturnIssueServiceImpl implements MesWmReturnIssueService {

    @Resource
    private MesWmReturnIssueMapper issueMapper;

    @Resource
    private MesWmReturnIssueLineService issueLineService;
    @Resource
    private MesWmReturnIssueDetailService issueDetailService;
    @Resource
    private MesMdWorkstationService workstationService;
    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmTransactionService wmTransactionService;
    @Resource
    private MesWmWarehouseService warehouseService;
    @Resource
    private MesWmWarehouseLocationService locationService;
    @Resource
    private MesWmWarehouseAreaService areaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturnIssue(MesWmReturnIssueSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateReturnIssueSaveData(createReqVO);

        // 2. 插入主表
        MesWmReturnIssueDO issue = BeanUtils.toBean(createReqVO, MesWmReturnIssueDO.class);
        issue.setStatus(MesWmReturnIssueStatusEnum.PREPARE.getStatus());
        issueMapper.insert(issue);
        return issue.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReturnIssue(MesWmReturnIssueSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 准备中状态
        MesWmReturnIssueDO oldIssue = validateReturnIssueExistsAndPrepare(updateReqVO.getId());
        // 1.2 校验关联数据
        validateReturnIssueSaveData(updateReqVO);

        // 2. 更新主表
        MesWmReturnIssueDO updateObj = BeanUtils.toBean(updateReqVO, MesWmReturnIssueDO.class);
        issueMapper.updateById(updateObj);

        // 3. 退料类型变更时，刷新所有行的质量状态
        if (ObjUtil.notEqual(oldIssue.getType(), updateReqVO.getType())) {
            issueLineService.updateReturnIssueQualityStatusByIssueId(updateReqVO.getId(), updateReqVO.getType());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReturnIssue(Long id) {
        // 1. 校验存在 + 准备中状态
        validateReturnIssueExistsAndPrepare(id);

        // 2.1 级联删除明细
        issueDetailService.deleteReturnIssueDetailByIssueId(id);
        // 2.2 级联删除行
        issueLineService.deleteReturnIssueLineByIssueId(id);
        // 2.3 删除主表
        issueMapper.deleteById(id);
    }

    @Override
    public MesWmReturnIssueDO getReturnIssue(Long id) {
        return issueMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmReturnIssueDO> getReturnIssuePage(MesWmReturnIssuePageReqVO pageReqVO) {
        return issueMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReturnIssue(Long id) {
        // 1.1 校验存在 + 草稿状态
        validateReturnIssueExistsAndPrepare(id);
        // 1.2 校验至少有一条行
        List<MesWmReturnIssueLineDO> lines = issueLineService.getReturnIssueLineListByIssueId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_RETURN_ISSUE_NO_LINE);
        }

        // 2.1 确定目标状态：1）有待检验物料 → 待检验状态；2）无待检验物料 → 待上架状态
        boolean hasPendingQc = CollUtil.contains(lines,
                line -> MesWmQualityStatusEnum.PENDING.getStatus().equals(line.getQualityStatus()));
        Integer targetStatus = hasPendingQc ? MesWmReturnIssueStatusEnum.CONFIRMED.getStatus()
                : MesWmReturnIssueStatusEnum.APPROVING.getStatus();
        // 2.2 提交（草稿 → 待检验/待上架）
        issueMapper.updateById(new MesWmReturnIssueDO().setId(id).setStatus(targetStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockReturnIssue(Long id) {
        // 1.1 校验存在 + 待上架状态
        MesWmReturnIssueDO issue = validateReturnIssueExists(id);
        if (ObjUtil.notEqual(MesWmReturnIssueStatusEnum.APPROVING.getStatus(), issue.getStatus())) {
            throw exception(WM_RETURN_ISSUE_NOT_APPROVING);
        }
        // 1.2 检查每个行的明细数量是否完成上架
        List<MesWmReturnIssueLineDO> lines = issueLineService.getReturnIssueLineListByIssueId(id);
        if (CollUtil.isNotEmpty(lines)) {
            // 批量查询所有明细
            List<MesWmReturnIssueDetailDO> allDetails = issueDetailService.getReturnIssueDetailListByIssueId(id);
            Map<Long, List<MesWmReturnIssueDetailDO>> detailMap = CollectionUtils.convertMultiMap(
                    allDetails, MesWmReturnIssueDetailDO::getLineId);
            // 检查每行的明细数量
            for (MesWmReturnIssueLineDO line : lines) {
                List<MesWmReturnIssueDetailDO> details = detailMap.getOrDefault(line.getId(), Collections.emptyList());
                BigDecimal totalDetailQuantity = CollectionUtils.getSumValue(details,
                        MesWmReturnIssueDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
                // 对比行数量与明细总数量，不满足直接抛出
                if (line.getQuantity().compareTo(totalDetailQuantity) > 0) {
                    MesMdItemDO item = itemService.validateItemExists(line.getItemId());
                    throw exception(WM_RETURN_ISSUE_DETAIL_QUANTITY_MISMATCH,
                            item.getCode() + " " + item.getName() + " 未完成上架");
                }
            }
        }

        // 2. 入库上架（待上架 → 待执行退料）
        issueMapper.updateById(new MesWmReturnIssueDO()
                .setId(id).setStatus(MesWmReturnIssueStatusEnum.APPROVED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishReturnIssue(Long id) {
        // 1. 校验存在 + 待执行退料状态
        MesWmReturnIssueDO issue = validateReturnIssueExists(id);
        if (ObjUtil.notEqual(MesWmReturnIssueStatusEnum.APPROVED.getStatus(), issue.getStatus())) {
            throw exception(WM_RETURN_ISSUE_NOT_APPROVED);
        }

        // 2. 遍历所有明细，创建库存事务（增加库存 + 记录流水）
        createTransactionList(issue);

        // 3. 更新退料单状态
        issueMapper.updateById(new MesWmReturnIssueDO()
                .setId(id).setStatus(MesWmReturnIssueStatusEnum.FINISHED.getStatus()));
    }

    private void createTransactionList(MesWmReturnIssueDO issue) {
        // 1. 查询虚拟线边库
        MesWmWarehouseDO virtualWarehouse = warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);
        MesWmWarehouseLocationDO virtualLocation = locationService.getWarehouseLocationByCode(MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION);
        MesWmWarehouseAreaDO virtualArea = areaService.getWarehouseAreaByCode(MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA);

        // 2. 遍历明细，每条明细产生 OUT（虚拟线边库扣减）+ IN（实际仓库增加）
        List<MesWmReturnIssueDetailDO> details = issueDetailService.getReturnIssueDetailListByIssueId(issue.getId());
        for (MesWmReturnIssueDetailDO detail : details) {
            // 2.1 先从虚拟线边库出库（库存减少）
            Long outTransactionId = wmTransactionService.createTransaction(new MesWmTransactionSaveReqDTO()
                    .setType(MesWmTransactionTypeEnum.OUT.getType()).setItemId(detail.getItemId())
                    .setQuantity(detail.getQuantity().negate()) // 库存减少
                    .setBatchId(detail.getBatchId()).setBatchCode(detail.getBatchCode())
                    .setWarehouseId(virtualWarehouse.getId()).setLocationId(virtualLocation.getId()).setAreaId(virtualArea.getId())
                    .setCheckFlag(false) // 线边库允许负库存
                    .setBizType(MesBizTypeConstants.WM_RETURN_ISSUE).setBizId(issue.getId())
                    .setBizCode(issue.getCode()).setBizLineId(detail.getLineId()));
            // 2.2 再入实际仓库（库存增加）
            wmTransactionService.createTransaction(new MesWmTransactionSaveReqDTO()
                    .setType(MesWmTransactionTypeEnum.IN.getType()).setItemId(detail.getItemId())
                    .setQuantity(detail.getQuantity()) // 库存增加（退料回仓）
                    .setBatchId(detail.getBatchId()).setBatchCode(detail.getBatchCode())
                    .setWarehouseId(detail.getWarehouseId()).setLocationId(detail.getLocationId()).setAreaId(detail.getAreaId())
                    .setBizType(MesBizTypeConstants.WM_RETURN_ISSUE).setBizId(issue.getId())
                    .setBizCode(issue.getCode()).setBizLineId(detail.getLineId())
                    .setRelatedTransactionId(outTransactionId)); // 关联出库事务
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReturnIssue(Long id) {
        // 校验存在
        MesWmReturnIssueDO issue = validateReturnIssueExists(id);
        // 已完成和已取消不允许取消
        if (ObjectUtils.equalsAny(issue.getStatus(),
                MesWmReturnIssueStatusEnum.FINISHED.getStatus(),
                MesWmReturnIssueStatusEnum.CANCELED.getStatus())) {
            throw exception(WM_RETURN_ISSUE_CANCEL_NOT_ALLOWED);
        }

        // 取消
        issueMapper.updateById(new MesWmReturnIssueDO()
                .setId(id).setStatus(MesWmReturnIssueStatusEnum.CANCELED.getStatus()));
    }

    @Override
    public MesWmReturnIssueDO validateReturnIssueExists(Long id) {
        MesWmReturnIssueDO issue = issueMapper.selectById(id);
        if (issue == null) {
            throw exception(WM_RETURN_ISSUE_NOT_EXISTS);
        }
        return issue;
    }

    /**
     * 校验生产退料单存在且为准备中状态
     */
    @Override
    public MesWmReturnIssueDO validateReturnIssueExistsAndPrepare(Long id) {
        MesWmReturnIssueDO issue = validateReturnIssueExists(id);
        if (ObjUtil.notEqual(MesWmReturnIssueStatusEnum.PREPARE.getStatus(), issue.getStatus())) {
            throw exception(WM_RETURN_ISSUE_NOT_PREPARE);
        }
        return issue;
    }

    @Override
    public void updateReturnIssueStatus(Long id, Integer status) {
        validateReturnIssueExists(id);
        issueMapper.updateById(new MesWmReturnIssueDO().setId(id).setStatus(status));
    }

    /**
     * 校验编码唯一性
     */
    private void validateCodeUnique(Long id, String code) {
        MesWmReturnIssueDO issue = issueMapper.selectByCode(code);
        if (issue == null) {
            return;
        }
        if (ObjUtil.notEqual(id, issue.getId())) {
            throw exception(WM_RETURN_ISSUE_CODE_DUPLICATE);
        }
    }

    /**
     * 校验保存时的关联数据
     */
    private void validateReturnIssueSaveData(MesWmReturnIssueSaveReqVO reqVO) {
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        workOrderService.validateWorkOrderExists(reqVO.getWorkOrderId());
        if (reqVO.getWorkstationId() != null) {
            workstationService.validateWorkstationExists(reqVO.getWorkstationId());
        }
    }

}
