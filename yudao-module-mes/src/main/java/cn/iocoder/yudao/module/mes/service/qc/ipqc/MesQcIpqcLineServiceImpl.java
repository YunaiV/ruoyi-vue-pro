package cn.iocoder.yudao.module.mes.service.qc.ipqc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.ipqc.vo.line.MesQcIpqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.ipqc.MesQcIpqcLineMapper;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcDefectLevelEnum;
import cn.iocoder.yudao.module.mes.service.qc.indicator.MesQcIndicatorService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateIndicatorService;
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
 * MES 过程检验单行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcIpqcLineServiceImpl implements MesQcIpqcLineService {

    @Resource
    private MesQcIpqcLineMapper ipqcLineMapper;

    @Resource
    private MesQcIndicatorService indicatorService;
    @Resource
    private MesQcTemplateIndicatorService templateIndicatorService;

    @Override
    public MesQcIpqcLineDO validateIpqcLineExists(Long id) {
        MesQcIpqcLineDO line = ipqcLineMapper.selectById(id);
        if (line == null) {
            throw exception(QC_IPQC_LINE_NOT_EXISTS);
        }
        return line;
    }

    @Override
    public MesQcIpqcLineDO getIpqcLine(Long id) {
        return ipqcLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcIpqcLineDO> getIpqcLinePage(MesQcIpqcLinePageReqVO pageReqVO) {
        return ipqcLineMapper.selectPage(pageReqVO);
    }

    @Override
    public void createLinesFromTemplate(Long ipqcId, Long templateId) {
        List<MesQcTemplateIndicatorDO> templateIndicators = templateIndicatorService.getTemplateIndicatorListByTemplateId(templateId);
        if (CollUtil.isEmpty(templateIndicators)) {
            return;
        }
        Map<Long, MesQcIndicatorDO> indicatorMap = indicatorService.getIndicatorMap(
                convertSet(templateIndicators, MesQcTemplateIndicatorDO::getIndicatorId));
        List<MesQcIpqcLineDO> lines = convertList(templateIndicators, templateIndicator -> {
            MesQcIndicatorDO indicator = indicatorMap.get(templateIndicator.getIndicatorId());
            return new MesQcIpqcLineDO()
                    .setIpqcId(ipqcId).setIndicatorId(templateIndicator.getIndicatorId())
                    .setTool(indicator != null ? indicator.getTool() : null)
                    .setCheckMethod(templateIndicator.getCheckMethod())
                    .setStandardValue(templateIndicator.getStandardValue()).setUnitMeasureId(templateIndicator.getUnitMeasureId())
                    .setMaxThreshold(templateIndicator.getThresholdMax()).setMinThreshold(templateIndicator.getThresholdMin())
                    .setCriticalQuantity(0).setMajorQuantity(0).setMinorQuantity(0);
        });
        ipqcLineMapper.insertBatch(lines);
    }

    @Override
    public void recalculateLineDefectStats(Long ipqcId, List<MesQcDefectRecordDO> records) {
        List<MesQcIpqcLineDO> lines = ipqcLineMapper.selectListByIpqcId(ipqcId);
        if (CollUtil.isEmpty(lines)) {
            return;
        }
        List<MesQcIpqcLineDO> updateLines = new ArrayList<>(lines.size());
        for (MesQcIpqcLineDO line : lines) {
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
            updateLines.add(new MesQcIpqcLineDO().setId(line.getId())
                    .setCriticalQuantity(critical).setMajorQuantity(major).setMinorQuantity(minor));
        }
        for (MesQcIpqcLineDO updateLine : updateLines) {
            ipqcLineMapper.updateById(updateLine);
        }
    }

    @Override
    public List<MesQcIpqcLineDO> getIpqcLineListByIpqcId(Long ipqcId) {
        return ipqcLineMapper.selectListByIpqcId(ipqcId);
    }

    @Override
    public void deleteListByIpqcId(Long ipqcId) {
        ipqcLineMapper.deleteByIpqcId(ipqcId);
    }

    @Override
    public Long getIpqcLineCountByUnitMeasureId(Long unitMeasureId) {
        return ipqcLineMapper.selectCountByUnitMeasureId(unitMeasureId);
    }

}
