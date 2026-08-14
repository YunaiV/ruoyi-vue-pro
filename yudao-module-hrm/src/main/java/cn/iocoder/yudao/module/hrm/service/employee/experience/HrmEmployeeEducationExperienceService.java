package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.educationexperience.HrmEmployeeEducationExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeEducationExperienceDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * HRM 员工教育经历 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeEducationExperienceService {

    /**
     * 创建员工教育经历
     *
     * @param reqVO 教育经历信息
     * @return 教育经历编号
     */
    Long createEducationExperience(@Valid HrmEmployeeEducationExperienceSaveReqVO reqVO);

    /**
     * 更新员工教育经历
     *
     * @param reqVO 教育经历信息
     */
    void updateEducationExperience(@Valid HrmEmployeeEducationExperienceSaveReqVO reqVO);

    /**
     * 删除员工教育经历
     *
     * @param id 教育经历编号
     */
    void deleteEducationExperience(Long id);

    /**
     * 校验员工教育经历是否存在
     *
     * @param id 教育经历编号
     * @return 教育经历
     */
    HrmEmployeeEducationExperienceDO validateEducationExperienceExists(Long id);

    /**
     * 获得员工教育经历列表
     *
     * @param employeeId 员工编号
     * @return 教育经历列表
     */
    List<HrmEmployeeEducationExperienceDO> getEducationExperienceListByEmployeeId(Long employeeId);

}
