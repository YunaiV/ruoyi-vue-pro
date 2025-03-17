package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyHistoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.data.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 设备属性")
@RestController
@RequestMapping("/iot/device/property")
@Validated
public class IotDevicePropertyController {

    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotThingModelService thingModelService;
    @Resource
    private IotDeviceService deviceService;

    @GetMapping("/latest")
    @Operation(summary = "获取设备属性最新属性")
    @Parameters({
            @Parameter(name = "deviceId", description = "设备编号", required = true),
            @Parameter(name = "identifier", description = "标识符"),
            @Parameter(name = "name", description = "名称")
    })
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<List<IotDevicePropertyRespVO>> getLatestDeviceProperties(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "name", required = false) String name) {
        Map<String, IotDevicePropertyDO> properties = devicePropertyService.getLatestDeviceProperties(deviceId);

        // 拼接数据
        IotDeviceDO device = deviceService.getDevice(deviceId);
        Assert.notNull(device, "设备不存在");
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductId(device.getProductId());
        return success(convertList(properties.entrySet(), entry -> {
            IotThingModelDO thingModel = CollUtil.findOne(thingModels,
                    item -> item.getIdentifier().equals(entry.getKey()));
            if (thingModel == null || thingModel.getProperty() == null) {
                return null;
            }
            if (StrUtil.isNotEmpty(identifier) && !StrUtil.contains(thingModel.getIdentifier(), identifier)) {
                return null;
            }
            if (StrUtil.isNotEmpty(name) && !StrUtil.contains(thingModel.getName(), name)) {
                return null;
            }
            // 构建对象
            IotDevicePropertyDO property = entry.getValue();
            return new IotDevicePropertyRespVO().setProperty(thingModel.getProperty())
                    .setValue(property.getValue()).setUpdateTime(LocalDateTimeUtil.toEpochMilli(property.getUpdateTime()));
        }));
    }

    @GetMapping("/history-page")
    @Operation(summary = "获取设备属性历史数据")
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<PageResult<IotDevicePropertyRespVO>> getHistoryDevicePropertyPage(
            @Valid IotDevicePropertyHistoryPageReqVO pageReqVO) {
        Assert.notEmpty(pageReqVO.getIdentifier(), "标识符不能为空");
        return success(devicePropertyService.getHistoryDevicePropertyPage(pageReqVO));
    }

}