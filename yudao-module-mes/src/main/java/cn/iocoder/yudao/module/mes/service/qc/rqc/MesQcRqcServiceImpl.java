package cn.iocoder.yudao.module.mes.service.qc.rqc;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.MesQcRqcPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.MesQcRqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateItemDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.rqc.MesQcRqcMapper;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.qc.defectrecord.MesQcDefectRecordService;
import cn.iocoder.yudao.module.mes.service.qc.indicatorresult.MesQcIndicatorResultService;
import cn.iocoder.yudao.module.mes.service.wm.returnissue.MesWmReturnIssueService;
import cn.iocoder.yudao.module.mes.service.wm.returnsales.MesWmReturnSalesService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateItemService;
import cn.iocoder.yudao.module.mes.service.wm.returnissue.MesWmReturnIssueLineService;
import cn.iocoder.yudao.module.mes.service.wm.returnsales.MesWmReturnSalesLineService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 退货检验单（RQC） Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcRqcServiceImpl implements MesQcRqcService {

    @Resource
    private MesQcRqcMapper rqcMapper;

    @Resource
    private MesQcTemplateItemService templateItemService;
    @Resource
    private MesQcRqcLineService rqcLineService;
    @Resource
    @Lazy
    private MesMdItemService itemService;
    @Resource
    @Lazy
    private MesQcDefectRecordService defectRecordService;
    @Resource
    @Lazy
    private MesWmReturnIssueLineService returnIssueLineService;
    @Resource
    @Lazy
    private MesWmReturnSalesLineService returnSalesLineService;
    @Resource
    @Lazy
    private MesWmReturnIssueService returnIssueService;
    @Resource
    @Lazy
    private MesWmReturnSalesService returnSalesService;
    @Resource
    @Lazy
    private MesQcIndicatorResultService indicatorResultService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRqc(MesQcRqcSaveReqVO createReqVO) {
        // 1.1 校验数据
        validateRqcSaveData(createReqVO);
        // 1.2 根据物料 + 检验类型自动匹配模板
        MesQcTemplateItemDO templateItem = templateItemService.getRequiredTemplateByItemIdAndType(
                createReqVO.getItemId(), createReqVO.getType());
        Long templateId = templateItem.getTemplateId();
        // 1.3 获取来源单据编号
        String sourceDocCode = validateAndGetSourceDocCode(
                createReqVO.getSourceDocType(), createReqVO.getSourceDocId());

        // 2. 插入主表
        MesQcRqcDO rqc = BeanUtils.toBean(createReqVO, MesQcRqcDO.class)
                .setTemplateId(templateId)
                .setSourceDocCode(sourceDocCode)
                .setStatus(MesQcStatusEnum.DRAFT.getStatus());
        rqcMapper.insert(rqc);

        // 3. 从模板指标自动生成检验行
        rqcLineService.createLinesFromTemplate(rqc.getId(), templateId);
        return rqc.getId();
    }

    @Override
    public void updateRqc(MesQcRqcSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 草稿状态
        validateRqcStatusPrepare(updateReqVO.getId());
        // 1.2 校验数据
        validateRqcSaveData(updateReqVO);

        // 2. 更新
        MesQcRqcDO updateObj = BeanUtils.toBean(updateReqVO, MesQcRqcDO.class);
        updateObj.setSourceDocType(null).setSourceDocId(null).setSourceLineId(null); // 不允许修改来源单据
        updateObj.setTemplateId(null); // 不允许修改模板
        updateObj.setItemId(null); // 不允许修改物料
        rqcMapper.updateById(updateObj);
    }

    private void validateRqcSaveData(MesQcRqcSaveReqVO reqVO) {
        // 校验编号唯一
        validateRqcCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验物料、检测人员存在
        itemService.validateItemExists(reqVO.getItemId());
        adminUserApi.validateUser(reqVO.getInspectorUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishRqc(Long id) {
        // 1.1 校验存在 + 草稿状态
        MesQcRqcDO rqc = validateRqcStatusPrepare(id);
        // 1.2 校验检测结论必填
        if (rqc.getCheckResult() == null) {
            throw exception(QC_RQC_CHECK_RESULT_EMPTY);
        }
        // 1.3 校验合格品 + 不合格品 = 检测数量
        if (rqc.getCheckQuantity() != null && rqc.getCheckQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal qualified = rqc.getQualifiedQuantity() != null ? rqc.getQualifiedQuantity() : BigDecimal.ZERO;
            BigDecimal unqualified = rqc.getUnqualifiedQuantity() != null ? rqc.getUnqualifiedQuantity() : BigDecimal.ZERO;
            if (qualified.add(unqualified).compareTo(rqc.getCheckQuantity()) != 0) {
                throw exception(QC_RQC_QUANTITY_MISMATCH);
            }
        }
        // 1.4 校验至少存在一条检测结果
        indicatorResultService.validateIndicatorResultExistsByQcIdAndType(id, MesQcTypeEnum.RQC.getType());

        // 2. 更新状态为已完成
        MesQcRqcDO updateObj = new MesQcRqcDO().setId(id).setStatus(MesQcStatusEnum.FINISHED.getStatus());
        rqcMapper.updateById(updateObj);

        // 3. 回写来源单据
        writeBackSourceDoc(rqc);
    }

    /**
     * 回写来源单据（RQC 完成后）
     *
     * <p>根据 sourceDocType 分发：
     * <ul>
     *     <li>WM_RETURN_ISSUE（生产退料单）→ 根据合格/不合格数量拆分行 + 更新 qualityStatus + 联动主单状态</li>
     *     <li>WM_RETURN_SALES（销售退货单）→ 同上</li>
     * </ul>
     *
     * @param rqc 退货检验单
     */
    private void writeBackSourceDoc(MesQcRqcDO rqc) {
        if (rqc.getSourceDocType() == null) {
            return;
        }
        if (rqc.getSourceLineId() == null) {
            throw new IllegalArgumentException(
                    "RQC 单[" + rqc.getId() + "] sourceDocType 非空但 sourceLineId 为空");
        }

        if (Objects.equals(rqc.getSourceDocType(), MesBizTypeConstants.WM_RETURN_ISSUE)) {
            // 回写生产退料单行：拆分行 + 更新质量状态 + 联动主单状态
            returnIssueLineService.updateReturnIssueLineWhenRqcFinish(
                    rqc.getSourceLineId(), rqc.getSourceDocId(), rqc.getCheckResult(),
                    rqc.getQualifiedQuantity(), rqc.getUnqualifiedQuantity());
        } else if (Objects.equals(rqc.getSourceDocType(), MesBizTypeConstants.WM_RETURN_SALES)) {
            // 回写销售退货单行：拆分行 + 更新质量状态 + 联动主单状态
            returnSalesLineService.updateReturnSalesLineWhenRqcFinish(
                    rqc.getSourceLineId(), rqc.getSourceDocId(), rqc.getCheckResult(),
                    rqc.getQualifiedQuantity(), rqc.getUnqualifiedQuantity());
        } else {
            throw new IllegalArgumentException(
                    "RQC 单[" + rqc.getId() + "] sourceDocType=" + rqc.getSourceDocType() + " 无法识别，无法回写来源单据");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRqc(Long id) {
        // 1. 校验存在 + 草稿状态
        validateRqcStatusPrepare(id);

        // 2.1 删除主表
        rqcMapper.deleteById(id);
        // 2.2 级联删除行
        rqcLineService.deleteByRqcId(id);
        // 2.3 级联删除缺陷记录
        defectRecordService.deleteListByQcTypeAndQcId(MesQcTypeEnum.RQC.getType(), id);
    }

    @Override
    public MesQcRqcDO validateRqcExists(Long id) {
        MesQcRqcDO rqc = rqcMapper.selectById(id);
        if (rqc == null) {
            throw exception(QC_RQC_NOT_EXISTS);
        }
        return rqc;
    }

    /**
     * 校验退货检验单存在且为草稿状态
     *
     * @param id 退货检验单 ID
     * @return 退货检验单
     */
    private MesQcRqcDO validateRqcStatusPrepare(Long id) {
        MesQcRqcDO rqc = validateRqcExists(id);
        if (ObjUtil.notEqual(rqc.getStatus(), MesQcStatusEnum.DRAFT.getStatus())) {
            throw exception(QC_RQC_NOT_PREPARE);
        }
        return rqc;
    }

    private void validateRqcCodeUnique(Long id, String code) {
        MesQcRqcDO rqc = rqcMapper.selectByCode(code);
        if (rqc == null) {
            return;
        }
        if (ObjUtil.notEqual(rqc.getId(), id)) {
            throw exception(QC_RQC_CODE_DUPLICATE);
        }
    }

    @Override
    public MesQcRqcDO getRqc(Long id) {
        return rqcMapper.selectById(id);
    }

    /**
     * 根据来源单据类型 + 来源单据 ID，推导来源单据编码
     */
    private String validateAndGetSourceDocCode(Integer sourceDocType, Long sourceDocId) {
        if (sourceDocType == null || sourceDocId == null) {
            return null;
        }
        if (Objects.equals(sourceDocType, MesBizTypeConstants.WM_RETURN_ISSUE)) {
            return returnIssueService.getReturnIssue(sourceDocId).getCode();
        } else if (Objects.equals(sourceDocType, MesBizTypeConstants.WM_RETURN_SALES)) {
            return returnSalesService.getReturnSales(sourceDocId).getCode();
        }
        return null;
    }

    @Override
    public PageResult<MesQcRqcDO> getRqcPage(MesQcRqcPageReqVO pageReqVO) {
        return rqcMapper.selectPage(pageReqVO);
    }

    @Override
    public void recalculateDefectStats(Long rqcId, List<MesQcDefectRecordDO> records) {
        MesQcRqcDO rqc = validateRqcExists(rqcId);
        // 1. 行级缺陷统计
        rqcLineService.recalculateLineDefectStats(rqcId, records);

        // 2.1 汇总主表的缺陷数量
        int totalCritical = 0;
        int totalMajor = 0;
        int totalMinor = 0;
        for (MesQcDefectRecordDO record : records) {
            int quantity = ObjUtil.defaultIfNull(record.getQuantity(), 1);
            if (Objects.equals(record.getLevel(), MesQcDefectLevelEnum.CRITICAL.getType())) {
                totalCritical += quantity;
            } else if (Objects.equals(record.getLevel(), MesQcDefectLevelEnum.MAJOR.getType())) {
                totalMajor += quantity;
            } else if (Objects.equals(record.getLevel(), MesQcDefectLevelEnum.MINOR.getType())) {
                totalMinor += quantity;
            } else {
                throw exception(QC_DEFECT_RECORD_LEVEL_UNKNOWN);
            }
        }
        // 2.2 计算缺陷率
        BigDecimal criticalRate = BigDecimal.ZERO;
        BigDecimal majorRate = BigDecimal.ZERO;
        BigDecimal minorRate = BigDecimal.ZERO;
        if (rqc.getCheckQuantity() != null && rqc.getCheckQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal checkQty = rqc.getCheckQuantity();
            criticalRate = BigDecimal.valueOf(totalCritical).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
            majorRate = BigDecimal.valueOf(totalMajor).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
            minorRate = BigDecimal.valueOf(totalMinor).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
        }
        // 2.3 更新主表
        MesQcRqcDO updateRqc = new MesQcRqcDO().setId(rqcId)
                .setCriticalQuantity(totalCritical).setMajorQuantity(totalMajor).setMinorQuantity(totalMinor)
                .setCriticalRate(criticalRate).setMajorRate(majorRate).setMinorRate(minorRate);
        rqcMapper.updateById(updateRqc);
    }

}
