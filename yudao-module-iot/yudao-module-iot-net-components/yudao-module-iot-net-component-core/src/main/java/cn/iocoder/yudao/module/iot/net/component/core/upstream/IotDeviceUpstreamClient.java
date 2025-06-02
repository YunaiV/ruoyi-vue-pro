package cn.iocoder.yudao.module.iot.net.component.core.upstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
/**
 * 设备数据 Upstream 上行客户端
 * <p>
 * 直接调用 IotDeviceUpstreamApi 接口
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamClient implements IotDeviceUpstreamApi {

    @Resource
    private IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    public CommonResult<Boolean> authenticateEmqxConnection(IotDeviceEmqxAuthReqDTO authReqDTO) {
        return deviceUpstreamApi.authenticateEmqxConnection(authReqDTO);
    }

}
