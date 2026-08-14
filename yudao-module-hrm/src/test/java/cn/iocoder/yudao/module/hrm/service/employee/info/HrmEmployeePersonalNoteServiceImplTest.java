package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.personalnote.HrmEmployeePersonalNoteCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeePersonalNoteDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeePersonalNoteMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_PERSONAL_NOTE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * {@link HrmEmployeePersonalNoteServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeePersonalNoteServiceImpl.class)
public class HrmEmployeePersonalNoteServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeePersonalNoteServiceImpl personalNoteService;
    @Resource
    private HrmEmployeePersonalNoteMapper personalNoteMapper;

    @MockBean
    private HrmEmployeeService employeeService;

    @Test
    public void testCreatePersonalNote_success() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeePersonalNoteCreateReqVO reqVO = new HrmEmployeePersonalNoteCreateReqVO();
        reqVO.setContent("跟进转正材料");
        reqVO.setReminderTime(LocalDate.now().atTime(15, 30));

        // 调用
        Long id = personalNoteService.createPersonalNote(employeeId, reqVO);

        // 断言
        assertNotNull(id);
        HrmEmployeePersonalNoteDO personalNote = personalNoteMapper.selectById(id);
        assertPojoEquals(reqVO, personalNote);
        assertEquals(employeeId, personalNote.getEmployeeId());
        verify(employeeService).validateEmployeeExists(employeeId);
    }

    @Test
    public void testDeletePersonalNote_success() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeePersonalNoteDO personalNote =
                personalNote(employeeId, "准备周报", LocalDate.now().atTime(17, 30));
        personalNoteMapper.insert(personalNote);

        // 调用
        personalNoteService.deletePersonalNote(employeeId, personalNote.getId());

        // 断言
        assertNull(personalNoteMapper.selectById(personalNote.getId()));
    }

    @Test
    public void testDeletePersonalNote_notBelongEmployee() {
        // mock 数据
        HrmEmployeePersonalNoteDO personalNote =
                personalNote(randomLongId(), "准备周报", LocalDate.now().atTime(17, 30));
        personalNoteMapper.insert(personalNote);

        // 调用，并断言异常
        assertServiceException(() -> personalNoteService.deletePersonalNote(randomLongId(), personalNote.getId()),
                EMPLOYEE_PERSONAL_NOTE_NOT_EXISTS);
    }

    @Test
    public void testGetPersonalNoteList() {
        // mock 数据
        Long employeeId = randomLongId();
        LocalDateTime beginTime = LocalDate.now().atStartOfDay();
        HrmEmployeePersonalNoteDO secondNote =
                personalNote(employeeId, "下午事项", beginTime.plusHours(15));
        personalNoteMapper.insert(secondNote);
        HrmEmployeePersonalNoteDO firstNote =
                personalNote(employeeId, "上午事项", beginTime.plusHours(9));
        personalNoteMapper.insert(firstNote);
        personalNoteMapper.insert(personalNote(randomLongId(), "其他员工事项", beginTime.plusHours(10)));
        HrmEmployeePersonalNoteDO endBoundaryNote =
                personalNote(employeeId, "结束边界事项", beginTime.plusDays(1));
        personalNoteMapper.insert(endBoundaryNote);
        personalNoteMapper.insert(personalNote(employeeId, "范围外事项", beginTime.plusDays(1).plusSeconds(1)));

        // 调用
        List<HrmEmployeePersonalNoteDO> result =
                personalNoteService.getPersonalNoteList(
                        employeeId, new LocalDateTime[]{beginTime, beginTime.plusDays(1)});

        // 断言
        assertEquals(3, result.size());
        assertEquals(firstNote.getId(), result.get(0).getId());
        assertEquals(secondNote.getId(), result.get(1).getId());
        assertEquals(endBoundaryNote.getId(), result.get(2).getId());
    }

    private HrmEmployeePersonalNoteDO personalNote(
            Long employeeId, String content, LocalDateTime reminderTime) {
        HrmEmployeePersonalNoteDO personalNote = new HrmEmployeePersonalNoteDO();
        personalNote.setEmployeeId(employeeId);
        personalNote.setContent(content);
        personalNote.setReminderTime(reminderTime);
        return personalNote;
    }

}
