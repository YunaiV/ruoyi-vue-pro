package cn.iocoder.yudao.module.mes.service.wm.returnissue;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.detail.MesWmReturnIssueDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.returnissue.MesWmReturnIssueDetailMapper;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 生产退料明细 Service 实现类
 */
@Service
@Validated
public class MesWmReturnIssueDetailServiceImpl implements MesWmReturnIssueDetailService {

    @Resource
    private MesWmReturnIssueDetailMapper issueDetailMapper;

    @Resource
    @Lazy
    private MesWmReturnIssueLineService issueLineService;
    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;
    @Resource
    @Lazy
    private MesWmMaterialStockService materialStockService;

    @Override
    public Long createReturnIssueDetail(MesWmReturnIssueDetailSaveReqVO createReqVO) {
        validateReturnIssueDetailSaveData(createReqVO);

        // 插入
        MesWmReturnIssueDetailDO detail = BeanUtils.toBean(createReqVO, MesWmReturnIssueDetailDO.class);
        issueDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateReturnIssueDetail(MesWmReturnIssueDetailSaveReqVO updateReqVO) {
        // 校验存在
        validateReturnIssueDetailExists(updateReqVO.getId());
        validateReturnIssueDetailSaveData(updateReqVO);

        // 更新
        MesWmReturnIssueDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmReturnIssueDetailDO.class);
        issueDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteReturnIssueDetail(Long id) {
        // 校验存在
        validateReturnIssueDetailExists(id);
        // 删除
        issueDetailMapper.deleteById(id);
    }

    @Override
    public MesWmReturnIssueDetailDO getReturnIssueDetail(Long id) {
        return issueDetailMapper.selectById(id);
    }

    @Override
    public List<MesWmReturnIssueDetailDO> getReturnIssueDetailListByLineId(Long lineId) {
        return issueDetailMapper.selectListByLineId(lineId);
    }

    @Override
    public List<MesWmReturnIssueDetailDO> getReturnIssueDetailListByIssueId(Long issueId) {
        return issueDetailMapper.selectListByIssueId(issueId);
    }

    @Override
    public void deleteReturnIssueDetailByIssueId(Long issueId) {
        issueDetailMapper.deleteByIssueId(issueId);
    }

    @Override
    public void deleteReturnIssueDetailByLineId(Long lineId) {
        issueDetailMapper.deleteByLineId(lineId);
    }

    private void validateReturnIssueDetailExists(Long id) {
        if (issueDetailMapper.selectById(id) == null) {
            throw exception(WM_RETURN_ISSUE_DETAIL_NOT_EXISTS);
        }
    }

    /**
     * 校验保存时的关联数据
     */
    private void validateReturnIssueDetailSaveData(MesWmReturnIssueDetailSaveReqVO reqVO) {
        // 校验父数据存在
        MesWmReturnIssueLineDO line = issueLineService.validateReturnIssueLineExists(reqVO.getLineId());
        if (ObjUtil.notEqual(line.getIssueId(), reqVO.getIssueId())) {
            throw exception(WM_RETURN_ISSUE_DETAIL_LINE_NOT_MATCH);
        }
        if (ObjUtil.notEqual(line.getItemId(), reqVO.getItemId())) {
            throw exception(WM_RETURN_ISSUE_DETAIL_ITEM_MISMATCH);
        }
        // 校验仓库、库区、库位的关联关系
        warehouseAreaService.validateWarehouseAreaExists(
                reqVO.getWarehouseId(), reqVO.getLocationId(), reqVO.getAreaId());
        // 校验库位物料/批次混放规则
        materialStockService.checkAreaMixingRule(reqVO.getAreaId(), reqVO.getItemId(), reqVO.getBatchId());
        // 校验库存记录
        materialStockService.validateSelectedStock(
                reqVO.getMaterialStockId(), reqVO.getItemId(), reqVO.getBatchId(), reqVO.getBatchCode(),
                reqVO.getWarehouseId(), reqVO.getLocationId(), reqVO.getAreaId(), reqVO.getQuantity());
        // 校验明细总数量不超过行数量（排除自身）
        validateDetailQuantityNotExceed(reqVO.getLineId(), reqVO.getQuantity(), reqVO.getId(), line);
    }

    /**
     * 校验明细总数量不超过行数量
     *
     * @param lineId 行 ID
     * @param newQuantity 本次新增/修改的数量
     * @param excludeDetailId 排除的明细 ID（更新时排除自身，新增时传 null）
     * @param line 退料单行
     */
    private void validateDetailQuantityNotExceed(Long lineId, BigDecimal newQuantity,
                                                  Long excludeDetailId, MesWmReturnIssueLineDO line) {
        // 计算已有明细总数量（排除自身）
        List<MesWmReturnIssueDetailDO> details = issueDetailMapper.selectListByLineId(lineId);
        BigDecimal existingTotal = CollectionUtils.getSumValue(details,
                detail -> excludeDetailId != null && excludeDetailId.equals(detail.getId())
                        ? BigDecimal.ZERO : detail.getQuantity(),
                BigDecimal::add, BigDecimal.ZERO);
        // 校验：已有 + 本次 <= 行数量
        if (existingTotal.add(newQuantity).compareTo(line.getQuantity()) > 0) {
            throw exception(WM_RETURN_ISSUE_DETAIL_QUANTITY_EXCEED);
        }
    }

}
