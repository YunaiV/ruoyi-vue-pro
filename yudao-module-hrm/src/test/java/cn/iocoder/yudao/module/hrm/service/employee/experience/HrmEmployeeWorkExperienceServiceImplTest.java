package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.workexperience.HrmEmployeeWorkExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeWorkExperienceDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience.HrmEmployeeWorkExperienceMapper;
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
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_WORK_EXPERIENCE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * {@link HrmEmployeeWorkExperienceServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeWorkExperienceServiceImpl.class)
public class HrmEmployeeWorkExperienceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeWorkExperienceServiceImpl workExperienceService;

    @Resource
    private HrmEmployeeWorkExperienceMapper workExperienceMapper;

    @MockitoBean
    private HrmEmployeeService employeeService;

    @Test
    public void testCreateWorkExperience_success() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeWorkExperienceSaveReqVO reqVO = randomPojo(
                HrmEmployeeWorkExperienceSaveReqVO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(10)
                        .setStartTime(LocalDateTime.of(2022, 3, 1, 9, 30))
                        .setEndTime(LocalDateTime.of(2024, 2, 29, 18, 0)));

        // 调用
        Long id = workExperienceService.createWorkExperience(reqVO);

        // 断言
        assertNotNull(id);
        HrmEmployeeWorkExperienceDO workExperience = workExperienceMapper.selectById(id);
        assertPojoEquals(reqVO, workExperience, "id", "startTime", "endTime");
        assertEquals(reqVO.getStartTime().toLocalDate().atStartOfDay(), workExperience.getStartTime());
        assertEquals(reqVO.getEndTime().toLocalDate().atStartOfDay(), workExperience.getEndTime());
        verify(employeeService).validateEmployeeExists(employeeId);
    }

    @Test
    public void testUpdateWorkExperience_success() {
        // mock 数据
        HrmEmployeeWorkExperienceDO dbRecord = randomPojo(HrmEmployeeWorkExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(20).setDeleted(false));
        workExperienceMapper.insert(dbRecord);
        // 准备参数
        HrmEmployeeWorkExperienceSaveReqVO reqVO = randomPojo(
                HrmEmployeeWorkExperienceSaveReqVO.class,
                o -> o.setId(dbRecord.getId()).setEmployeeId(dbRecord.getEmployeeId()).setSort(10)
                        .setStartTime(LocalDateTime.of(2023, 3, 1, 9, 30))
                        .setEndTime(LocalDateTime.of(2025, 2, 28, 18, 0)));

        // 调用
        workExperienceService.updateWorkExperience(reqVO);

        // 断言
        HrmEmployeeWorkExperienceDO workExperience = workExperienceMapper.selectById(dbRecord.getId());
        assertPojoEquals(reqVO, workExperience, "employeeId", "startTime", "endTime");
        assertEquals(dbRecord.getEmployeeId(), workExperience.getEmployeeId());
        assertEquals(reqVO.getStartTime().toLocalDate().atStartOfDay(), workExperience.getStartTime());
        assertEquals(reqVO.getEndTime().toLocalDate().atStartOfDay(), workExperience.getEndTime());
    }

    @Test
    public void testDeleteWorkExperience_success() {
        // mock 数据
        HrmEmployeeWorkExperienceDO dbRecord = randomPojo(HrmEmployeeWorkExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(20).setDeleted(false));
        workExperienceMapper.insert(dbRecord);
        // 准备参数
        Long id = dbRecord.getId();

        // 调用
        workExperienceService.deleteWorkExperience(id);

        // 断言
        assertNull(workExperienceMapper.selectById(id));
    }

    @Test
    public void testDeleteWorkExperience_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> workExperienceService.deleteWorkExperience(id),
                EMPLOYEE_WORK_EXPERIENCE_NOT_EXISTS);
    }

    @Test
    public void testGetWorkExperienceListByEmployeeId() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeWorkExperienceDO firstRecord = randomPojo(HrmEmployeeWorkExperienceDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(20).setDeleted(false));
        workExperienceMapper.insert(firstRecord);
        HrmEmployeeWorkExperienceDO secondRecord = randomPojo(HrmEmployeeWorkExperienceDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(10).setDeleted(false));
        workExperienceMapper.insert(secondRecord);
        workExperienceMapper.insert(randomPojo(HrmEmployeeWorkExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(1).setDeleted(false)));

        // 调用
        List<HrmEmployeeWorkExperienceDO> result =
                workExperienceService.getWorkExperienceListByEmployeeId(employeeId);
        // 断言
        assertEquals(2, result.size());
        assertEquals(secondRecord.getId(), result.get(0).getId());
        assertEquals(firstRecord.getId(), result.get(1).getId());
    }

}
