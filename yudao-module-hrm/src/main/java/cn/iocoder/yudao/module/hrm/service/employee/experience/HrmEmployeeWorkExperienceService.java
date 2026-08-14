package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.workexperience.HrmEmployeeWorkExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeWorkExperienceDO;
import javax.validation.Valid;

import java.util.List;

/**
 * HRM 员工工作经历 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeWorkExperienceService {

    /**
     * 创建员工工作经历
     *
     * @param reqVO 工作经历信息
     * @return 工作经历编号
     */
    Long createWorkExperience(@Valid HrmEmployeeWorkExperienceSaveReqVO reqVO);

    /**
     * 更新员工工作经历
     *
     * @param reqVO 工作经历信息
     */
    void updateWorkExperience(@Valid HrmEmployeeWorkExperienceSaveReqVO reqVO);

    /**
     * 删除员工工作经历
     *
     * @param id 工作经历编号
     */
    void deleteWorkExperience(Long id);

    /**
     * 校验员工工作经历是否存在
     *
     * @param id 工作经历编号
     * @return 工作经历
     */
    HrmEmployeeWorkExperienceDO validateWorkExperienceExists(Long id);

    /**
     * 获得员工工作经历列表
     *
     * @param employeeId 员工编号
     * @return 工作经历列表
     */
    List<HrmEmployeeWorkExperienceDO> getWorkExperienceListByEmployeeId(Long employeeId);

}
