package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contact.HrmEmployeeContactSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeContactDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * HRM 员工联系人 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeContactService {

    /**
     * 创建员工联系人
     *
     * @param reqVO 联系人信息
     * @return 联系人编号
     */
    Long createContact(@Valid HrmEmployeeContactSaveReqVO reqVO);

    /**
     * 更新员工联系人
     *
     * @param reqVO 联系人信息
     */
    void updateContact(@Valid HrmEmployeeContactSaveReqVO reqVO);

    /**
     * 删除员工联系人
     *
     * @param id 联系人编号
     */
    void deleteContact(Long id);

    /**
     * 校验员工联系人是否存在
     *
     * @param id 联系人编号
     * @return 联系人
     */
    HrmEmployeeContactDO validateContactExists(Long id);

    /**
     * 获得员工联系人列表
     *
     * @param employeeId 员工编号
     * @return 联系人列表
     */
    List<HrmEmployeeContactDO> getContactListByEmployeeId(Long employeeId);

}
