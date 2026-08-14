package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changetemplate.HrmSalaryChangeTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryChangeTemplateDO;

import jakarta.validation.Valid;

import java.util.List;

/**
 * HRM 调薪模板 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalaryChangeTemplateService {

    /**
     * 创建调薪模板
     *
     * @param createReqVO 调薪模板信息
     * @return 调薪模板编号
     */
    Long createSalaryChangeTemplate(@Valid HrmSalaryChangeTemplateSaveReqVO createReqVO);

    /**
     * 更新调薪模板
     *
     * @param updateReqVO 调薪模板信息
     */
    void updateSalaryChangeTemplate(@Valid HrmSalaryChangeTemplateSaveReqVO updateReqVO);

    /**
     * 删除调薪模板
     *
     * @param id 调薪模板编号
     */
    void deleteSalaryChangeTemplate(Long id);

    /**
     * 获得调薪模板
     *
     * @param id 调薪模板编号
     * @return 调薪模板
     */
    HrmSalaryChangeTemplateDO getSalaryChangeTemplate(Long id);

    /**
     * 获得调薪模板列表
     *
     * @return 调薪模板列表
     */
    List<HrmSalaryChangeTemplateDO> getSalaryChangeTemplateList();

}
