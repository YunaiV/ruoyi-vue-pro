package cn.iocoder.yudao.module.mes.service.dv.checkplan;

import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.subject.MesDvCheckPlanSubjectSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanSubjectDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 点检保养方案项目 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvCheckPlanSubjectService {

    /**
     * 创建方案项目关联
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCheckPlanSubject(@Valid MesDvCheckPlanSubjectSaveReqVO createReqVO);

    /**
     * 删除方案项目关联
     *
     * @param id 编号
     */
    void deleteCheckPlanSubject(Long id);

    /**
     * 获得指定方案的项目列表
     *
     * @param planId 方案编号
     * @return 项目关联列表
     */
    List<MesDvCheckPlanSubjectDO> getCheckPlanSubjectListByPlanId(Long planId);

    /**
     * 获得指定方案的项目数量
     *
     * @param planId 方案编号
     * @return 项目数量
     */
    Long getCheckPlanSubjectCountByPlanId(Long planId);

    /**
     * 根据方案编号删除所有项目关联
     *
     * @param planId 方案编号
     */
    void deleteByPlanId(Long planId);

    /**
     * 获得指定项目的方案项目关联数量
     *
     * @param subjectId 项目编号
     * @return 项目关联数量
     */
    Long getCheckPlanSubjectCountBySubjectId(Long subjectId);

}
