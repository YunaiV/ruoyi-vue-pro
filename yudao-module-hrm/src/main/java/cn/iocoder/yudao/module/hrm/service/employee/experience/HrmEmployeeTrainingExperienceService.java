package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.trainingexperience.HrmEmployeeTrainingExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeTrainingExperienceDO;
import javax.validation.Valid;

import java.util.List;

/**
 * HRM 员工培训经历 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeTrainingExperienceService {

    /**
     * 创建员工培训经历
     *
     * @param reqVO 培训经历信息
     * @return 培训经历编号
     */
    Long createTrainingExperience(@Valid HrmEmployeeTrainingExperienceSaveReqVO reqVO);

    /**
     * 更新员工培训经历
     *
     * @param reqVO 培训经历信息
     */
    void updateTrainingExperience(@Valid HrmEmployeeTrainingExperienceSaveReqVO reqVO);

    /**
     * 删除员工培训经历
     *
     * @param id 培训经历编号
     */
    void deleteTrainingExperience(Long id);

    /**
     * 校验员工培训经历是否存在
     *
     * @param id 培训经历编号
     * @return 培训经历
     */
    HrmEmployeeTrainingExperienceDO validateTrainingExperienceExists(Long id);

    /**
     * 获得员工培训经历列表
     *
     * @param employeeId 员工编号
     * @return 培训经历列表
     */
    List<HrmEmployeeTrainingExperienceDO> getTrainingExperienceListByEmployeeId(Long employeeId);

}
