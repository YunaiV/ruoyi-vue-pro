package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.OaVehiclePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.OaVehicleSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleDO;
import cn.iocoder.yudao.module.oa.dal.mysql.vehicle.OaVehicleMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaVehicleServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaVehicleServiceImpl.class)
public class OaVehicleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaVehicleService vehicleService;

    @Resource
    private OaVehicleMapper vehicleMapper;

    @MockitoBean
    private DeptApi deptApi;
    @MockitoBean
    private OaVehicleApplyService vehicleApplyService;

    @Test
    public void testDeleteVehicle_inUse() {
        // mock 数据
        OaVehicleDO record = randomVehicleDO();
        vehicleMapper.insert(record);
        // mock 方法
        when(vehicleApplyService.getVehicleApplyCountByVehicleId(record.getId())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> vehicleService.deleteVehicle(record.getId()), VEHICLE_IN_USE);
        assertNotNull(vehicleMapper.selectById(record.getId()));
    }

    @Test
    public void testGetVehiclePage_filters() {
        // mock 数据
        LocalDateTime expireTime = LocalDateTime.of(2026, 9, 12, 12, 0);
        OaVehicleDO vehicle = randomVehicleDO().setType("商务车").setBrandModel("别克 GL8")
                .setCompulsoryInsuranceExpireTime(expireTime).setCommercialInsuranceExpireTime(expireTime)
                .setInspectionExpireTime(expireTime);
        vehicleMapper.insert(vehicle);
        // 准备参数
        OaVehiclePageReqVO reqVO = new OaVehiclePageReqVO().setType("商务").setBrandModel("GL8")
                .setCompulsoryInsuranceExpireTime(new LocalDateTime[]{expireTime, expireTime})
                .setCommercialInsuranceExpireTime(new LocalDateTime[]{expireTime, expireTime})
                .setInspectionExpireTime(new LocalDateTime[]{expireTime, expireTime});

        // 调用，并断言：组合筛选包含时间边界，各个条件独立生效
        assertEquals(1, vehicleService.getVehiclePage(reqVO).getTotal());
        assertEquals(0, vehicleService.getVehiclePage(BeanUtils.toBean(reqVO, OaVehiclePageReqVO.class)
                .setType("货车")).getTotal());
        assertEquals(0, vehicleService.getVehiclePage(BeanUtils.toBean(reqVO, OaVehiclePageReqVO.class)
                .setBrandModel("宝马")).getTotal());
        LocalDateTime[] outsideTime = {expireTime.plusSeconds(1), expireTime.plusDays(1)};
        assertEquals(0, vehicleService.getVehiclePage(BeanUtils.toBean(reqVO, OaVehiclePageReqVO.class)
                .setCompulsoryInsuranceExpireTime(outsideTime)).getTotal());
        assertEquals(0, vehicleService.getVehiclePage(BeanUtils.toBean(reqVO, OaVehiclePageReqVO.class)
                .setCommercialInsuranceExpireTime(outsideTime)).getTotal());
        assertEquals(0, vehicleService.getVehiclePage(BeanUtils.toBean(reqVO, OaVehiclePageReqVO.class)
                .setInspectionExpireTime(outsideTime)).getTotal());
    }

    @Test
    public void testUpdateVehicleStatus() {
        // mock 数据
        OaVehicleDO vehicle = randomVehicleDO();
        vehicleMapper.insert(vehicle);
        // 准备参数
        OaVehicleSaveReqVO reqVO = BeanUtils.toBean(vehicle, OaVehicleSaveReqVO.class).setStatus(2);

        // 调用
        vehicleService.updateVehicle(reqVO);
        vehicleService.updateVehicle(reqVO);

        // 断言
        assertEquals(2, vehicleMapper.selectById(vehicle.getId()).getStatus());
    }

    @Test
    public void testDeleteVehicle_success() {
        // mock 数据
        OaVehicleDO vehicle = randomVehicleDO();
        vehicleMapper.insert(vehicle);

        // 调用
        vehicleService.deleteVehicle(vehicle.getId());

        // 断言
        assertNull(vehicleMapper.selectById(vehicle.getId()));
        assertServiceException(() -> vehicleService.validateVehicleExists(vehicle.getId()), VEHICLE_NOT_EXISTS);
    }

    @Test
    public void testDeleteVehicle_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> vehicleService.deleteVehicle(-1L), VEHICLE_NOT_EXISTS);
    }

    @Test
    public void testUpdateVehicle_inUseToIdle() {
        // mock 数据
        OaVehicleDO vehicle = randomVehicleDO().setStatus(2);
        vehicleMapper.insert(vehicle);
        // 准备参数
        OaVehicleSaveReqVO reqVO = BeanUtils.toBean(vehicle, OaVehicleSaveReqVO.class).setStatus(0);

        // 调用
        vehicleService.updateVehicle(reqVO);
        // 断言
        assertEquals(0, vehicleMapper.selectById(vehicle.getId()).getStatus());
    }

    @Test
    public void testCreateVehicle_deptAndImage() {
        // 准备参数
        OaVehicleSaveReqVO reqVO = BeanUtils.toBean(randomVehicleDO(), OaVehicleSaveReqVO.class)
                .setDeptId(103L).setPicUrl("https://example.com/vehicle.png").setStatus(2);

        // 调用
        Long id = vehicleService.createVehicle(reqVO);
        // 断言
        OaVehicleDO vehicle = vehicleMapper.selectById(id);
        assertEquals(103L, vehicle.getDeptId());
        assertEquals(reqVO.getPicUrl(), vehicle.getPicUrl());
        assertEquals(2, vehicle.getStatus());
        verify(deptApi).validateDeptList(Collections.singleton(103L));
    }

    @Test
    public void testUpdateVehicle_deptAndImage() {
        // mock 数据
        OaVehicleDO vehicle = randomVehicleDO();
        vehicleMapper.insert(vehicle);
        // 准备参数
        OaVehicleSaveReqVO reqVO = BeanUtils.toBean(vehicle, OaVehicleSaveReqVO.class)
                .setDeptId(103L).setPicUrl("https://example.com/new.png");

        // 调用
        vehicleService.updateVehicle(reqVO);
        // 断言
        OaVehicleDO updatedVehicle = vehicleMapper.selectById(vehicle.getId());
        assertEquals(103L, updatedVehicle.getDeptId());
        assertEquals(reqVO.getPicUrl(), updatedVehicle.getPicUrl());
        verify(deptApi).validateDeptList(Collections.singleton(103L));
    }

    @Test
    public void testUpdateVehicle_clearDeptAndImage() {
        // mock 数据
        OaVehicleDO vehicle = randomVehicleDO().setDeptId(103L).setPicUrl("https://example.com/old.png");
        vehicleMapper.insert(vehicle);
        // 准备参数
        OaVehicleSaveReqVO reqVO = BeanUtils.toBean(vehicle, OaVehicleSaveReqVO.class)
                .setDeptId(null).setPicUrl("");

        // 调用
        vehicleService.updateVehicle(reqVO);
        // 断言
        OaVehicleDO updatedVehicle = vehicleMapper.selectById(vehicle.getId());
        assertNull(updatedVehicle.getDeptId());
        assertEquals("", updatedVehicle.getPicUrl());
        verifyNoInteractions(deptApi);
    }

    @Test
    public void testCreateVehicle_deletedPlateCanBeReused() {
        // mock 数据
        OaVehicleDO vehicle = randomVehicleDO();
        vehicleMapper.insert(vehicle);
        OaVehicleSaveReqVO reqVO = BeanUtils.toBean(vehicle, OaVehicleSaveReqVO.class).setId(null);

        // 调用，并断言：有效车牌仍由后端查重
        assertServiceException(() -> vehicleService.createVehicle(reqVO), VEHICLE_NO_DUPLICATE);
        vehicleService.deleteVehicle(vehicle.getId());
        Long newId = vehicleService.createVehicle(reqVO);
        assertEquals(vehicle.getNo(), vehicleMapper.selectById(newId).getNo());
        assertNotEquals(vehicle.getId(), newId);
    }

    // ========== 随机对象 ==========

    /**
     * 构造可用车辆。
     *
     * @return 未入库的测试对象
     */
    private static OaVehicleDO randomVehicleDO() {
        return randomPojo(OaVehicleDO.class, vehicle -> vehicle.setId(null).setNo("沪A12345")
                .setName("商务车").setType("1").setSeatCount(5).setBarePrice(BigDecimal.ZERO)
                .setStatus(0).setSort(0));
    }

}
