package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.trainingexperience.HrmEmployeeTrainingExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeTrainingExperienceDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience.HrmEmployeeTrainingExperienceMapper;
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
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_TRAINING_EXPERIENCE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * {@link HrmEmployeeTrainingExperienceServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeTrainingExperienceServiceImpl.class)
public class HrmEmployeeTrainingExperienceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeTrainingExperienceServiceImpl trainingExperienceService;

    @Resource
    private HrmEmployeeTrainingExperienceMapper trainingExperienceMapper;

    @MockBean
    private HrmEmployeeService employeeService;

    @Test
    public void testCreateTrainingExperience_success() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeTrainingExperienceSaveReqVO reqVO = randomPojo(
                HrmEmployeeTrainingExperienceSaveReqVO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(10)
                        .setStartTime(LocalDateTime.of(2026, 3, 1, 9, 30))
                        .setEndTime(LocalDateTime.of(2026, 3, 31, 18, 0)));

        // 调用
        Long id = trainingExperienceService.createTrainingExperience(reqVO);

        // 断言
        assertNotNull(id);
        HrmEmployeeTrainingExperienceDO trainingExperience = trainingExperienceMapper.selectById(id);
        assertPojoEquals(reqVO, trainingExperience, "id", "startTime", "endTime");
        assertEquals(reqVO.getStartTime().toLocalDate().atStartOfDay(), trainingExperience.getStartTime());
        assertEquals(reqVO.getEndTime().toLocalDate().atStartOfDay(), trainingExperience.getEndTime());
        verify(employeeService).validateEmployeeExists(employeeId);
    }

    @Test
    public void testUpdateTrainingExperience_success() {
        // mock 数据
        HrmEmployeeTrainingExperienceDO dbRecord = randomPojo(HrmEmployeeTrainingExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(20).setDeleted(false));
        trainingExperienceMapper.insert(dbRecord);
        // 准备参数
        HrmEmployeeTrainingExperienceSaveReqVO reqVO = randomPojo(
                HrmEmployeeTrainingExperienceSaveReqVO.class,
                o -> o.setId(dbRecord.getId()).setEmployeeId(dbRecord.getEmployeeId()).setSort(10)
                        .setStartTime(LocalDateTime.of(2026, 4, 1, 9, 30))
                        .setEndTime(LocalDateTime.of(2026, 4, 30, 18, 0)));

        // 调用
        trainingExperienceService.updateTrainingExperience(reqVO);

        // 断言
        HrmEmployeeTrainingExperienceDO trainingExperience = trainingExperienceMapper.selectById(dbRecord.getId());
        assertPojoEquals(reqVO, trainingExperience, "employeeId", "startTime", "endTime");
        assertEquals(dbRecord.getEmployeeId(), trainingExperience.getEmployeeId());
        assertEquals(reqVO.getStartTime().toLocalDate().atStartOfDay(), trainingExperience.getStartTime());
        assertEquals(reqVO.getEndTime().toLocalDate().atStartOfDay(), trainingExperience.getEndTime());
    }

    @Test
    public void testDeleteTrainingExperience_success() {
        // mock 数据
        HrmEmployeeTrainingExperienceDO dbRecord = randomPojo(HrmEmployeeTrainingExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(20).setDeleted(false));
        trainingExperienceMapper.insert(dbRecord);
        // 准备参数
        Long id = dbRecord.getId();

        // 调用
        trainingExperienceService.deleteTrainingExperience(id);

        // 断言
        assertNull(trainingExperienceMapper.selectById(id));
    }

    @Test
    public void testDeleteTrainingExperience_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> trainingExperienceService.deleteTrainingExperience(id),
                EMPLOYEE_TRAINING_EXPERIENCE_NOT_EXISTS);
    }

    @Test
    public void testGetTrainingExperienceListByEmployeeId() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeTrainingExperienceDO firstRecord = randomPojo(HrmEmployeeTrainingExperienceDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(20).setDeleted(false));
        trainingExperienceMapper.insert(firstRecord);
        HrmEmployeeTrainingExperienceDO secondRecord = randomPojo(HrmEmployeeTrainingExperienceDO.class,
                o -> o.setId(null).setEmployeeId(employeeId).setSort(10).setDeleted(false));
        trainingExperienceMapper.insert(secondRecord);
        trainingExperienceMapper.insert(randomPojo(HrmEmployeeTrainingExperienceDO.class,
                o -> o.setId(null).setEmployeeId(randomLongId()).setSort(1).setDeleted(false)));

        // 调用
        List<HrmEmployeeTrainingExperienceDO> result =
                trainingExperienceService.getTrainingExperienceListByEmployeeId(employeeId);
        // 断言
        assertEquals(2, result.size());
        assertEquals(secondRecord.getId(), result.get(0).getId());
        assertEquals(firstRecord.getId(), result.get(1).getId());
    }

}
