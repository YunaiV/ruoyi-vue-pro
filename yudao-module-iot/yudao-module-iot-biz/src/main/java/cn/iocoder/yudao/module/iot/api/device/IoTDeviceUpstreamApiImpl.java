package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import cn.iocoder.yudao.module.iot.service.device.control.IotDeviceUpstreamService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * * 设备数据 Upstream 上行 API 实现类
 */
@RestController
@Validated
@Primary // 保证优先匹配，因为 yudao-module-iot-net-component-core 也有 IotDeviceUpstreamApi 的实现，并且也可能会被 biz 引入
public class IoTDeviceUpstreamApiImpl implements IotDeviceUpstreamApi {

    @Resource
    private IotDeviceUpstreamService deviceUpstreamService;

    @Override
    public CommonResult<Boolean> authenticateEmqxConnection(IotDeviceEmqxAuthReqDTO authReqDTO) {
        boolean result = deviceUpstreamService.authenticateEmqxConnection(authReqDTO);
        return success(result);
    }

}