package cn.iocoder.yudao.module.mes.service.wm.returnsales;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.line.MesWmReturnSalesLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.line.MesWmReturnSalesLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.returnsales.MesWmReturnSalesLineMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnSalesStatusEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.MD_ITEM_BATCH_REQUIRED;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_RETURN_SALES_LINE_NOT_EXISTS;

/**
 * MES 销售退货单行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmReturnSalesLineServiceImpl implements MesWmReturnSalesLineService {

    @Resource
    private MesWmReturnSalesLineMapper returnSalesLineMapper;

    @Resource
    @Lazy
    private MesWmReturnSalesService returnSalesService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmBatchService batchService;
    @Resource
    @Lazy
    private MesWmReturnSalesDetailService returnSalesDetailService;

    @Override
    public Long createReturnSalesLine(MesWmReturnSalesLineSaveReqVO createReqVO) {
        validateLineSaveData(createReqVO);

        // 插入
        MesWmReturnSalesLineDO line = BeanUtils.toBean(createReqVO, MesWmReturnSalesLineDO.class);
        // 质量状态自动赋值
        line.setQualityStatus(calculateQualityStatus(line.getRqcCheckFlag()));
        returnSalesLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateReturnSalesLine(MesWmReturnSalesLineSaveReqVO updateReqVO) {
        // 校验存在
        MesWmReturnSalesLineDO oldLine = validateReturnSalesLineExists(updateReqVO.getId());
        // 固定父单 ID，防止通过接口篡改
        updateReqVO.setReturnId(oldLine.getReturnId());
        validateLineSaveData(updateReqVO);

        // 更新
        MesWmReturnSalesLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmReturnSalesLineDO.class);
        // 质量状态自动赋值
        updateObj.setQualityStatus(calculateQualityStatus(updateObj.getRqcCheckFlag()));
        returnSalesLineMapper.updateById(updateObj);
    }

    /**
     * 校验保存时的关联数据
     */
    private void validateLineSaveData(MesWmReturnSalesLineSaveReqVO reqVO) {
        // 校验退货单存在 + 草稿状态
        returnSalesService.validateReturnSalesExistsAndPrepare(reqVO.getReturnId());
        // 校验物料存在 + 批次管理
        validateItemBatchManagement(reqVO.getReturnId(), reqVO.getItemId(), reqVO.getBatchId());
    }

    @Override
    public void deleteReturnSalesLine(Long id) {
        // 校验存在
        MesWmReturnSalesLineDO line = validateReturnSalesLineExists(id);
        // 校验主单为草稿状态才允许删除行
        returnSalesService.validateReturnSalesExistsAndPrepare(line.getReturnId());
        // 级联删除该行的明细
        returnSalesDetailService.deleteReturnSalesDetailByLineId(id);
        // 删除行
        returnSalesLineMapper.deleteById(id);
    }

    @Override
    public MesWmReturnSalesLineDO getReturnSalesLine(Long id) {
        return returnSalesLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmReturnSalesLineDO> getReturnSalesLinePage(MesWmReturnSalesLinePageReqVO pageReqVO) {
        return returnSalesLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmReturnSalesLineDO> getReturnSalesLineListByReturnId(Long returnId) {
        return returnSalesLineMapper.selectListByReturnId(returnId);
    }

    @Override
    public void deleteReturnSalesLineByReturnId(Long returnId) {
        returnSalesLineMapper.deleteByReturnId(returnId);
    }

    @Override
    public MesWmReturnSalesLineDO validateReturnSalesLineExists(Long id) {
        MesWmReturnSalesLineDO line = returnSalesLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_RETURN_SALES_LINE_NOT_EXISTS);
        }
        return line;
    }

    @Override
    public void updateQualityStatusByReturnId(Long returnId, Integer qualityStatus) {
        List<MesWmReturnSalesLineDO> lines = returnSalesLineMapper.selectListByReturnId(returnId);
        for (MesWmReturnSalesLineDO line : lines) {
            Integer newStatus = calculateQualityStatus(line.getRqcCheckFlag());
            if (ObjUtil.notEqual(newStatus, line.getQualityStatus())) {
                returnSalesLineMapper.updateById(new MesWmReturnSalesLineDO()
                        .setId(line.getId()).setQualityStatus(newStatus));
            }
        }
    }

    private Integer calculateQualityStatus(Boolean rqcCheckFlag) {
        if (Boolean.TRUE.equals(rqcCheckFlag)) {
            return MesWmQualityStatusEnum.PENDING.getStatus(); // 待检
        }
        return MesWmQualityStatusEnum.PASS.getStatus(); // 不需检验，默认合格
    }

    /**
     * 校验物料批次管理
     *
     * @param returnId 退货单ID
     * @param itemId   物料ID
     * @param batchId  批次ID
     */
    private void validateItemBatchManagement(Long returnId, Long itemId, Long batchId) {
        MesMdItemDO item = itemService.validateItemExistsAndEnable(itemId);
        // 如果物料启用了批次管理，则必须选择批次
        if (Boolean.TRUE.equals(item.getBatchFlag()) && batchId == null) {
            throw exception(MD_ITEM_BATCH_REQUIRED);
        }
        // 校验批次存在且属于当前物料和客户
        if (batchId != null) {
            // 从父单获取客户ID，校验批次归属
            MesWmReturnSalesDO returnSales = returnSalesService.validateReturnSalesExistsAndPrepare(returnId);
            batchService.validateBatchExists(batchId, itemId, returnSales.getClientId(), null);
        }
    }

    @Override
    public void updateReturnSalesLineWhenRqcFinish(Long sourceLineId, Long sourceDocId, Integer checkResult,
                                                    BigDecimal qualifiedQuantity, BigDecimal unqualifiedQuantity) {
        MesWmReturnSalesLineDO sourceLine = validateReturnSalesLineExists(sourceLineId);
        boolean hasUnqualified = unqualifiedQuantity != null && unqualifiedQuantity.compareTo(BigDecimal.ZERO) > 0;
        boolean hasQualified = qualifiedQuantity != null && qualifiedQuantity.compareTo(BigDecimal.ZERO) > 0;

        // 1. 根据合格/不合格品数量，更新退货单行的质量状态
        if (!hasUnqualified) {
            // 1.A 情况一：全部合格
            returnSalesLineMapper.updateById(new MesWmReturnSalesLineDO()
                    .setId(sourceLineId).setQualityStatus(MesWmQualityStatusEnum.PASS.getStatus()));
        } else if (!hasQualified) {
            // 1.B 情况二：全部不合格
            returnSalesLineMapper.updateById(new MesWmReturnSalesLineDO()
                    .setId(sourceLineId).setQualityStatus(MesWmQualityStatusEnum.FAIL.getStatus()));
        } else {
            // 1.C 情况三：部分合格部分不合格 → 拆分行
            // 1.C.1 新增一行不合格品
            MesWmReturnSalesLineDO unqualifiedLine = new MesWmReturnSalesLineDO()
                    .setReturnId(sourceLine.getReturnId())
                    .setItemId(sourceLine.getItemId())
                    .setQuantity(unqualifiedQuantity)
                    .setBatchId(sourceLine.getBatchId())
                    .setRqcId(sourceLine.getRqcId())
                    .setRqcCheckFlag(sourceLine.getRqcCheckFlag())
                    .setQualityStatus(MesWmQualityStatusEnum.FAIL.getStatus())
                    .setRemark(sourceLine.getRemark());
            returnSalesLineMapper.insert(unqualifiedLine);
            // 1.C.2 更新原行为合格品
            returnSalesLineMapper.updateById(new MesWmReturnSalesLineDO()
                    .setId(sourceLineId)
                    .setQuantity(qualifiedQuantity)
                    .setQualityStatus(MesWmQualityStatusEnum.PASS.getStatus()));
        }

        // 2. 检查退货单下是否还有待检验的行，若全检完则将退货单状态设为"待上架"
        if (sourceDocId != null) {
            List<MesWmReturnSalesLineDO> allLines = returnSalesLineMapper.selectListByReturnId(sourceDocId);
            boolean allInspected = !CollUtil.contains(allLines,
                    line -> Objects.equals(line.getQualityStatus(), MesWmQualityStatusEnum.PENDING.getStatus()));
            if (allInspected) {
                returnSalesService.updateReturnSalesStatus(sourceDocId, MesWmReturnSalesStatusEnum.APPROVING.getStatus());
            }
        }
    }

}
