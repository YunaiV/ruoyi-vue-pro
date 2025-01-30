package cn.iocoder.yudao.module.iot.plugin.common.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;

/**
 * IoT 插件的通用工具类
 *
 * 芋道源码
 */
public class IotPluginCommonUtils {

    /**
     * 流程实例的进程编号
     */
    private static String processId;

    public static String getProcessId() {
        if (StrUtil.isEmpty(processId)) {
            initProcessId();
        }
        return processId;
    }

    private synchronized static void initProcessId() {
        processId = String.format("%s@%d@%s", // IP@PID@${uuid}
                SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID(), IdUtil.fastSimpleUUID());
    }

}
