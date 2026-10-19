package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleDO;
import cn.iocoder.yudao.module.oa.dal.mysql.vehicle.OaVehicleMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 车辆 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaVehicleServiceImpl implements OaVehicleService {

    @Resource
    private OaVehicleMapper vehicleMapper;

    @Resource
    private DeptApi deptApi;

    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private OaVehicleApplyService vehicleApplyService;

    @Override
    public Long createVehicle(OaVehicleSaveReqVO createReqVO) {
        // 1.1 校验车牌号唯一性
        validateVehicleNoUnique(null, createReqVO.getNo());
        // 1.2 校验所属部门有效
        if (createReqVO.getDeptId() != null) {
            deptApi.validateDeptList(Collections.singleton(createReqVO.getDeptId()));
        }

        // 2. 保存车辆信息
        OaVehicleDO vehicle = BeanUtils.toBean(createReqVO, OaVehicleDO.class);
        vehicleMapper.insert(vehicle);
        return vehicle.getId();
    }

    @Override
    public void updateVehicle(OaVehicleSaveReqVO updateReqVO) {
        // 1.1 校验车辆存在
        validateVehicleExists(updateReqVO.getId());
        // 1.2 校验车牌号唯一性
        validateVehicleNoUnique(updateReqVO.getId(), updateReqVO.getNo());
        // 1.3 校验所属部门有效
        if (updateReqVO.getDeptId() != null) {
            deptApi.validateDeptList(Collections.singleton(updateReqVO.getDeptId()));
        }

        // 2. 保存基础信息和车辆状态
        vehicleMapper.updateById(BeanUtils.toBean(updateReqVO, OaVehicleDO.class));
    }

    @Override
    public PageResult<OaVehicleDO> getVehiclePage(OaVehiclePageReqVO pageReqVO) {
        return vehicleMapper.selectPage(pageReqVO);
    }

    @Override
    public void deleteVehicle(Long id) {
        // 1.1 校验车辆存在
        validateVehicleExists(id);
        // 1.2 校验车辆未被业务单据引用
        if (vehicleApplyService.getVehicleApplyCountByVehicleId(id) > 0) {
            throw exception(VEHICLE_IN_USE);
        }

        // 2. 删除车辆信息
        vehicleMapper.deleteById(id);
    }

    @Override
    public OaVehicleDO validateVehicleExists(Long id) {
        OaVehicleDO vehicle = vehicleMapper.selectById(id);
        if (vehicle == null) {
            throw exception(VEHICLE_NOT_EXISTS);
        }
        return vehicle;
    }

    /**
     * 校验车辆编号唯一
     *
     * @param id 当前编号，新增时为空
     * @param no 业务编号
     */
    private void validateVehicleNoUnique(Long id, String no) {
        OaVehicleDO vehicle = vehicleMapper.selectByNo(no);
        if (vehicle != null && ObjUtil.notEqual(vehicle.getId(), id)) {
            throw exception(VEHICLE_NO_DUPLICATE);
        }
    }

}
