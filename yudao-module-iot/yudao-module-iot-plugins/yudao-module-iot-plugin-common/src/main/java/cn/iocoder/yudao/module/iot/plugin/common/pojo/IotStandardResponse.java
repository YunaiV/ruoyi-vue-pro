package cn.iocoder.yudao.module.iot.plugin.common.pojo;

import lombok.Data;

// TODO @芋艿：1）后续考虑，要不要叫 IoT 网关之类的 Response；2）包名 pojo
/**
 * IoT 标准协议响应实体类
 * <p>
 * 用于统一 MQTT 和 HTTP 的响应格式
 *
 * @author haohao
 */
@Data
public class IotStandardResponse {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 方法名
     */
    private String method;

    /**
     * 协议版本
     */
    private String version;

    /**
     * 创建成功响应
     *
     * @param id     消息ID
     * @param method 方法名
     * @return 成功响应
     */
    public static IotStandardResponse success(String id, String method) {
        return success(id, method, null);
    }

    /**
     * 创建成功响应
     *
     * @param id     消息ID
     * @param method 方法名
     * @param data   响应数据
     * @return 成功响应
     */
    public static IotStandardResponse success(String id, String method, Object data) {
        return new IotStandardResponse()
                .setId(id)
                .setCode(200)
                .setData(data)
                .setMessage("success")
                .setMethod(method)
                .setVersion("1.0");
    }

    /**
     * 创建错误响应
     *
     * @param id      消息ID
     * @param method  方法名
     * @param code    错误码
     * @param message 错误消息
     * @return 错误响应
     */
    public static IotStandardResponse error(String id, String method, Integer code, String message) {
        return new IotStandardResponse()
                .setId(id)
                .setCode(code)
                .setData(null)
                .setMessage(message)
                .setMethod(method)
                .setVersion("1.0");
    }

}