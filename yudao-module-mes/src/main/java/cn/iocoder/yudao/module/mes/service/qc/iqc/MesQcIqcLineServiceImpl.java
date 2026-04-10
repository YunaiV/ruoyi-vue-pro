package cn.iocoder.yudao.module.mes.service.qc.iqc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.iqc.vo.line.MesQcIqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.iqc.MesQcIqcLineMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.template.MesQcTemplateIndicatorMapper;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum;
import cn.iocoder.yudao.module.mes.service.qc.indicator.MesQcIndicatorService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 来料检验单行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcIqcLineServiceImpl implements MesQcIqcLineService {

    @Resource
    private MesQcIqcLineMapper iqcLineMapper;
    @Resource
    private MesQcTemplateIndicatorMapper templateIndicatorMapper;

    @Resource
    private MesQcIndicatorService indicatorService;

    @Override
    public MesQcIqcLineDO validateIqcLineExists(Long id) {
        MesQcIqcLineDO line = iqcLineMapper.selectById(id);
        if (line == null) {
            throw exception(QC_IQC_LINE_NOT_EXISTS);
        }
        return line;
    }

    @Override
    public MesQcIqcLineDO getIqcLine(Long id) {
        return iqcLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcIqcLineDO> getIqcLinePage(MesQcIqcLinePageReqVO pageReqVO) {
        return iqcLineMapper.selectPage(pageReqVO);
    }

    @Override
    public void createLinesFromTemplate(Long iqcId, Long templateId) {
        List<MesQcTemplateIndicatorDO> templateIndicators = templateIndicatorMapper.selectListByTemplateId(templateId);
        if (CollUtil.isEmpty(templateIndicators)) {
            return;
        }
        Map<Long, MesQcIndicatorDO> indicatorMap = indicatorService.getIndicatorMap(
                convertSet(templateIndicators, MesQcTemplateIndicatorDO::getIndicatorId));
        List<MesQcIqcLineDO> lines = convertList(templateIndicators, templateIndicator -> {
            MesQcIndicatorDO indicator = indicatorMap.get(templateIndicator.getIndicatorId());
            return new MesQcIqcLineDO()
                    .setIqcId(iqcId).setIndicatorId(templateIndicator.getIndicatorId())
                    .setTool(indicator != null ? indicator.getTool() : null)
                    .setCheckMethod(templateIndicator.getCheckMethod())
                    .setStandardValue(templateIndicator.getStandardValue()).setUnitMeasureId(templateIndicator.getUnitMeasureId())
                    .setMaxThreshold(templateIndicator.getThresholdMax()).setMinThreshold(templateIndicator.getThresholdMin())
                    .setCriticalQuantity(0).setMajorQuantity(0).setMinorQuantity(0);
        });
        iqcLineMapper.insertBatch(lines);
    }

    @Override
    public void recalculateLineDefectStats(Long iqcId, List<MesQcDefectRecordDO> records) {
        List<MesQcIqcLineDO> lines = iqcLineMapper.selectListByIqcId(iqcId);
        if (CollUtil.isEmpty(lines)) {
            return;
        }
        List<MesQcIqcLineDO> updateLines = new ArrayList<>(lines.size());
        for (MesQcIqcLineDO line : lines) {
            int critical = 0;
            int major = 0;
            int minor = 0;
            for (MesQcDefectRecordDO record : records) {
                if (ObjUtil.notEqual(record.getLineId(), line.getId())) {
                    continue;
                }
                int quantity = ObjUtil.defaultIfNull(record.getQuantity(), 1);
                if (Objects.equals(record.getLevel(), MesQcDefectLevelEnum.CRITICAL.getType())) {
                    critical += quantity;
                } else if (Objects.equals(record.getLevel(), MesQcDefectLevelEnum.MAJOR.getType())) {
                    major += quantity;
                } else if (Objects.equals(record.getLevel(), MesQcDefectLevelEnum.MINOR.getType())) {
                    minor += quantity;
                } else {
                    throw exception(QC_DEFECT_RECORD_LEVEL_UNKNOWN);
                }
            }
            updateLines.add(new MesQcIqcLineDO().setId(line.getId())
                    .setCriticalQuantity(critical).setMajorQuantity(major).setMinorQuantity(minor));
        }
        for (MesQcIqcLineDO updateLine : updateLines) {
            iqcLineMapper.updateById(updateLine);
        }
    }

    @Override
    public List<MesQcIqcLineDO> getIqcLineListByIqcId(Long iqcId) {
        return iqcLineMapper.selectListByIqcId(iqcId);
    }

    @Override
    public void deleteListByIqcId(Long iqcId) {
        iqcLineMapper.deleteByIqcId(iqcId);
    }

}
