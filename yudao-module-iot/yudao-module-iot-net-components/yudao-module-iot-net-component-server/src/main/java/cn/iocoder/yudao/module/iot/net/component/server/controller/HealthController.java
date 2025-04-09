package cn.iocoder.yudao.module.iot.net.component.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查接口
 *
 * @author haohao
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查接口
     *
     * @return 返回服务状态信息
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("message", "IoT 网络组件服务运行正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
} 