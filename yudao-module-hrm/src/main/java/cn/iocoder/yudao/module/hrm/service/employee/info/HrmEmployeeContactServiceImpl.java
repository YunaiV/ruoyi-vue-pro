package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contact.HrmEmployeeContactSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeContactDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeeContactMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CONTACT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_RESOURCE_BELONG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * HRM 员工联系人 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeContactServiceImpl implements HrmEmployeeContactService {

    @Resource
    private HrmEmployeeContactMapper contactMapper;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CONTACT_CREATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_CONTACT_CREATE_SUCCESS)
    public Long createContact(HrmEmployeeContactSaveReqVO reqVO) {
        // 1. 校验员工
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());

        // 2. 创建联系人
        HrmEmployeeContactDO contact = BeanUtils.toBean(reqVO, HrmEmployeeContactDO.class);
        contactMapper.insert(contact);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("contact", contact);
        return contact.getId();
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CONTACT_UPDATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_CONTACT_UPDATE_SUCCESS)
    public void updateContact(HrmEmployeeContactSaveReqVO reqVO) {
        // 1. 校验员工和联系人
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());
        HrmEmployeeContactDO contact = validateContactExists(reqVO.getId());
        if (notEqual(contact.getEmployeeId(), reqVO.getEmployeeId())) {
            throw exception(EMPLOYEE_RESOURCE_BELONG_INVALID, "联系人");
        }

        // 2. 更新联系人
        contactMapper.updateById(BeanUtils.toBean(reqVO, HrmEmployeeContactDO.class)
                .setEmployeeId(null));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(contact, HrmEmployeeContactSaveReqVO.class));
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CONTACT_DELETE_SUB_TYPE,
            bizNo = "{{#contact.employeeId}}", success = HRM_EMPLOYEE_CONTACT_DELETE_SUCCESS)
    public void deleteContact(Long id) {
        // 1. 校验员工联系人存在
        HrmEmployeeContactDO contact = validateContactExists(id);

        // 2. 删除员工联系人
        contactMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("contact", contact);
    }

    @Override
    public HrmEmployeeContactDO validateContactExists(Long id) {
        HrmEmployeeContactDO contact = contactMapper.selectById(id);
        if (contact == null) {
            throw exception(EMPLOYEE_CONTACT_NOT_EXISTS);
        }
        return contact;
    }

    @Override
    public List<HrmEmployeeContactDO> getContactListByEmployeeId(Long employeeId) {
        return contactMapper.selectListByEmployeeId(employeeId);
    }

}
