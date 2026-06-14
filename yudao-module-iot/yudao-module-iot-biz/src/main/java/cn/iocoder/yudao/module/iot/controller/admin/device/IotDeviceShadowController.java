package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.shadow.IotDeviceShadowPropertyRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.shadow.IotDeviceShadowRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.shadow.IotDeviceShadowUpdateDesiredReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.shadow.IotDeviceShadowUpdateStatusReqVO;
import cn.iocoder.yudao.module.iot.core.enums.shadow.IotDeviceShadowMetadatas;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceShadowDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.shadow.IotDeviceShadowService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 设备影子")
@RestController
@RequestMapping("/iot/device/shadow")
@Validated
public class IotDeviceShadowController {

    @Resource
    private IotDeviceShadowService deviceShadowService;
    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotThingModelService thingModelService;

    @GetMapping("/get")
    @Operation(summary = "获取设备影子")
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:shadow-query')")
    public CommonResult<IotDeviceShadowRespVO> getDeviceShadow(@RequestParam("deviceId") Long deviceId) {
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);
        IotDeviceShadowDO shadow = deviceShadowService.getOrCreateShadow(device);

        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
        Map<String, Object> desired = shadow.getDesired() != null ? shadow.getDesired() : MapUtil.empty();
        Map<String, Object> reported = shadow.getReported() != null ? shadow.getReported() : MapUtil.empty();
        Map<String, Object> metadata = shadow.getMetadata() != null ? shadow.getMetadata() : MapUtil.empty();
        List<IotDeviceShadowPropertyRespVO> properties = convertList(thingModels, thingModel -> {
            ThingModelProperty prop = thingModel.getProperty();
            Assert.notNull(prop, "属性不能为空");
            return new IotDeviceShadowPropertyRespVO()
                    .setIdentifier(thingModel.getIdentifier())
                    .setName(thingModel.getName())
                    .setDataType(prop.getDataType())
                    .setDataSpecs(prop.getDataSpecs())
                    .setDataSpecsList(prop.getDataSpecsList())
                    .setDesiredValue(desired.get(thingModel.getIdentifier()))
                    .setReportedValue(reported.get(thingModel.getIdentifier()))
                    .setDesiredTime(IotDeviceShadowMetadatas.getDesiredTimestamp(metadata, thingModel.getIdentifier()))
                    .setReportedTime(IotDeviceShadowMetadatas.getReportedTimestamp(metadata, thingModel.getIdentifier()));
        });

        return success(new IotDeviceShadowRespVO()
                .setVersion(shadow.getVersion())
                .setShadowTime(shadow.getShadowTime())
                .setStatus(shadow.getStatus())
                .setProperties(properties)
                .setDesired(desired)
                .setReported(reported)
                .setMetadata(metadata));
    }

    @PutMapping("/update-desired")
    @Operation(summary = "更新设备影子期望状态")
    @PreAuthorize("@ss.hasPermission('iot:device:shadow-update')")
    public CommonResult<Boolean> updateDesiredState(@Valid @RequestBody IotDeviceShadowUpdateDesiredReqVO reqVO) {
        deviceShadowService.updateDesiredState(reqVO.getDeviceId(), reqVO.getDesired());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新设备影子开关状态")
    @PreAuthorize("@ss.hasPermission('iot:device:shadow-update')")
    public CommonResult<Boolean> updateShadowStatus(@Valid @RequestBody IotDeviceShadowUpdateStatusReqVO reqVO) {
        deviceShadowService.updateStatus(reqVO.getDeviceId(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/reset")
    @Operation(summary = "重置设备影子")
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:shadow-update')")
    public CommonResult<Boolean> resetShadow(@RequestParam("deviceId") Long deviceId) {
        deviceShadowService.resetShadow(deviceId);
        return success(true);
    }

}
