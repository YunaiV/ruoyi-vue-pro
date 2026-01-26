package cn.iocoder.yudao.module.iot.gateway.protocol.coap.router;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.util.IotCoapUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Map;

/**
 * IoT 网关 CoAP 协议的【设备动态注册】处理器
 * <p>
 * 用于直连设备/网关的一型一密动态注册，不需要认证
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
 * @see cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpRegisterHandler
 */
@Slf4j
public class IotCoapRegisterHandler {

    private final IotDeviceCommonApi deviceApi;

    public IotCoapRegisterHandler() {
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    /**
     * 处理设备动态注册请求
     *
     * @param exchange CoAP 交换对象
     */
    @SuppressWarnings("unchecked")
    public void handle(CoapExchange exchange) {
        try {
            // 1.1 解析请求体
            byte[] payload = exchange.getRequestPayload();
            if (payload == null || payload.length == 0) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "请求体不能为空");
                return;
            }
            Map<String, Object> body;
            try {
                body = JsonUtils.parseObject(new String(payload), Map.class);
            } catch (Exception e) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "请求体 JSON 格式错误");
                return;
            }

            // 1.2 解析参数
            String productKey = MapUtil.getStr(body, "productKey");
            if (StrUtil.isEmpty(productKey)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "productKey 不能为空");
                return;
            }
            String deviceName = MapUtil.getStr(body, "deviceName");
            if (StrUtil.isEmpty(deviceName)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "deviceName 不能为空");
                return;
            }
            String productSecret = MapUtil.getStr(body, "productSecret");
            if (StrUtil.isEmpty(productSecret)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "productSecret 不能为空");
                return;
            }

            // 2. 调用动态注册
            IotDeviceRegisterReqDTO reqDTO = new IotDeviceRegisterReqDTO()
                    .setProductKey(productKey)
                    .setDeviceName(deviceName)
                    .setProductSecret(productSecret);
            CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(reqDTO);
            if (result.isError()) {
                log.warn("[handle][设备动态注册失败，productKey: {}, deviceName: {}, 错误: {}]",
                        productKey, deviceName, result.getMsg());
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST,
                        "设备动态注册失败：" + result.getMsg());
                return;
            }

            // 3. 返回成功响应
            log.info("[handle][设备动态注册成功，productKey: {}, deviceName: {}]", productKey, deviceName);
            IotCoapUtils.respondSuccess(exchange, result.getData());
        } catch (Exception e) {
            log.error("[handle][设备动态注册处理异常]", e);
            IotCoapUtils.respondError(exchange, CoAP.ResponseCode.INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

}
