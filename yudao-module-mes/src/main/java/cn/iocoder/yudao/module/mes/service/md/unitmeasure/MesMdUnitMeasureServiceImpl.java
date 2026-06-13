package cn.iocoder.yudao.module.mes.service.md.unitmeasure;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasurePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasureSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.unitmeasure.MesMdUnitMeasureMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.pro.task.MesProTaskIssueService;
import cn.iocoder.yudao.module.mes.service.qc.ipqc.MesQcIpqcLineService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcLineService;
import cn.iocoder.yudao.module.mes.service.qc.oqc.MesQcOqcLineService;
import cn.iocoder.yudao.module.mes.service.qc.rqc.MesQcRqcLineService;
import cn.iocoder.yudao.module.mes.service.qc.template.MesQcTemplateIndicatorService;
import org.springframework.context.annotation.Lazy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 计量单位 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdUnitMeasureServiceImpl implements MesMdUnitMeasureService {

    @Resource
    private MesMdUnitMeasureMapper unitMeasureMapper;

    @Resource
    @Lazy
    private MesMdItemService itemService;
    @Resource
    @Lazy
    private MesProTaskIssueService taskIssueService;
    @Resource
    @Lazy
    private MesQcTemplateIndicatorService templateIndicatorService;
    @Resource
    @Lazy
    private MesQcIqcLineService iqcLineService;
    @Resource
    @Lazy
    private MesQcOqcLineService oqcLineService;
    @Resource
    @Lazy
    private MesQcIpqcLineService ipqcLineService;
    @Resource
    @Lazy
    private MesQcRqcLineService rqcLineService;

    @Override
    public Long createUnitMeasure(MesMdUnitMeasureSaveReqVO createReqVO) {
        // 校验编码唯一
        validateUnitMeasureCodeUnique(null, createReqVO.getCode());

        // 插入
        MesMdUnitMeasureDO unitMeasure = BeanUtils.toBean(createReqVO, MesMdUnitMeasureDO.class);
        unitMeasureMapper.insert(unitMeasure);
        return unitMeasure.getId();
    }

    @Override
    public void updateUnitMeasure(MesMdUnitMeasureSaveReqVO updateReqVO) {
        // 校验存在
        validateUnitMeasureExists(updateReqVO.getId());
        // 校验编码唯一
        validateUnitMeasureCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        MesMdUnitMeasureDO updateObj = BeanUtils.toBean(updateReqVO, MesMdUnitMeasureDO.class);
        unitMeasureMapper.updateById(updateObj);
    }

    @Override
    public void deleteUnitMeasure(Long id) {
        // 校验存在
        validateUnitMeasureExists(id);
        // 校验是否存在以此为主单位的辅单位
        if (unitMeasureMapper.selectCountByPrimaryId(id) > 0) {
            throw exception(MD_UNIT_MEASURE_HAS_SECONDARY);
        }
        // 校验是否被物料引用
        if (itemService.getItemCountByUnitMeasureId(id) > 0) {
            throw exception(MD_UNIT_MEASURE_HAS_ITEM);
        }
        // 校验是否被生产任务投料引用
        if (taskIssueService.getTaskIssueCountByUnitMeasureId(id) > 0) {
            throw exception(MD_UNIT_MEASURE_HAS_TASK_ISSUE);
        }
        // 校验是否被质检方案指标项引用
        if (templateIndicatorService.getTemplateIndicatorCountByUnitMeasureId(id) > 0) {
            throw exception(MD_UNIT_MEASURE_HAS_QC_TEMPLATE_INDICATOR);
        }
        // 校验是否被质检单据行引用（IQC/OQC/IPQC/RQC）
        if (iqcLineService.getIqcLineCountByUnitMeasureId(id) > 0
                || oqcLineService.getOqcLineCountByUnitMeasureId(id) > 0
                || ipqcLineService.getIpqcLineCountByUnitMeasureId(id) > 0
                || rqcLineService.getRqcLineCountByUnitMeasureId(id) > 0) {
            throw exception(MD_UNIT_MEASURE_HAS_QC_LINE);
        }

        // 删除
        unitMeasureMapper.deleteById(id);
    }

    private void validateUnitMeasureExists(Long id) {
        if (unitMeasureMapper.selectById(id) == null) {
            throw exception(MD_UNIT_MEASURE_NOT_EXISTS);
        }
    }

    private void validateUnitMeasureCodeUnique(Long id, String code) {
        MesMdUnitMeasureDO unitMeasure = unitMeasureMapper.selectByCode(code);
        if (unitMeasure == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的记录
        if (id == null) {
            throw exception(MD_UNIT_MEASURE_CODE_DUPLICATE);
        }
        if (!Objects.equals(unitMeasure.getId(), id)) {
            throw exception(MD_UNIT_MEASURE_CODE_DUPLICATE);
        }
    }

    @Override
    public MesMdUnitMeasureDO getUnitMeasure(Long id) {
        return unitMeasureMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdUnitMeasureDO> getUnitMeasurePage(MesMdUnitMeasurePageReqVO pageReqVO) {
        return unitMeasureMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesMdUnitMeasureDO> getUnitMeasureListByStatus(Integer status) {
        return unitMeasureMapper.selectListByStatus(status);
    }

    @Override
    public List<MesMdUnitMeasureDO> getUnitMeasureList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return unitMeasureMapper.selectByIds(ids);
    }

    @Override
    public MesMdUnitMeasureDO getUnitMeasureByCode(String code) {
        return unitMeasureMapper.selectByCode(code);
    }

}
