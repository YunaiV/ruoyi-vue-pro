package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceOpcUaPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT 设备 OPC UA 点位配置")
@RestController
@RequestMapping("/iot/device-opc-ua-point")
@Validated
public class IotDeviceOpcUaPointController {

    @Resource
    private IotDeviceOpcUaPointService opcUaPointService;
    @Resource
    private IotDeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建设备 OPC UA 点位配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Long> createDeviceOpcUaPoint(@Valid @RequestBody IotDeviceOpcUaPointSaveReqVO createReqVO) {
        return success(opcUaPointService.createDeviceOpcUaPoint(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备 OPC UA 点位配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceOpcUaPoint(@Valid @RequestBody IotDeviceOpcUaPointSaveReqVO updateReqVO) {
        opcUaPointService.updateDeviceOpcUaPoint(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备 OPC UA 点位配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> deleteDeviceOpcUaPoint(@RequestParam("id") Long id) {
        opcUaPointService.deleteDeviceOpcUaPoint(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备 OPC UA 点位配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceOpcUaPointRespVO> getDeviceOpcUaPoint(@RequestParam("id") Long id) {
        IotDeviceOpcUaPointDO point = opcUaPointService.getDeviceOpcUaPoint(id);
        if (point == null) {
            return success(null);
        }
        return success(buildPointList(Collections.singletonList(point)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备 OPC UA 点位配置分页")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceOpcUaPointRespVO>> getDeviceOpcUaPointPage(@Valid IotDeviceOpcUaPointPageReqVO pageReqVO) {
        PageResult<IotDeviceOpcUaPointDO> pageResult = opcUaPointService.getDeviceOpcUaPointPage(pageReqVO);
        return success(new PageResult<>(buildPointList(pageResult.getList()), pageResult.getTotal()));
    }

    /**
     * 拼接物模型属性：点位不冗余标识符和名称，关联设备所属产品的物模型属性；关联不到时标为失效
     */
    private List<IotDeviceOpcUaPointRespVO> buildPointList(List<IotDeviceOpcUaPointDO> points) {
        if (CollUtil.isEmpty(points)) {
            return Collections.emptyList();
        }
        Map<Long, IotDeviceDO> deviceMap = deviceService.getDeviceMap(convertSet(points, IotDeviceOpcUaPointDO::getDeviceId));
        Map<Long, IotThingModelDO> thingModelMap = opcUaPointService.getPropertyThingModelMap(
                convertSet(deviceMap.values(), IotDeviceDO::getProductId));
        return BeanUtils.toBean(points, IotDeviceOpcUaPointRespVO.class, pointVO -> {
            IotDeviceDO device = deviceMap.get(pointVO.getDeviceId());
            IotThingModelDO thingModel = thingModelMap.get(pointVO.getThingModelId());
            if (device == null || thingModel == null || ObjUtil.notEqual(device.getProductId(), thingModel.getProductId())) {
                pointVO.setThingModelValid(false);
                return;
            }
            pointVO.setThingModelValid(true).setIdentifier(thingModel.getIdentifier()).setName(thingModel.getName());
        });
    }

}
