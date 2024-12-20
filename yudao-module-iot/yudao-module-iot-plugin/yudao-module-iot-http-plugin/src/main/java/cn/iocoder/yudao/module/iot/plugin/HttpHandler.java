package cn.iocoder.yudao.module.iot.plugin;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.api.DeviceDataApi;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * 基于 Netty 的 HTTP 处理器，用于接收设备上报的数据并调用主程序的 DeviceDataApi 接口进行处理。
 * <p>
 * 请求格式：
 * POST /sys/{productKey}/{deviceName}/thing/event/property/post
 * 请求体为 JSON 格式数据。
 * <p>
 * 返回结果为 JSON 格式，包含统一的 code、data、id、message、method、version 字段。
 */
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final DeviceDataApi deviceDataApi;

    public HttpHandler(DeviceDataApi deviceDataApi) {
        this.deviceDataApi = deviceDataApi;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();

        // 期望的路径格式: /sys/{productKey}/{deviceName}/thing/event/property/post
        // 使用 "/" 拆分路径
        String[] parts = uri.split("/");

        /*
          拆分结果示例:
          parts[0] = ""
          parts[1] = "sys"
          parts[2] = productKey
          parts[3] = deviceName
          parts[4] = "thing"
          parts[5] = "event"
          parts[6] = "property"
          parts[7] = "post"
         */
        boolean isCorrectPath = parts.length == 8
                && "sys".equals(parts[1])
                && "thing".equals(parts[4])
                && "event".equals(parts[5])
                && "property".equals(parts[6])
                && "post".equals(parts[7]);

        if (!isCorrectPath) {
            // 如果路径不匹配，返回 404 错误
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "Not Found");
            return;
        }

        String productKey = parts[2];
        String deviceName = parts[3];

        // 从请求中获取原始数据
        String requestBody = request.content().toString(CharsetUtil.UTF_8);

        // 尝试解析请求数据为 JSON 对象
        JSONObject jsonData;
        try {
            jsonData = JSONUtil.parseObj(requestBody);
        } catch (Exception e) {
            // 数据不是合法的 JSON 格式，返回 400 错误
            JSONObject res = createResponseJson(
                    400,
                    new JSONObject(),
                    null,
                    "请求数据不是合法的 JSON 格式: " + e.getMessage(),
                    "thing.event.property.post",
                    "1.0"
            );
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, res.toString());
            return;
        }

        // 获取请求中的 id 字段，若不存在则为 null
        String id = jsonData.getStr("id", null);

        try {
            // 调用主程序的接口保存数据
            deviceDataApi.saveDeviceData(productKey, deviceName, jsonData.toString());

            // 构造成功响应内容
            JSONObject successRes = createResponseJson(
                    200,
                    new JSONObject(),
                    id,
                    "success",
                    "thing.event.property.post",
                    "1.0"
            );
            writeResponse(ctx, HttpResponseStatus.OK, successRes.toString());
        } catch (Exception e) {
            // 保存数据过程中出现异常，返回 500 错误
            JSONObject errorRes = createResponseJson(
                    500,
                    new JSONObject(),
                    id,
                    "The format of result is error!",
                    "thing.event.property.post",
                    "1.0"
            );
            writeResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, errorRes.toString());
        }
    }

    /**
     * 创建标准化的响应 JSON 对象
     *
     * @param code    响应状态码（业务层面的）
     * @param data    返回的数据对象（JSON）
     * @param id      请求的 id（可选）
     * @param message 返回的提示信息
     * @param method  返回的 method 标识
     * @param version 返回的版本号
     * @return 构造好的 JSON 对象
     */
    private JSONObject createResponseJson(int code, JSONObject data, String id, String message, String method, String version) {
        JSONObject res = new JSONObject();
        res.set("code", code);
        res.set("data", data != null ? data : new JSONObject()); // 确保 data 不为 null
        res.set("id", id);
        res.set("message", message);
        res.set("method", method);
        res.set("version", version);
        return res;
    }

    /**
     * 向客户端返回 HTTP 响应的辅助方法。
     *
     * @param ctx     通道上下文
     * @param status  HTTP 响应状态码（网络层面的）
     * @param content 响应内容（JSON 字符串或其他文本）
     */
    private void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
        );

        // 设置响应头为 JSON 类型和正确的编码
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        // 发送响应并在发送完成后关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}