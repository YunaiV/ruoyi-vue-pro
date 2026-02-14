package cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import io.vertx.ext.web.RoutingContext;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 网关 HTTP 协议的【设备动态注册】处理器
 * <p>
 * 用于直连设备/网关的一型一密动态注册，不需要认证
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
 */
public class IotHttpRegisterHandler extends IotHttpAbstractHandler {

    public static final String PATH = "/auth/register/device";

    private final IotDeviceCommonApi deviceApi;

    public IotHttpRegisterHandler() {
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    @Override
    public CommonResult<Object> handle0(RoutingContext context) {
        // 1. 解析参数
        IotDeviceRegisterReqDTO request = deserializeRequest(context, IotDeviceRegisterReqDTO.class);
        Assert.notNull(request, "请求参数不能为空");
        Assert.notBlank(request.getProductKey(), "productKey 不能为空");
        Assert.notBlank(request.getDeviceName(), "deviceName 不能为空");
        Assert.notBlank(request.getSign(), "sign 不能为空");

        // 2. 调用动态注册
        CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(request);
        result.checkError();

        // 3. 返回结果
        return success(result.getData());
    }

}
