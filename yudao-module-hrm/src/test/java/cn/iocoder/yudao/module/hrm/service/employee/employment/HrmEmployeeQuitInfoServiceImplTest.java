package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.quitinfo.HrmEmployeeQuitInfoSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment.HrmEmployeeQuitInfoMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_QUIT_INFO_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link HrmEmployeeQuitInfoServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmEmployeeQuitInfoServiceImpl.class)
public class HrmEmployeeQuitInfoServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmEmployeeQuitInfoServiceImpl quitInfoService;

    @Resource
    private HrmEmployeeQuitInfoMapper quitInfoMapper;

    @Test
    public void testSaveEmployeeQuitInfo_create() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmEmployeeQuitInfoSaveReqVO reqVO = randomPojo(HrmEmployeeQuitInfoSaveReqVO.class,
                o -> o.setEmployeeId(employeeId).setPlanQuitTime(LocalDateTime.now().plusDays(10))
                        .setType(1).setReason(1).setOldEmployeeStatus(1));

        // 调用
        Long id = quitInfoService.saveEmployeeQuitInfo(reqVO);

        // 断言
        assertNotNull(id);
        assertPojoEquals(reqVO, quitInfoMapper.selectById(id));
    }

    @Test
    public void testSaveEmployeeQuitInfo_update() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeQuitInfoDO dbQuitInfo =
                randomQuitInfoDO(employeeId, LocalDateTime.now().plusDays(10));
        quitInfoMapper.insert(dbQuitInfo);
        // 准备参数
        HrmEmployeeQuitInfoSaveReqVO reqVO = randomPojo(HrmEmployeeQuitInfoSaveReqVO.class,
                o -> o.setEmployeeId(employeeId).setPlanQuitTime(LocalDateTime.now().plusDays(20))
                        .setType(2).setReason(11).setOldEmployeeStatus(2));

        // 调用
        Long id = quitInfoService.saveEmployeeQuitInfo(reqVO);

        // 断言
        assertEquals(dbQuitInfo.getId(), id);
        assertPojoEquals(reqVO, quitInfoMapper.selectById(id));
        assertEquals(1, quitInfoMapper.selectList().size());
    }

    @Test
    public void testDeleteEmployeeQuitInfo_success() {
        // mock 数据
        HrmEmployeeQuitInfoDO quitInfo = randomQuitInfoDO(randomLongId(), LocalDateTime.now().plusDays(10));
        quitInfoMapper.insert(quitInfo);

        // 调用
        quitInfoService.deleteEmployeeQuitInfo(quitInfo.getEmployeeId());
        // 断言
        assertNull(quitInfoMapper.selectById(quitInfo.getId()));
    }

    @Test
    public void testValidateQuitInfoByEmployeeId_notExists() {
        // 准备参数
        Long employeeId = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> quitInfoService.validateQuitInfoByEmployeeId(employeeId),
                EMPLOYEE_QUIT_INFO_NOT_EXISTS);
    }

    @Test
    public void testValidateQuitInfoByEmployeeId_duplicateReturnsLatest() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmEmployeeQuitInfoDO firstQuitInfo = randomQuitInfoDO(
                employeeId, LocalDateTime.now().plusDays(10));
        quitInfoMapper.insert(firstQuitInfo);
        HrmEmployeeQuitInfoDO latestQuitInfo = randomQuitInfoDO(
                employeeId, LocalDateTime.now().plusDays(20));
        quitInfoMapper.insert(latestQuitInfo);

        // 调用
        HrmEmployeeQuitInfoDO result = quitInfoService.validateQuitInfoByEmployeeId(employeeId);

        // 断言
        assertEquals(latestQuitInfo.getId(), result.getId());
    }

    @Test
    public void testGetDueQuitInfoList() {
        // mock 数据
        LocalDate planQuitDate = LocalDate.of(2026, 8, 1);
        HrmEmployeeQuitInfoDO firstRecord = randomQuitInfoDO(randomLongId(), planQuitDate.atTime(9, 30));
        quitInfoMapper.insert(firstRecord);
        HrmEmployeeQuitInfoDO secondRecord = randomQuitInfoDO(randomLongId(), planQuitDate.atTime(18, 45));
        quitInfoMapper.insert(secondRecord);
        quitInfoMapper.insert(randomQuitInfoDO(randomLongId(), planQuitDate.plusDays(1).atStartOfDay()));

        // 调用
        List<HrmEmployeeQuitInfoDO> result = quitInfoService.getDueQuitInfoList(planQuitDate.atTime(18, 45));
        // 断言
        assertEquals(2, result.size());
        assertEquals(firstRecord.getId(), result.get(0).getId());
        assertEquals(secondRecord.getId(), result.get(1).getId());
    }

    @Test
    public void testGetQuitInfoListByEmployeeIds() {
        // mock 数据
        HrmEmployeeQuitInfoDO quitInfo = randomQuitInfoDO(
                randomLongId(), LocalDateTime.now().plusDays(10));
        quitInfoMapper.insert(quitInfo);
        quitInfoMapper.insert(randomQuitInfoDO(randomLongId(), LocalDateTime.now().plusDays(20)));

        // 调用
        List<HrmEmployeeQuitInfoDO> result = quitInfoService.getQuitInfoListByEmployeeIds(
                Arrays.asList(quitInfo.getEmployeeId(), randomLongId()));

        // 断言
        assertEquals(1, result.size());
        assertEquals(quitInfo.getId(), result.get(0).getId());
        assertEquals(Collections.emptyList(), quitInfoService.getQuitInfoListByEmployeeIds(Collections.emptyList()));
    }

    private static HrmEmployeeQuitInfoDO randomQuitInfoDO(Long employeeId, LocalDateTime planQuitTime) {
        return randomPojo(HrmEmployeeQuitInfoDO.class, o -> o.setId(null).setEmployeeId(employeeId)
                .setPlanQuitTime(planQuitTime).setType(1).setReason(1).setOldEmployeeStatus(1).setDeleted(false));
    }

}
