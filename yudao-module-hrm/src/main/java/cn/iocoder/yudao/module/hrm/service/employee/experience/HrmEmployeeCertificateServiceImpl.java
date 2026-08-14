package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.certificate.HrmEmployeeCertificateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeCertificateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience.HrmEmployeeCertificateMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayBeginTime;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CERTIFICATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_RESOURCE_BELONG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * HRM 员工证书 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeCertificateServiceImpl implements HrmEmployeeCertificateService {

    @Resource
    private HrmEmployeeCertificateMapper certificateMapper;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CERTIFICATE_CREATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_CERTIFICATE_CREATE_SUCCESS)
    public Long createCertificate(HrmEmployeeCertificateSaveReqVO reqVO) {
        // 1. 校验员工
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());
        // 2. 创建证书
        HrmEmployeeCertificateDO certificate = BeanUtils.toBean(reqVO, HrmEmployeeCertificateDO.class)
                .setStartTime(getDayBeginTime(reqVO.getStartTime()))
                .setEndTime(getDayBeginTime(reqVO.getEndTime()))
                .setIssuingTime(getDayBeginTime(reqVO.getIssuingTime()));
        certificateMapper.insert(certificate);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("certificate", certificate);
        return certificate.getId();
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CERTIFICATE_UPDATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_CERTIFICATE_UPDATE_SUCCESS)
    public void updateCertificate(HrmEmployeeCertificateSaveReqVO reqVO) {
        // 1. 校验员工和证书
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());
        HrmEmployeeCertificateDO certificate = validateCertificateExists(reqVO.getId());
        if (notEqual(certificate.getEmployeeId(), reqVO.getEmployeeId())) {
            throw exception(EMPLOYEE_RESOURCE_BELONG_INVALID, "证书");
        }

        // 2. 更新证书
        certificateMapper.updateById(BeanUtils.toBean(reqVO, HrmEmployeeCertificateDO.class)
                .setEmployeeId(null)
                .setStartTime(getDayBeginTime(reqVO.getStartTime()))
                .setEndTime(getDayBeginTime(reqVO.getEndTime()))
                .setIssuingTime(getDayBeginTime(reqVO.getIssuingTime())));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(certificate, HrmEmployeeCertificateSaveReqVO.class));
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CERTIFICATE_DELETE_SUB_TYPE,
            bizNo = "{{#certificate.employeeId}}", success = HRM_EMPLOYEE_CERTIFICATE_DELETE_SUCCESS)
    public void deleteCertificate(Long id) {
        // 1. 校验员工证书存在
        HrmEmployeeCertificateDO certificate = validateCertificateExists(id);

        // 2. 删除员工证书
        certificateMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("certificate", certificate);
    }

    @Override
    public HrmEmployeeCertificateDO validateCertificateExists(Long id) {
        HrmEmployeeCertificateDO certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw exception(EMPLOYEE_CERTIFICATE_NOT_EXISTS);
        }
        return certificate;
    }

    @Override
    public List<HrmEmployeeCertificateDO> getCertificateListByEmployeeId(Long employeeId) {
        return certificateMapper.selectListByEmployeeId(employeeId);
    }

}
