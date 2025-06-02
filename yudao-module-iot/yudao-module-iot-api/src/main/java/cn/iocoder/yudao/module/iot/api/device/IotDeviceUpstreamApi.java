package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import cn.iocoder.yudao.module.iot.enums.ApiConstants;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 设备数据 Upstream 上行 API
 *
 * 目的：设备 -> 插件 -> 服务端
 *
 * @author haohao
 */
public interface IotDeviceUpstreamApi {

    String PREFIX = ApiConstants.PREFIX + "/device/upstream";

    // TODO @芋艿：考虑 http 认证
    /**
     * 认证 Emqx 连接
     *
     * @param authReqDTO 认证 Emqx 连接 DTO
     */
    @PostMapping(PREFIX + "/authenticate-emqx-connection")
    CommonResult<Boolean> authenticateEmqxConnection(@Valid @RequestBody IotDeviceEmqxAuthReqDTO authReqDTO);

}