package cn.iocoder.yudao.module.iot.protocol.example;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.protocol.util.IotHttpTopicUtils;

/**
 * 阿里云IoT平台HTTPS协议示例
 * <p>
 * 参考阿里云IoT平台HTTPS连接通信标准，演示设备认证和数据上报的完整流程
 *
 * @author haohao
 */
public class AliyunHttpProtocolExample {

    public static void main(String[] args) {
        System.out.println("=== 阿里云IoT平台HTTPS协议演示 ===\n");

        // 演示设备认证
        demonstrateDeviceAuth();

        // 演示数据上报
        demonstrateDataUpload();

        // 演示路径构建
        demonstratePathBuilding();
    }

    /**
     * 演示设备认证流程
     */
    private static void demonstrateDeviceAuth() {
        System.out.println("1. 设备认证流程:");
        System.out.println("认证路径: " + IotHttpTopicUtils.buildAuthPath());

        // 构建认证请求消息
        JSONObject authRequest = new JSONObject();
        authRequest.set("productKey", "a1GFjLP****");
        authRequest.set("deviceName", "device123");
        authRequest.set("clientId", "device123_001");
        authRequest.set("timestamp", String.valueOf(System.currentTimeMillis()));
        authRequest.set("sign", "4870141D4067227128CBB4377906C3731CAC221C");
        authRequest.set("signmethod", "hmacsha1");
        authRequest.set("version", "default");

        System.out.println("认证请求消息:");
        System.out.println(authRequest.toString());

        // 模拟认证响应
        JSONObject authResponse = new JSONObject();
        authResponse.set("code", 0);
        authResponse.set("message", "success");

        JSONObject info = new JSONObject();
        info.set("token", "6944e5bfb92e4d4ea3918d1eda39****");
        authResponse.set("info", info);

        System.out.println("认证响应:");
        System.out.println(authResponse.toString());
        System.out.println();
    }

    /**
     * 演示数据上报流程
     */
    private static void demonstrateDataUpload() {
        System.out.println("2. 数据上报流程:");

        String productKey = "a1GFjLP****";
        String deviceName = "device123";

        // 属性上报
        String propertyPostPath = IotHttpTopicUtils.buildPropertyPostPath(productKey, deviceName);
        System.out.println("属性上报路径: " + propertyPostPath);

        // Alink格式的属性上报消息
        JSONObject propertyMessage = new JSONObject();
        propertyMessage.set("id", "123456");
        propertyMessage.set("version", "1.0");
        propertyMessage.set("method", "thing.event.property.post");

        JSONObject propertyParams = new JSONObject();
        JSONObject properties = new JSONObject();
        properties.set("temperature", 25.6);
        properties.set("humidity", 60.3);
        propertyParams.set("properties", properties);
        propertyMessage.set("params", propertyParams);

        System.out.println("属性上报消息:");
        System.out.println(propertyMessage.toString());

        // 事件上报
        String eventPostPath = IotHttpTopicUtils.buildEventPostPath(productKey, deviceName, "temperatureAlert");
        System.out.println("\n事件上报路径: " + eventPostPath);

        JSONObject eventMessage = new JSONObject();
        eventMessage.set("id", "123457");
        eventMessage.set("version", "1.0");
        eventMessage.set("method", "thing.event.temperatureAlert.post");

        JSONObject eventParams = new JSONObject();
        eventParams.set("value", new JSONObject().set("alertLevel", "high").set("currentTemp", 45.2));
        eventParams.set("time", System.currentTimeMillis());
        eventMessage.set("params", eventParams);

        System.out.println("事件上报消息:");
        System.out.println(eventMessage.toString());

        // 模拟数据上报响应
        JSONObject uploadResponse = new JSONObject();
        uploadResponse.set("code", 0);
        uploadResponse.set("message", "success");

        JSONObject responseInfo = new JSONObject();
        responseInfo.set("messageId", 892687470447040L);
        uploadResponse.set("info", responseInfo);

        System.out.println("\n数据上报响应:");
        System.out.println(uploadResponse.toString());
        System.out.println();
    }

    /**
     * 演示路径构建功能
     */
    private static void demonstratePathBuilding() {
        System.out.println("3. 路径构建功能:");

        String productKey = "smartProduct";
        String deviceName = "sensor001";

        // 系统主题路径
        System.out.println("系统主题路径:");
        System.out.println("  属性设置: " + IotHttpTopicUtils.buildPropertySetPath(productKey, deviceName));
        System.out.println("  属性获取: " + IotHttpTopicUtils.buildPropertyGetPath(productKey, deviceName));
        System.out.println("  属性上报: " + IotHttpTopicUtils.buildPropertyPostPath(productKey, deviceName));
        System.out.println("  事件上报: " + IotHttpTopicUtils.buildEventPostPath(productKey, deviceName, "alarm"));
        System.out.println("  服务调用: " + IotHttpTopicUtils.buildServiceInvokePath(productKey, deviceName, "reboot"));

        // 自定义主题路径
        System.out.println("\n自定义主题路径:");
        System.out.println("  用户主题: " + IotHttpTopicUtils.buildCustomTopicPath(productKey, deviceName, "user/get"));

        // 响应路径
        String requestPath = IotHttpTopicUtils.buildPropertySetPath(productKey, deviceName);
        String replyPath = IotHttpTopicUtils.buildReplyPath(requestPath);
        System.out.println("\n响应路径:");
        System.out.println("  请求路径: " + requestPath);
        System.out.println("  响应路径: " + replyPath);

        // 路径解析
        System.out.println("\n路径解析:");
        String testPath = "/topic/sys/smartProduct/sensor001/thing/service/property/set";
        String actualTopic = IotHttpTopicUtils.extractActualTopic(testPath);
        System.out.println("  HTTP路径: " + testPath);
        System.out.println("  实际主题: " + actualTopic);
        System.out.println("  产品Key: " + IotHttpTopicUtils.parseProductKeyFromTopic(actualTopic));
        System.out.println("  设备名称: " + IotHttpTopicUtils.parseDeviceNameFromTopic(actualTopic));
        System.out.println("  是否为系统主题: " + IotHttpTopicUtils.isSystemTopic(actualTopic));

        // 路径类型检查
        System.out.println("\n路径类型检查:");
        System.out.println("  认证路径检查: " + IotHttpTopicUtils.isAuthPath("/auth"));
        System.out.println("  数据路径检查: " + IotHttpTopicUtils.isTopicPath("/topic/test"));
        System.out.println("  有效路径检查: " + IotHttpTopicUtils.isValidHttpPath("/topic/sys/test/device/property"));
    }
}