package cn.iocoder.yudao.module.mes.service.qc.iqc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.mes.controller.admin.qc.iqc.vo.MesQcIqcPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.iqc.vo.MesQcIqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateItemDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.iqc.MesQcIqcMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.qc.defectrecord.MesQcDefectRecordService;
import cn.iocoder.yudao.module.mes.service.qc.indicatorresult.MesQcIndicatorResultService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateItemService;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeService;
import cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt.MesWmOutsourceReceiptService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 来料检验单（IQC） Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcIqcServiceImpl implements MesQcIqcService {

    @Resource
    private MesQcIqcMapper iqcMapper;

    @Resource
    private MesQcTemplateItemService templateItemService;
    @Resource
    private MesQcIqcLineService iqcLineService;
    @Resource
    private MesQcDefectRecordService defectRecordService;
    @Resource
    private MesWmArrivalNoticeService arrivalNoticeService;
    @Resource
    private MesWmOutsourceReceiptService outsourceReceiptService;
    @Resource
    private MesMdVendorService vendorService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    @Lazy
    private MesQcIndicatorResultService indicatorResultService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createIqc(MesQcIqcSaveReqVO createReqVO) {
        // 1.1 校验数据
        validateIqcSaveData(createReqVO);
        // 1.2 校验来源单据参数，并验证数据存在
        String sourceDocCode = validateAndGetSourceDocCode(
                createReqVO.getSourceDocType(), createReqVO.getSourceDocId(), createReqVO.getSourceLineId());
        // 1.3 通过 itemId + IQC 类型自动查找模板
        MesQcTemplateItemDO templateItem = templateItemService.getRequiredTemplateByItemIdAndType(
                createReqVO.getItemId(), MesQcTypeEnum.IQC.getType());
        Long templateId = templateItem.getTemplateId();

        // 2. 插入主表
        MesQcIqcDO iqc = BeanUtils.toBean(createReqVO, MesQcIqcDO.class)
                .setTemplateId(templateId)
                .setCheckQuantity(createReqVO.getQualifiedQuantity().add(createReqVO.getUnqualifiedQuantity()))
                .setSourceDocCode(sourceDocCode)
                .setStatus(MesQcStatusEnum.DRAFT.getStatus());
        iqcMapper.insert(iqc);

        // 3. 从模板指标自动生成检验行
        iqcLineService.createLinesFromTemplate(iqc.getId(), templateId);
        return iqc.getId();
    }

    @Override
    public void updateIqc(MesQcIqcSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 草稿状态
        validateIqcStatusPrepare(updateReqVO.getId());
        // 1.2 校验数据
        validateIqcSaveData(updateReqVO);

        // 2. 更新主表
        MesQcIqcDO updateObj = BeanUtils.toBean(updateReqVO, MesQcIqcDO.class);
        updateObj.setSourceDocType(null).setSourceDocId(null).setSourceLineId(null); // 不允许修改来源单据
        updateObj.setTemplateId(null); // 不允许修改模板
        updateObj.setItemId(null); // 不允许修改物料
        updateObj.setCheckQuantity(updateReqVO.getQualifiedQuantity().add(updateReqVO.getUnqualifiedQuantity()));
        iqcMapper.updateById(updateObj);
    }

    private void validateIqcSaveData(MesQcIqcSaveReqVO reqVO) {
        // 校验编号唯一
        validateIqcCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验供应商、物料、检测人员存在
        vendorService.validateVendorExists(reqVO.getVendorId());
        itemService.validateItemExists(reqVO.getItemId());
        adminUserApi.validateUser(reqVO.getInspectorUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishIqc(Long id) {
        // 1.1 校验存在 + 草稿状态
        MesQcIqcDO iqc = validateIqcStatusPrepare(id);
        if (iqc.getCheckResult() == null) {
            throw exception(QC_IQC_CHECK_RESULT_EMPTY);
        }
        // 1.2 校验至少存在一条检测结果
        indicatorResultService.validateIndicatorResultExistsByQcIdAndType(id, MesQcTypeEnum.IQC.getType());

        // 2. 更新状态为已完成
        MesQcIqcDO updateObj = new MesQcIqcDO()
                .setId(id).setStatus(MesQcStatusEnum.FINISHED.getStatus());
        iqcMapper.updateById(updateObj);

        // 3. 回写来源单据
        writeBackSourceDoc(iqc);
    }

    /**
     * 回写来源单据（IQC 完成后）
     *
     * @param iqc 来料检验单
     */
    private void writeBackSourceDoc(MesQcIqcDO iqc) {
        // 校验：sourceDocType 非空时，lineId 和 docId 必须非空，否则为脏数据
        if (iqc.getSourceDocType() == null) {
            return;
        }
        if (ObjectUtil.hasNull(iqc.getSourceLineId(), iqc.getSourceDocId())) {
            throw new IllegalArgumentException(
                    "IQC 单[" + iqc.getId() + "] sourceDocType 非空但 sourceLineId 或 sourceDocId 为空");
        }

        // 根据不同的来源单据类型，回写不同的单据
        if (Objects.equals(iqc.getSourceDocType(), MesBizTypeConstants.WM_ARRIVAL_NOTICE)) {
            arrivalNoticeService.updateArrivalNoticeWhenIqcFinish(iqc.getSourceDocId(), iqc.getSourceLineId(),
                    iqc.getId(), iqc.getQualifiedQuantity());
        } else if (Objects.equals(iqc.getSourceDocType(), MesBizTypeConstants.WM_OUTSOURCE_RECPT)) {
            outsourceReceiptService.updateOutsourceReceiptWhenIqcFinish(iqc.getSourceDocId(), iqc.getSourceLineId(),
                    iqc.getId(), iqc.getQualifiedQuantity(), iqc.getUnqualifiedQuantity());
        } else {
            throw new IllegalArgumentException(
                    "IQC 单[" + iqc.getId() + "] sourceDocType=" + iqc.getSourceDocType() + " 无法识别，无法回写来源单据");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIqc(Long id) {
        // 1. 校验存在 + 草稿状态
        validateIqcStatusPrepare(id);

        // 2.1 删除主表
        iqcMapper.deleteById(id);
        // 2.2 级联删除行
        iqcLineService.deleteListByIqcId(id);
        // 2.3 级联删除缺陷记录
        defectRecordService.deleteListByQcTypeAndQcId(MesQcTypeEnum.IQC.getType(), id);
    }

    @Override
    public MesQcIqcDO validateIqcExists(Long id) {
        MesQcIqcDO iqc = iqcMapper.selectById(id);
        if (iqc == null) {
            throw exception(QC_IQC_NOT_EXISTS);
        }
        return iqc;
    }

    /**
     * 校验来料检验单存在且为草稿状态
     *
     * @param id 来料检验单 ID
     * @return 来料检验单
     */
    private MesQcIqcDO validateIqcStatusPrepare(Long id) {
        MesQcIqcDO iqc = validateIqcExists(id);
        if (ObjUtil.notEqual(iqc.getStatus(), MesQcStatusEnum.DRAFT.getStatus())) {
            throw exception(QC_IQC_NOT_PREPARE);
        }
        return iqc;
    }

    private void validateIqcCodeUnique(Long id, String code) {
        MesQcIqcDO iqc = iqcMapper.selectByCode(code);
        if (iqc == null) {
            return;
        }
        if (ObjUtil.notEqual(iqc.getId(), id)) {
            throw exception(QC_IQC_CODE_DUPLICATE);
        }
    }

    /**
     * 校验来源单据参数，并验证数据存在
     *
     * @param sourceDocType 来源单据类型
     * @param sourceDocId 来源单据 ID
     * @param sourceLineId 来源单据行 ID
     */
    private String validateAndGetSourceDocCode(Integer sourceDocType, Long sourceDocId, Long sourceLineId) {
        if (sourceDocType == null) {
            return null;
        }
        if (ObjUtil.hasNull(sourceDocId, sourceLineId)) {
            throw exception(QC_IQC_SOURCE_DOC_PARAMS_MISSING);
        }
        // 根据来源单据类型，校验数据存在且匹配
        if (Objects.equals(sourceDocType, MesBizTypeConstants.WM_ARRIVAL_NOTICE)) {
            arrivalNoticeService.validateArrivalNoticeAndLineExists(sourceDocId, sourceLineId);
            return arrivalNoticeService.getArrivalNotice(sourceDocId).getCode();
        } else if (Objects.equals(sourceDocType, MesBizTypeConstants.WM_OUTSOURCE_RECPT)) {
            outsourceReceiptService.validateOutsourceReceiptAndLineExists(sourceDocId, sourceLineId);
            return outsourceReceiptService.getOutsourceReceipt(sourceDocId).getCode();
        }
        return null;
    }

    @Override
    public MesQcIqcDO getIqc(Long id) {
        return iqcMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcIqcDO> getIqcPage(MesQcIqcPageReqVO pageReqVO) {
        return iqcMapper.selectPage(pageReqVO);
    }

    @Override
    public void recalculateDefectStats(Long iqcId, List<MesQcDefectRecordDO> records) {
        MesQcIqcDO iqc = validateIqcExists(iqcId);
        // 1. 行级缺陷统计
        iqcLineService.recalculateLineDefectStats(iqcId, records);

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
        if (iqc.getCheckQuantity() != null && iqc.getCheckQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal checkQty = iqc.getCheckQuantity();
            criticalRate = BigDecimal.valueOf(totalCritical).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
            majorRate = BigDecimal.valueOf(totalMajor).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
            minorRate = BigDecimal.valueOf(totalMinor).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
        }
        // 2.3 更新主表
        MesQcIqcDO updateIqc = new MesQcIqcDO().setId(iqcId)
                .setCriticalQuantity(totalCritical).setMajorQuantity(totalMajor).setMinorQuantity(totalMinor)
                .setCriticalRate(criticalRate).setMajorRate(majorRate).setMinorRate(minorRate);
        iqcMapper.updateById(updateIqc);
    }

    @Override
    public Map<Long, MesQcIqcDO> getIqcMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<MesQcIqcDO> list = iqcMapper.selectByIds(ids);
        return convertMap(list, MesQcIqcDO::getId);
    }

    @Override
    public Long getIqcCountByVendorId(Long vendorId) {
        return iqcMapper.selectCountByVendorId(vendorId);
    }

}
