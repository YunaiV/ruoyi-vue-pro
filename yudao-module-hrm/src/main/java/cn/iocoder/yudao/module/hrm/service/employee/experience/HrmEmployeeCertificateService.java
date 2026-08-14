package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.certificate.HrmEmployeeCertificateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeCertificateDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * HRM 员工证书 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeCertificateService {

    /**
     * 创建员工证书
     *
     * @param reqVO 证书信息
     * @return 证书编号
     */
    Long createCertificate(@Valid HrmEmployeeCertificateSaveReqVO reqVO);

    /**
     * 更新员工证书
     *
     * @param reqVO 证书信息
     */
    void updateCertificate(@Valid HrmEmployeeCertificateSaveReqVO reqVO);

    /**
     * 删除员工证书
     *
     * @param id 证书编号
     */
    void deleteCertificate(Long id);

    /**
     * 校验员工证书是否存在
     *
     * @param id 证书编号
     * @return 员工证书
     */
    HrmEmployeeCertificateDO validateCertificateExists(Long id);

    /**
     * 获得员工证书列表
     *
     * @param employeeId 员工编号
     * @return 证书列表
     */
    List<HrmEmployeeCertificateDO> getCertificateListByEmployeeId(Long employeeId);

}
