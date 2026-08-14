package cn.iocoder.yudao.module.hrm.service.performance.config;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.config.HrmPerformanceResultTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_RESULT_TEMPLATE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_RESULT_TEMPLATE_NOT_EXISTS;

/**
 * HRM 绩效结果模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmPerformanceResultTemplateServiceImpl implements HrmPerformanceResultTemplateService {

    @Resource
    private HrmPerformanceResultTemplateMapper resultTemplateMapper;

    @Resource
    @Lazy
    private HrmPerformancePlanService performancePlanService;

    @Override
    public Long createPerformanceResultTemplate(HrmPerformanceResultTemplateSaveReqVO createReqVO) {
        // 1. 校验结果模板名称唯一
        validateResultTemplateNameUnique(null, createReqVO.getName());

        // 2. 创建结果模板
        HrmPerformanceResultTemplateDO template = BeanUtils.toBean(
                createReqVO, HrmPerformanceResultTemplateDO.class)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePerformanceResultTemplate(HrmPerformanceResultTemplateSaveReqVO updateReqVO) {
        // 1.1 校验结果模板存在
        HrmPerformanceResultTemplateDO oldTemplate =
                validatePerformanceResultTemplateEnabled(updateReqVO.getId());
        // 1.2 校验结果模板名称唯一
        validateResultTemplateNameUnique(updateReqVO.getId(), updateReqVO.getName());

        // 2. 停用旧模板，避免修改已被绩效计划引用的模板版本
        resultTemplateMapper.updateById(new HrmPerformanceResultTemplateDO()
                .setId(oldTemplate.getId()).setStatus(CommonStatusEnum.DISABLE.getStatus()));

        // 3. 创建新版本，既有绩效计划继续引用旧模板编号和结果配置快照
        HrmPerformanceResultTemplateDO template = BeanUtils.toBean(
                updateReqVO, HrmPerformanceResultTemplateDO.class)
                .setId(null).setStatus(CommonStatusEnum.ENABLE.getStatus());
        template.setCreator(oldTemplate.getCreator());
        template.setCreateTime(oldTemplate.getCreateTime());
        resultTemplateMapper.insert(template);
    }

    @Override
    public void deletePerformanceResultTemplate(Long id) {
        // 1.1 校验结果模板存在
        validatePerformanceResultTemplateExists(id);
        // 1.2 校验结果模板未被绩效计划使用
        if (performancePlanService.getPerformancePlanCountByResultTemplateId(id) > 0) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 2. 删除结果模板
        resultTemplateMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerformanceResultTemplateList(List<Long> ids) {
        // 1.1 校验结果模板全部存在
        List<HrmPerformanceResultTemplateDO> templates = resultTemplateMapper.selectByIds(ids);
        if (templates.size() != new HashSet<>(ids).size()) {
            throw exception(PERFORMANCE_RESULT_TEMPLATE_NOT_EXISTS);
        }
        // 1.2 校验结果模板未被绩效计划使用
        for (HrmPerformanceResultTemplateDO template : templates) {
            if (performancePlanService.getPerformancePlanCountByResultTemplateId(template.getId()) > 0) {
                throw exception(PERFORMANCE_DATA_ILLEGAL);
            }
        }

        // 2. 批量删除结果模板
        resultTemplateMapper.deleteByIds(ids);
    }

    @Override
    public HrmPerformanceResultTemplateDO getPerformanceResultTemplate(Long id) {
        return resultTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<HrmPerformanceResultTemplateDO> getPerformanceResultTemplatePage(
            HrmPerformanceResultTemplatePageReqVO pageReqVO) {
        return resultTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HrmPerformanceResultTemplateDO> getPerformanceResultTemplateList(Integer status) {
        return resultTemplateMapper.selectListByStatus(status);
    }

    @Override
    public HrmPerformanceResultTemplateDO validatePerformanceResultTemplateExists(Long id) {
        HrmPerformanceResultTemplateDO template = resultTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(PERFORMANCE_RESULT_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @Override
    public HrmPerformanceResultTemplateDO validatePerformanceResultTemplateEnabled(Long id) {
        HrmPerformanceResultTemplateDO template = validatePerformanceResultTemplateExists(id);
        if (ObjectUtil.notEqual(template.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(PERFORMANCE_RESULT_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private void validateResultTemplateNameUnique(Long id, String name) {
        HrmPerformanceResultTemplateDO template = resultTemplateMapper.selectByName(name);
        if (template != null && ObjectUtil.notEqual(template.getId(), id)) {
            throw exception(PERFORMANCE_RESULT_TEMPLATE_NAME_DUPLICATE);
        }
    }

}
