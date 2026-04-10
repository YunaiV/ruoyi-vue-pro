package cn.iocoder.yudao.module.mes.service.qc.oqc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.line.MesQcOqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.oqc.MesQcOqcLineMapper;
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
 * MES 出货检验单行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcOqcLineServiceImpl implements MesQcOqcLineService {

    @Resource
    private MesQcOqcLineMapper oqcLineMapper;
    @Resource
    private MesQcTemplateIndicatorMapper templateIndicatorMapper;
    @Resource
    private MesQcIndicatorService indicatorService;

    @Override
    public MesQcOqcLineDO validateOqcLineExists(Long id) {
        MesQcOqcLineDO line = oqcLineMapper.selectById(id);
        if (line == null) {
            throw exception(QC_OQC_LINE_NOT_EXISTS);
        }
        return line;
    }

    @Override
    public MesQcOqcLineDO getOqcLine(Long id) {
        return oqcLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcOqcLineDO> getOqcLinePage(MesQcOqcLinePageReqVO pageReqVO) {
        return oqcLineMapper.selectPage(pageReqVO);
    }

    @Override
    public void createLinesFromTemplate(Long oqcId, Long templateId) {
        List<MesQcTemplateIndicatorDO> templateIndicators = templateIndicatorMapper.selectListByTemplateId(templateId);
        if (CollUtil.isEmpty(templateIndicators)) {
            return;
        }
        Map<Long, MesQcIndicatorDO> indicatorMap = indicatorService.getIndicatorMap(
                convertSet(templateIndicators, MesQcTemplateIndicatorDO::getIndicatorId));
        List<MesQcOqcLineDO> lines = convertList(templateIndicators, templateIndicator -> {
            MesQcIndicatorDO indicator = indicatorMap.get(templateIndicator.getIndicatorId());
            return new MesQcOqcLineDO()
                    .setOqcId(oqcId).setIndicatorId(templateIndicator.getIndicatorId())
                    .setTool(indicator != null ? indicator.getTool() : null)
                    .setCheckMethod(templateIndicator.getCheckMethod())
                    .setStandardValue(templateIndicator.getStandardValue()).setUnitMeasureId(templateIndicator.getUnitMeasureId())
                    .setMaxThreshold(templateIndicator.getThresholdMax()).setMinThreshold(templateIndicator.getThresholdMin())
                    .setCriticalQuantity(0).setMajorQuantity(0).setMinorQuantity(0);
        });
        oqcLineMapper.insertBatch(lines);
    }

    @Override
    public void recalculateLineDefectStats(Long oqcId, List<MesQcDefectRecordDO> records) {
        List<MesQcOqcLineDO> lines = oqcLineMapper.selectListByOqcId(oqcId);
        if (CollUtil.isEmpty(lines)) {
            return;
        }
        List<MesQcOqcLineDO> updateLines = new ArrayList<>(lines.size());
        for (MesQcOqcLineDO line : lines) {
            int critical = 0, major = 0, minor = 0;
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
            updateLines.add(new MesQcOqcLineDO().setId(line.getId())
                    .setCriticalQuantity(critical).setMajorQuantity(major).setMinorQuantity(minor));
        }
        for (MesQcOqcLineDO updateLine : updateLines) {
            oqcLineMapper.updateById(updateLine);
        }
    }

    @Override
    public List<MesQcOqcLineDO> getOqcLineListByOqcId(Long oqcId) {
        return oqcLineMapper.selectListByOqcId(oqcId);
    }

    @Override
    public void deleteByOqcId(Long oqcId) {
        oqcLineMapper.deleteByOqcId(oqcId);
    }

}
