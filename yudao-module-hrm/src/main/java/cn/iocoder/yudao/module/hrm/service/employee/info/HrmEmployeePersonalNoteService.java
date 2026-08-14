package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.personalnote.HrmEmployeePersonalNoteCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeePersonalNoteDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 员工个人备忘 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeePersonalNoteService {

    /**
     * 创建员工个人备忘
     *
     * @param employeeId 员工编号
     * @param reqVO 个人备忘信息
     * @return 个人备忘编号
     */
    Long createPersonalNote(Long employeeId, @Valid HrmEmployeePersonalNoteCreateReqVO reqVO);

    /**
     * 删除员工个人备忘
     *
     * @param employeeId 员工编号
     * @param id 个人备忘编号
     */
    void deletePersonalNote(Long employeeId, Long id);

    /**
     * 获得员工指定提醒时间范围内的个人备忘
     *
     * @param employeeId 员工编号
     * @param reminderTimes 提醒时间双闭区间
     * @return 个人备忘列表
     */
    List<HrmEmployeePersonalNoteDO> getPersonalNoteList(
            Long employeeId, LocalDateTime[] reminderTimes);

}
