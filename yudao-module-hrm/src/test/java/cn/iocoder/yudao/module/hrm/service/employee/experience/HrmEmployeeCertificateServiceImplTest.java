package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.certificate.HrmEmployeeCertificateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeCertificateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience.HrmEmployeeCertificateMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CERTIFICATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * {@link HrmEmployeeCertificateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeCertificateServiceImpl.class)
public class HrmEmployeeCertificateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeCertificateServiceImpl certificateService;

    @Resource
    private HrmEmployeeCertificateMapper certificateMapper;

    @MockBean
    private HrmEmployeeService employeeService;

    @Test
    public void testCreateCertificate_success() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeCertificateSaveReqVO reqVO = randomPojo(
                HrmEmployeeCertificateSaveReqVO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(10)
                        .setStartTime(LocalDateTime.of(2025, 1, 1, 9, 30))
                        .setEndTime(LocalDateTime.of(2027, 1, 1, 18, 0))
                        .setIssuingTime(LocalDateTime.of(2024, 12, 20, 15, 30)));

        // 调用
        Long id = certificateService.createCertificate(reqVO);

        // 断言
        assertNotNull(id);
        HrmEmployeeCertificateDO certificate = certificateMapper.selectById(id);
        assertPojoEquals(reqVO, certificate, "id", "startTime", "endTime", "issuingTime");
        assertEquals(reqVO.getStartTime().toLocalDate().atStartOfDay(), certificate.getStartTime());
        assertEquals(reqVO.getEndTime().toLocalDate().atStartOfDay(), certificate.getEndTime());
        assertEquals(reqVO.getIssuingTime().toLocalDate().atStartOfDay(), certificate.getIssuingTime());
        verify(employeeService).validateEmployeeExists(employeeId);
    }

    @Test
    public void testUpdateCertificate_success() {
        // mock 数据
        HrmEmployeeCertificateDO dbRecord = randomPojo(HrmEmployeeCertificateDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(20).setDeleted(false));
        certificateMapper.insert(dbRecord);
        // 准备参数
        HrmEmployeeCertificateSaveReqVO reqVO = randomPojo(
                HrmEmployeeCertificateSaveReqVO.class,
                o -> o.setId(dbRecord.getId()).setEmployeeId(dbRecord.getEmployeeId()).setSort(10)
                        .setStartTime(LocalDateTime.of(2026, 1, 1, 9, 30))
                        .setEndTime(LocalDateTime.of(2028, 1, 1, 18, 0))
                        .setIssuingTime(LocalDateTime.of(2025, 12, 20, 15, 30)));

        // 调用
        certificateService.updateCertificate(reqVO);

        // 断言
        HrmEmployeeCertificateDO certificate = certificateMapper.selectById(dbRecord.getId());
        assertPojoEquals(reqVO, certificate, "employeeId", "startTime", "endTime", "issuingTime");
        assertEquals(dbRecord.getEmployeeId(), certificate.getEmployeeId());
        assertEquals(reqVO.getStartTime().toLocalDate().atStartOfDay(), certificate.getStartTime());
        assertEquals(reqVO.getEndTime().toLocalDate().atStartOfDay(), certificate.getEndTime());
        assertEquals(reqVO.getIssuingTime().toLocalDate().atStartOfDay(), certificate.getIssuingTime());
    }

    @Test
    public void testDeleteCertificate_success() {
        // mock 数据
        HrmEmployeeCertificateDO dbRecord = randomPojo(HrmEmployeeCertificateDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(20).setDeleted(false));
        certificateMapper.insert(dbRecord);
        // 准备参数
        Long id = dbRecord.getId();

        // 调用
        certificateService.deleteCertificate(id);

        // 断言
        assertNull(certificateMapper.selectById(id));
    }

    @Test
    public void testDeleteCertificate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> certificateService.deleteCertificate(id), EMPLOYEE_CERTIFICATE_NOT_EXISTS);
    }

    @Test
    public void testGetCertificateListByEmployeeId() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeCertificateDO firstRecord = randomPojo(HrmEmployeeCertificateDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(20).setDeleted(false));
        certificateMapper.insert(firstRecord);
        HrmEmployeeCertificateDO secondRecord = randomPojo(HrmEmployeeCertificateDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(10).setDeleted(false));
        certificateMapper.insert(secondRecord);
        certificateMapper.insert(randomPojo(HrmEmployeeCertificateDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(1).setDeleted(false)));

        // 调用
        List<HrmEmployeeCertificateDO> result = certificateService.getCertificateListByEmployeeId(employeeId);
        // 断言
        assertEquals(2, result.size());
        assertEquals(secondRecord.getId(), result.get(0).getId());
        assertEquals(firstRecord.getId(), result.get(1).getId());
    }

}
