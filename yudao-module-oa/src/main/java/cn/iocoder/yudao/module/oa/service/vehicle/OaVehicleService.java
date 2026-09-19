package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleDO;
import jakarta.validation.Valid;

/**
 * 车辆 Service 接口
 *
 * @author 芋道源码
 */
public interface OaVehicleService {

    /**
     * 创建车辆
     *
     * @param createReqVO 车辆信息
     * @return 编号
     */
    Long createVehicle(@Valid OaVehicleSaveReqVO createReqVO);

    /**
     * 更新车辆
     *
     * @param updateReqVO 车辆信息
     */
    void updateVehicle(@Valid OaVehicleSaveReqVO updateReqVO);

    /**
     * 删除车辆
     *
     * @param id 编号
     */
    void deleteVehicle(Long id);

    /**
     * 获得车辆分页
     *
     * @param pageReqVO 分页查询
     * @return 车辆分页
     */
    PageResult<OaVehicleDO> getVehiclePage(OaVehiclePageReqVO pageReqVO);

    /**
     * 校验车辆存在
     *
     * @param id 编号
     * @return 车辆
     */
    OaVehicleDO validateVehicleExists(Long id);

}
