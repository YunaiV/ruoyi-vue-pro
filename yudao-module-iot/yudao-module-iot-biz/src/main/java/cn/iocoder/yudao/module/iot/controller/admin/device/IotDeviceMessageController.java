package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessageRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessageSendReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备消息")
@RestController
@RequestMapping("/iot/device/message")
@Validated
public class IotDeviceMessageController {

    @Resource
    private IotDeviceMessageService deviceMessageService;

    @GetMapping("/page")
    @Operation(summary = "获得设备消息分页")
    @PreAuthorize("@ss.hasPermission('iot:device:message-query')")
    public CommonResult<PageResult<IotDeviceMessageRespVO>> getDeviceLogPage(@Valid IotDeviceMessagePageReqVO pageReqVO) {
        PageResult<IotDeviceMessageDO> pageResult = deviceMessageService.getDeviceMessagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceMessageRespVO.class));
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "可用于设备模拟")
    @PreAuthorize("@ss.hasPermission('iot:device:message-end')")
    public CommonResult<Boolean> upstreamDevice(@Valid @RequestBody IotDeviceMessageSendReqVO sendReqVO) {
        deviceMessageService.sendDeviceMessage(BeanUtils.toBean(sendReqVO, IotDeviceMessage.class));
        return success(true);
    }

}