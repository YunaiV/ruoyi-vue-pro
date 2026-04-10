package cn.iocoder.yudao.module.mes.service.qc.oqc;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.MesQcOqcPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.MesQcOqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateItemDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.oqc.MesQcOqcMapper;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcStatusEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.qc.defectrecord.MesQcDefectRecordService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateItemService;
import cn.iocoder.yudao.module.mes.service.wm.productsales.MesWmProductSalesLineService;
import cn.iocoder.yudao.module.mes.service.wm.productsales.MesWmProductSalesService;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesLineDO;
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
 * MES 出货检验单（OQC） Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcOqcServiceImpl implements MesQcOqcService {

    @Resource
    private MesQcOqcMapper oqcMapper;
    @Resource
    private MesQcTemplateItemService templateItemService;
    @Resource
    private MesQcOqcLineService oqcLineService;
    @Resource
    @Lazy
    private MesQcDefectRecordService defectRecordService;
    @Resource
    @Lazy
    private MesMdClientService clientService;
    @Resource
    @Lazy
    private MesMdItemService itemService;
    @Resource
    @Lazy
    private MesWmProductSalesLineService productSalesLineService;
    @Resource
    @Lazy
    private MesWmProductSalesService productSalesService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOqc(MesQcOqcSaveReqVO createReqVO) {
        // 1.1 校验数据
        validateOqcSaveData(createReqVO);
        // 1.2 校验来源单据参数，并验证数据存在
        String sourceDocCode = validateAndGetSourceDocCode(
                createReqVO.getSourceDocType(), createReqVO.getSourceLineId());
        // 1.3 根据产品 + 检验类型自动匹配模板
        MesQcTemplateItemDO templateItem = templateItemService.getRequiredTemplateByItemIdAndType(
                createReqVO.getItemId(), MesQcTypeEnum.OQC.getType());
        Long templateId = templateItem.getTemplateId();

        // 2. 插入主表
        MesQcOqcDO oqc = BeanUtils.toBean(createReqVO, MesQcOqcDO.class)
                .setTemplateId(templateId)
                .setSourceDocCode(sourceDocCode)
                .setStatus(MesQcStatusEnum.DRAFT.getStatus());
        oqcMapper.insert(oqc);

        // 3. 从模板指标自动生成检验行
        oqcLineService.createLinesFromTemplate(oqc.getId(), templateId);
        return oqc.getId();
    }

    @Override
    public void updateOqc(MesQcOqcSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 草稿状态
        validateOqcStatusPrepare(updateReqVO.getId());
        // 1.2 校验数据
        validateOqcSaveData(updateReqVO);

        // 2. 更新
        MesQcOqcDO updateObj = BeanUtils.toBean(updateReqVO, MesQcOqcDO.class);
        updateObj.setSourceDocType(null).setSourceDocId(null).setSourceLineId(null); // 不允许修改来源单据
        updateObj.setTemplateId(null); // 不允许修改模板
        updateObj.setItemId(null); // 不允许修改物料
        oqcMapper.updateById(updateObj);
    }

    private void validateOqcSaveData(MesQcOqcSaveReqVO reqVO) {
        // 校验编号唯一
        validateOqcCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验客户、物料、检测人员存在
        clientService.validateClientExists(reqVO.getClientId());
        itemService.validateItemExists(reqVO.getItemId());
        adminUserApi.validateUser(reqVO.getInspectorUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishOqc(Long id) {
        // 1.1 校验存在 + 草稿状态
        MesQcOqcDO oqc = validateOqcStatusPrepare(id);
        // 1.2 校验检测结论必填
        if (oqc.getCheckResult() == null) {
            throw exception(QC_OQC_CHECK_RESULT_EMPTY);
        }

        // 2. 更新状态为已完成
        MesQcOqcDO updateObj = new MesQcOqcDO()
                .setId(id).setStatus(MesQcStatusEnum.FINISHED.getStatus());
        oqcMapper.updateById(updateObj);

        // 3. 回写来源单据
        writeBackSourceDoc(oqc);
    }

    /**
     * 回写来源单据（OQC 完成后）
     *
     * <p>当来源为销售出库单时，调用
     * {@link MesWmProductSalesLineService#updateProductSalesLineWhenOqcFinish}
     * 回写行的 oqcId、qualityStatus，并由该方法内部联动检查出库单整体状态
     *
     * @param oqc 出货检验单
     */
    private void writeBackSourceDoc(MesQcOqcDO oqc) {
        if (oqc.getSourceDocType() == null) {
            return;
        }
        if (oqc.getSourceLineId() == null) {
            throw new IllegalArgumentException(
                    "OQC 单[" + oqc.getId() + "] sourceDocType 非空但 sourceLineId 为空");
        }

        if (Objects.equals(oqc.getSourceDocType(), MesBizTypeConstants.WM_PRODUCT_SALES)) {
            productSalesLineService.updateProductSalesLineWhenOqcFinish(
                    oqc.getSourceLineId(), oqc.getId(), oqc.getCheckResult());
        } else {
            throw new IllegalArgumentException(
                    "OQC 单[" + oqc.getId() + "] sourceDocType=" + oqc.getSourceDocType() + " 无法识别，无法回写来源单据");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOqc(Long id) {
        // 1. 校验存在 + 草稿状态
        validateOqcStatusPrepare(id);

        // 2.1 删除主表
        oqcMapper.deleteById(id);
        // 2.2 级联删除行
        oqcLineService.deleteByOqcId(id);
        // 2.3 级联删除缺陷记录
        defectRecordService.deleteListByQcTypeAndQcId(MesQcTypeEnum.OQC.getType(), id);
    }

    @Override
    public MesQcOqcDO validateOqcExists(Long id) {
        MesQcOqcDO oqc = oqcMapper.selectById(id);
        if (oqc == null) {
            throw exception(QC_OQC_NOT_EXISTS);
        }
        return oqc;
    }

    /**
     * 校验出货检验单存在且为草稿状态
     */
    private MesQcOqcDO validateOqcStatusPrepare(Long id) {
        MesQcOqcDO oqc = validateOqcExists(id);
        if (ObjUtil.notEqual(oqc.getStatus(), MesQcStatusEnum.DRAFT.getStatus())) {
            throw exception(QC_OQC_NOT_PREPARE);
        }
        return oqc;
    }

    private void validateOqcCodeUnique(Long id, String code) {
        MesQcOqcDO oqc = oqcMapper.selectByCode(code);
        if (oqc == null) {
            return;
        }
        if (ObjUtil.notEqual(oqc.getId(), id)) {
            throw exception(QC_OQC_CODE_DUPLICATE);
        }
    }

    /**
     * 校验来源单据参数，并验证数据存在
     *
     * @param sourceDocType 来源单据类型
     * @param sourceLineId  来源单据行 ID
     * @return 来源单据编号
     */
    private String validateAndGetSourceDocCode(Integer sourceDocType, Long sourceLineId) {
        if (sourceDocType == null || sourceLineId == null) {
            return null;
        }
        if (Objects.equals(sourceDocType, MesBizTypeConstants.WM_PRODUCT_SALES)) {
            MesWmProductSalesLineDO salesLine = productSalesLineService.getProductSalesLine(sourceLineId);
            if (salesLine != null && salesLine.getSalesId() != null) {
                MesWmProductSalesDO sales = productSalesService.getProductSales(salesLine.getSalesId());
                return sales != null ? sales.getCode() : null;
            }
        }
        return null;
    }

    @Override
    public MesQcOqcDO getOqc(Long id) {
        return oqcMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcOqcDO> getOqcPage(MesQcOqcPageReqVO pageReqVO) {
        return oqcMapper.selectPage(pageReqVO);
    }

    @Override
    public void recalculateDefectStats(Long oqcId, List<MesQcDefectRecordDO> records) {
        // 1. 行级缺陷统计
        oqcLineService.recalculateLineDefectStats(oqcId, records);

        // 2.1 汇总主表的缺陷数量
        int totalCritical = 0, totalMajor = 0, totalMinor = 0;
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
        MesQcOqcDO oqc = validateOqcExists(oqcId);
        BigDecimal criticalRate = BigDecimal.ZERO;
        BigDecimal majorRate = BigDecimal.ZERO;
        BigDecimal minorRate = BigDecimal.ZERO;
        if (oqc.getCheckQuantity() != null && oqc.getCheckQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal checkQty = oqc.getCheckQuantity();
            criticalRate = BigDecimal.valueOf(totalCritical).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
            majorRate = BigDecimal.valueOf(totalMajor).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
            minorRate = BigDecimal.valueOf(totalMinor).multiply(BigDecimal.valueOf(100))
                    .divide(checkQty, 2, RoundingMode.HALF_UP);
        }
        // 2.3 更新主表
        MesQcOqcDO updateOqc = new MesQcOqcDO().setId(oqcId)
                .setCriticalQuantity(totalCritical).setMajorQuantity(totalMajor).setMinorQuantity(totalMinor)
                .setCriticalRate(criticalRate).setMajorRate(majorRate).setMinorRate(minorRate);
        oqcMapper.updateById(updateOqc);
    }

}
