package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.system.SystemUtil;

/**
 * IoT 核心模块的工具类
 *
 * @author 芋道源码
 */
public class IotCoreUtils {

    /**
     * 生成服务器编号
     *
     * @param serverPort 服务器端口
     * @return 服务器编号
     */
    public static String generateServerId(Integer serverPort) {
        String serverId = String.format("%s.%d", SystemUtil.getHostInfo().getAddress(), serverPort);
        // 避免一些场景无法使用 . 符号，例如说 RocketMQ Topic
        return serverId.replaceAll("\\.", "_");
    }

    public static String generateMessageId() {
        return IdUtil.fastSimpleUUID();
    }

}