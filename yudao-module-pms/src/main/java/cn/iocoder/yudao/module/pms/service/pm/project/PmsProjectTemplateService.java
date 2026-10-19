package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template.PmsProjectTemplatePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template.PmsProjectTemplateSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectTemplateDO;

/**
 * PMS 项目模板 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsProjectTemplateService {

    /**
     * 创建项目模板
     *
     * @param createReqVO 创建信息
     * @return 模板编号
     */
    Long createProjectTemplate(PmsProjectTemplateSaveReqVO createReqVO);

    /**
     * 更新项目模板
     *
     * @param updateReqVO 更新信息
     */
    void updateProjectTemplate(PmsProjectTemplateSaveReqVO updateReqVO);

    /**
     * 删除项目模板
     *
     * @param id 模板编号
     */
    void deleteProjectTemplate(Long id);

    /**
     * 获得项目模板
     *
     * @param id 模板编号
     * @return 项目模板
     */
    PmsProjectTemplateDO getProjectTemplate(Long id);

    /**
     * 获得项目模板分页
     *
     * @param pageReqVO 分页查询
     * @return 项目模板分页
     */
    PageResult<PmsProjectTemplateDO> getProjectTemplatePage(PmsProjectTemplatePageReqVO pageReqVO);

}
