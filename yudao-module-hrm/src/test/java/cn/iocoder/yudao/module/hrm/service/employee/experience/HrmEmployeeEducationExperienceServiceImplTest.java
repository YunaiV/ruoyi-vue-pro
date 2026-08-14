package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.educationexperience.HrmEmployeeEducationExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeEducationExperienceDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience.HrmEmployeeEducationExperienceMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_EDUCATION_EXPERIENCE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * {@link HrmEmployeeEducationExperienceServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeEducationExperienceServiceImpl.class)
public class HrmEmployeeEducationExperienceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeEducationExperienceServiceImpl educationExperienceService;

    @Resource
    private HrmEmployeeEducationExperienceMapper educationExperienceMapper;

    @MockitoBean
    private HrmEmployeeService employeeService;

    @Test
    public void testCreateEducationExperience_success() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeEducationExperienceSaveReqVO reqVO = randomPojo(
                HrmEmployeeEducationExperienceSaveReqVO.class,
                o -> {
                    o.setId(null).setEmployeeId(employeeId).setSort(10);
                    o.setEducation(1).setTeachingMethods(1).setFirstDegree(true)
                            .setAdmissionTime(LocalDateTime.of(2020, 9, 1, 9, 30))
                            .setGraduationTime(LocalDateTime.of(2024, 6, 30, 18, 0));
                });

        // 调用
        Long id = educationExperienceService.createEducationExperience(reqVO);

        // 断言
        assertNotNull(id);
        HrmEmployeeEducationExperienceDO educationExperience = educationExperienceMapper.selectById(id);
        assertPojoEquals(reqVO, educationExperience, "id", "admissionTime", "graduationTime");
        assertEquals(reqVO.getAdmissionTime().toLocalDate().atStartOfDay(), educationExperience.getAdmissionTime());
        assertEquals(reqVO.getGraduationTime().toLocalDate().atStartOfDay(), educationExperience.getGraduationTime());
        verify(employeeService).validateEmployeeExists(employeeId);
    }

    @Test
    public void testUpdateEducationExperience_success() {
        // mock 数据
        HrmEmployeeEducationExperienceDO dbRecord = randomPojo(HrmEmployeeEducationExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setEducation(1)
                        .setTeachingMethods(1).setFirstDegree(false).setSort(20).setDeleted(false));
        educationExperienceMapper.insert(dbRecord);
        // 准备参数
        HrmEmployeeEducationExperienceSaveReqVO reqVO = randomPojo(
                HrmEmployeeEducationExperienceSaveReqVO.class,
                o -> {
                    o.setId(dbRecord.getId()).setEmployeeId(dbRecord.getEmployeeId()).setSort(10);
                    o.setEducation(2).setTeachingMethods(2).setFirstDegree(true)
                            .setAdmissionTime(LocalDateTime.of(2021, 9, 1, 9, 30))
                            .setGraduationTime(LocalDateTime.of(2025, 6, 30, 18, 0));
                });

        // 调用
        educationExperienceService.updateEducationExperience(reqVO);

        // 断言
        HrmEmployeeEducationExperienceDO educationExperience = educationExperienceMapper.selectById(dbRecord.getId());
        assertPojoEquals(reqVO, educationExperience, "employeeId", "admissionTime", "graduationTime");
        assertEquals(dbRecord.getEmployeeId(), educationExperience.getEmployeeId());
        assertEquals(reqVO.getAdmissionTime().toLocalDate().atStartOfDay(), educationExperience.getAdmissionTime());
        assertEquals(reqVO.getGraduationTime().toLocalDate().atStartOfDay(), educationExperience.getGraduationTime());
    }

    @Test
    public void testDeleteEducationExperience_success() {
        // mock 数据
        HrmEmployeeEducationExperienceDO dbRecord = randomPojo(HrmEmployeeEducationExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setEducation(1)
                        .setTeachingMethods(1).setFirstDegree(false).setSort(20).setDeleted(false));
        educationExperienceMapper.insert(dbRecord);
        // 准备参数
        Long id = dbRecord.getId();

        // 调用
        educationExperienceService.deleteEducationExperience(id);

        // 断言
        assertNull(educationExperienceMapper.selectById(id));
    }

    @Test
    public void testDeleteEducationExperience_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> educationExperienceService.deleteEducationExperience(id),
                EMPLOYEE_EDUCATION_EXPERIENCE_NOT_EXISTS);
    }

    @Test
    public void testGetEducationExperienceListByEmployeeId() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeEducationExperienceDO firstRecord = randomPojo(HrmEmployeeEducationExperienceDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setEducation(1)
                        .setTeachingMethods(1).setFirstDegree(true).setSort(20).setDeleted(false));
        educationExperienceMapper.insert(firstRecord);
        HrmEmployeeEducationExperienceDO secondRecord = randomPojo(HrmEmployeeEducationExperienceDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setEducation(2)
                        .setTeachingMethods(2).setFirstDegree(false).setSort(10).setDeleted(false));
        educationExperienceMapper.insert(secondRecord);
        educationExperienceMapper.insert(randomPojo(HrmEmployeeEducationExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setEducation(3)
                        .setTeachingMethods(1).setFirstDegree(false).setSort(1).setDeleted(false)));

        // 调用
        List<HrmEmployeeEducationExperienceDO> result =
                educationExperienceService.getEducationExperienceListByEmployeeId(employeeId);
        // 断言
        assertEquals(2, result.size());
        assertEquals(secondRecord.getId(), result.get(0).getId());
        assertEquals(firstRecord.getId(), result.get(1).getId());
        assertNull(educationExperienceMapper.selectById(randomLongId()));
    }

}
