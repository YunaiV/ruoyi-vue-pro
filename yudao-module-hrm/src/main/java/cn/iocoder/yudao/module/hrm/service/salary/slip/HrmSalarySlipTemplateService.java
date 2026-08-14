package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template.HrmSalarySlipTemplateOptionVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template.HrmSalarySlipTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * HRM 工资条模板 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalarySlipTemplateService {

    /**
     * 创建工资条模板
     *
     * @param createReqVO 工资条模板信息
     * @return 工资条模板编号
     */
    Long createSalarySlipTemplate(@Valid HrmSalarySlipTemplateSaveReqVO createReqVO);

    /**
     * 更新工资条模板
     *
     * @param updateReqVO 工资条模板信息
     */
    void updateSalarySlipTemplate(@Valid HrmSalarySlipTemplateSaveReqVO updateReqVO);

    /**
     * 删除工资条模板
     *
     * @param id 工资条模板编号
     */
    void deleteSalarySlipTemplate(Long id);

    /**
     * 获得工资条模板详情
     *
     * @param id 工资条模板编号
     * @return 工资条模板详情
     */
    HrmSalarySlipTemplateDO getSalarySlipTemplate(Long id);

    /**
     * 获得工资条模板列表
     *
     * @return 工资条模板列表
     */
    List<HrmSalarySlipTemplateDO> getSalarySlipTemplateList();

    /**
     * 构建工资条模板快照
     *
     * @param hideEmpty 是否隐藏空值
     * @param options 工资条模板项
     * @return 工资条模板快照
     */
    HrmSalarySlipTemplateDO buildSalarySlipTemplateSnapshot(
            Boolean hideEmpty, List<HrmSalarySlipTemplateOptionVO> options);

}
