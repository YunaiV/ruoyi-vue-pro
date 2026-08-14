package cn.iocoder.yudao.module.hrm.service.performance.config;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.config.HrmPerformanceAssessmentTemplateMapper;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_ASSESSMENT_TEMPLATE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_ASSESSMENT_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;

/**
 * HRM 绩效考核模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmPerformanceAssessmentTemplateServiceImpl implements HrmPerformanceAssessmentTemplateService {

    @Resource
    private HrmPerformanceAssessmentTemplateMapper assessmentTemplateMapper;

    @Resource
    @Lazy
    private HrmPerformancePlanService performancePlanService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPerformanceAssessmentTemplate(HrmPerformanceAssessmentTemplateSaveReqVO createReqVO) {
        // 1. 校验模板名称唯一
        validateAssessmentTemplateNameUnique(null, createReqVO.getName());

        // 2. 创建考核模板
        HrmPerformanceAssessmentTemplateDO template = BeanUtils.toBean(
                createReqVO, HrmPerformanceAssessmentTemplateDO.class)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        normalizeAssessmentTemplate(template);
        assessmentTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePerformanceAssessmentTemplate(HrmPerformanceAssessmentTemplateSaveReqVO updateReqVO) {
        // 1.1 校验模板存在
        HrmPerformanceAssessmentTemplateDO oldTemplate =
                validatePerformanceAssessmentTemplateEnabled(updateReqVO.getId());
        // 1.2 校验模板名称唯一
        validateAssessmentTemplateNameUnique(updateReqVO.getId(), updateReqVO.getName());

        // 2. 停用旧模板，避免修改已被绩效计划引用的模板版本
        assessmentTemplateMapper.updateById(new HrmPerformanceAssessmentTemplateDO()
                .setId(oldTemplate.getId()).setStatus(CommonStatusEnum.DISABLE.getStatus()));

        // 3. 创建新版本，既有绩效计划继续引用旧模板编号
        HrmPerformanceAssessmentTemplateDO template = BeanUtils.toBean(
                updateReqVO, HrmPerformanceAssessmentTemplateDO.class)
                .setId(null).setStatus(CommonStatusEnum.ENABLE.getStatus());
        template.setCreator(oldTemplate.getCreator());
        template.setCreateTime(oldTemplate.getCreateTime());
        normalizeAssessmentTemplate(template);
        assessmentTemplateMapper.insert(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerformanceAssessmentTemplate(Long id) {
        // 1.1 校验模板存在
        validatePerformanceAssessmentTemplateExists(id);
        // 1.2 校验模板未被绩效计划使用
        if (performancePlanService.getPerformancePlanCountByAssessmentTemplateId(id) > 0) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 2. 删除考核模板
        assessmentTemplateMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerformanceAssessmentTemplateList(List<Long> ids) {
        // 1.1 校验模板全部存在
        List<HrmPerformanceAssessmentTemplateDO> templates = assessmentTemplateMapper.selectByIds(ids);
        if (templates.size() != new HashSet<>(ids).size()) {
            throw exception(PERFORMANCE_ASSESSMENT_TEMPLATE_NOT_EXISTS);
        }
        // 1.2 校验模板未被绩效计划使用
        for (HrmPerformanceAssessmentTemplateDO template : templates) {
            if (performancePlanService.getPerformancePlanCountByAssessmentTemplateId(template.getId()) > 0) {
                throw exception(PERFORMANCE_DATA_ILLEGAL);
            }
        }

        // 2. 批量删除考核模板
        assessmentTemplateMapper.deleteByIds(ids);
    }

    @Override
    public HrmPerformanceAssessmentTemplateDO getPerformanceAssessmentTemplate(Long id) {
        return assessmentTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<HrmPerformanceAssessmentTemplateDO> getPerformanceAssessmentTemplatePage(
            HrmPerformanceAssessmentTemplatePageReqVO pageReqVO) {
        return assessmentTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HrmPerformanceAssessmentTemplateDO> getPerformanceAssessmentTemplateListByStatus(Integer status) {
        return assessmentTemplateMapper.selectListByStatus(status);
    }

    @Override
    public HrmPerformanceAssessmentTemplateDO validatePerformanceAssessmentTemplateExists(Long id) {
        HrmPerformanceAssessmentTemplateDO template = assessmentTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(PERFORMANCE_ASSESSMENT_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @Override
    public HrmPerformanceAssessmentTemplateDO validatePerformanceAssessmentTemplateEnabled(Long id) {
        HrmPerformanceAssessmentTemplateDO template = validatePerformanceAssessmentTemplateExists(id);
        if (ObjectUtil.notEqual(template.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(PERFORMANCE_ASSESSMENT_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private void validateAssessmentTemplateNameUnique(Long id, String name) {
        HrmPerformanceAssessmentTemplateDO template = assessmentTemplateMapper.selectByName(name);
        if (template != null && ObjectUtil.notEqual(template.getId(), id)) {
            throw exception(PERFORMANCE_ASSESSMENT_TEMPLATE_NAME_DUPLICATE);
        }
    }

    /**
     * 规范化考核模板，并重新计算维度、指标数量
     *
     * @param template 考核模板
     */
    private void normalizeAssessmentTemplate(HrmPerformanceAssessmentTemplateDO template) {
        template.setName(template.getName().trim()).setIllustrate(StringUtils.trimToNull(template.getIllustrate()));
        int quotaCount = 0;
        for (HrmPerformanceAssessmentTemplateDO.Dimension dimension : template.getDimensions()) {
            dimension.setName(dimension.getName().trim()).setRemark(StringUtils.trimToNull(dimension.getRemark()))
                    .setAllowEdit(Boolean.TRUE.equals(dimension.getAllowEdit()));
            for (HrmPerformanceAssessmentTemplateDO.Quota quota : dimension.getQuotas()) {
                quota.setName(quota.getName().trim())
                        .setIllustrate(StringUtils.trimToNull(quota.getIllustrate()))
                        .setStandard(StringUtils.trimToNull(quota.getStandard()));
                quotaCount++;
            }
        }
        template.setDimensionCount(template.getDimensions().size()).setQuotaCount(quotaCount);
    }

}
