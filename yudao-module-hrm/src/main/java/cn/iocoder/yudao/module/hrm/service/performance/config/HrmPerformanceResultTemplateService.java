package cn.iocoder.yudao.module.hrm.service.performance.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO;
import javax.validation.Valid;

import java.util.List;

/**
 * HRM 绩效结果模板 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmPerformanceResultTemplateService {

    /**
     * 创建绩效结果模板
     *
     * @param createReqVO 创建信息
     * @return 模板编号
     */
    Long createPerformanceResultTemplate(@Valid HrmPerformanceResultTemplateSaveReqVO createReqVO);

    /**
     * 更新绩效结果模板
     *
     * @param updateReqVO 更新信息
     */
    void updatePerformanceResultTemplate(@Valid HrmPerformanceResultTemplateSaveReqVO updateReqVO);

    /**
     * 删除绩效结果模板
     *
     * @param id 模板编号
     */
    void deletePerformanceResultTemplate(Long id);

    /**
     * 批量删除绩效结果模板
     *
     * @param ids 模板编号列表
     */
    void deletePerformanceResultTemplateList(List<Long> ids);

    /**
     * 获得绩效结果模板
     *
     * @param id 模板编号
     * @return 绩效结果模板
     */
    HrmPerformanceResultTemplateDO getPerformanceResultTemplate(Long id);

    /**
     * 获得绩效结果模板分页
     *
     * @param pageReqVO 分页查询
     * @return 绩效结果模板分页
     */
    PageResult<HrmPerformanceResultTemplateDO> getPerformanceResultTemplatePage(
            HrmPerformanceResultTemplatePageReqVO pageReqVO);

    /**
     * 获得绩效结果模板列表
     *
     * @param status 状态
     * @return 绩效结果模板列表
     */
    List<HrmPerformanceResultTemplateDO> getPerformanceResultTemplateList(Integer status);

    /**
     * 校验绩效结果模板存在
     *
     * @param id 模板编号
     * @return 绩效结果模板
     */
    HrmPerformanceResultTemplateDO validatePerformanceResultTemplateExists(Long id);

    /**
     * 校验绩效结果模板已启用
     *
     * @param id 模板编号
     * @return 绩效结果模板
     */
    HrmPerformanceResultTemplateDO validatePerformanceResultTemplateEnabled(Long id);

}
