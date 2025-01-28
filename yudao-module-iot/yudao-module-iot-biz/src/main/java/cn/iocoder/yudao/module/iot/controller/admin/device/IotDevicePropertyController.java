package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.data.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // TODO @芋艿：权限
    @GetMapping("/latest")
    @Operation(summary = "获取设备属性最新属性")
    public CommonResult<List<IotDevicePropertyRespVO>> getLatestDeviceProperties(@Valid IotDevicePropertyPageReqVO pageReqVO) {
        Map<String, IotDevicePropertyDO> properties = devicePropertyService.getLatestDeviceProperties(pageReqVO);

        // 拼接数据
        IotDeviceDO device = deviceService.getDevice(pageReqVO.getDeviceId());
        Assert.notNull(device, "设备不存在");
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductId(device.getProductId());
        return success(convertList(properties.entrySet(), entry -> {
            IotThingModelDO thingModel = CollUtil.findOne(thingModels,
                    item -> item.getIdentifier().equals(entry.getKey()));
            if (thingModel == null || thingModel.getProperty() == null) {
                return null;
            }
            IotDevicePropertyDO property = entry.getValue();
            return BeanUtils.toBean(thingModel, IotDevicePropertyRespVO.class)
                    .setDataType(thingModel.getProperty().getDataType())
                    .setValue(property.getValue())
                    .setUpdateTime(LocalDateTimeUtil.toEpochMilli(property.getUpdateTime()));
        }));
    }

    // TODO @芋艿：权限
    @GetMapping("/history-page")
    @Operation(summary = "获取设备属性历史数据")
    public CommonResult<PageResult<IotDevicePropertyRespVO>> getHistoryDevicePropertyPage(
            @Valid IotDevicePropertyPageReqVO pageReqVO) {
        Assert.notEmpty(pageReqVO.getIdentifier(), "标识符不能为空");
        return success(devicePropertyService.getHistoryDevicePropertyPage(pageReqVO));
    }

}