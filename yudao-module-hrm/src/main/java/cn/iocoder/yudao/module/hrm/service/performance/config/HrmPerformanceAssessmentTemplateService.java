package cn.iocoder.yudao.module.hrm.service.performance.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * HRM 绩效考核模板 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmPerformanceAssessmentTemplateService {

    /**
     * 创建绩效考核模板
     *
     * @param createReqVO 创建信息
     * @return 模板编号
     */
    Long createPerformanceAssessmentTemplate(@Valid HrmPerformanceAssessmentTemplateSaveReqVO createReqVO);

    /**
     * 更新绩效考核模板
     *
     * @param updateReqVO 更新信息
     */
    void updatePerformanceAssessmentTemplate(@Valid HrmPerformanceAssessmentTemplateSaveReqVO updateReqVO);

    /**
     * 删除绩效考核模板
     *
     * @param id 模板编号
     */
    void deletePerformanceAssessmentTemplate(Long id);

    /**
     * 批量删除绩效考核模板
     *
     * @param ids 模板编号列表
     */
    void deletePerformanceAssessmentTemplateList(List<Long> ids);

    /**
     * 获得绩效考核模板
     *
     * @param id 模板编号
     * @return 绩效考核模板
     */
    HrmPerformanceAssessmentTemplateDO getPerformanceAssessmentTemplate(Long id);

    /**
     * 获得绩效考核模板分页
     *
     * @param pageReqVO 分页查询
     * @return 绩效考核模板分页
     */
    PageResult<HrmPerformanceAssessmentTemplateDO> getPerformanceAssessmentTemplatePage(
            HrmPerformanceAssessmentTemplatePageReqVO pageReqVO);

    /**
     * 获得指定状态的绩效考核模板列表
     *
     * @param status 状态
     * @return 绩效考核模板列表
     */
    List<HrmPerformanceAssessmentTemplateDO> getPerformanceAssessmentTemplateListByStatus(Integer status);

    /**
     * 校验绩效考核模板存在
     *
     * @param id 模板编号
     * @return 绩效考核模板
     */
    HrmPerformanceAssessmentTemplateDO validatePerformanceAssessmentTemplateExists(Long id);

    /**
     * 校验绩效考核模板有效
     *
     * @param id 模板编号
     * @return 绩效考核模板
     */
    HrmPerformanceAssessmentTemplateDO validatePerformanceAssessmentTemplateEnabled(Long id);

}
