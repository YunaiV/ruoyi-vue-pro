package cn.iocoder.yudao.module.iot.service.plugin;

import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotPluginInstanceHeartbeatReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInstanceDO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * IoT 插件实例 Service 接口
 *
 * @author 芋道源码
 */
public interface IotPluginInstanceService {

    /**
     * 心跳插件实例
     *
     * @param heartbeatReqDTO 心跳插件实例 DTO
     */
    void heartbeatPluginInstance(IotPluginInstanceHeartbeatReqDTO heartbeatReqDTO);

    /**
     * 离线超时插件实例
     *
     * @param maxHeartbeatTime 最大心跳时间
     */
    int offlineTimeoutPluginInstance(LocalDateTime maxHeartbeatTime);

    /**
     * 停止并卸载插件
     *
     * @param pluginKey 插件标识符
     */
    void stopAndUnloadPlugin(String pluginKey);

    /**
     * 删除插件文件
     *
     * @param pluginConfigDO 插件配置
     */
    void deletePluginFile(IotPluginConfigDO pluginConfigDO);

    /**
     * 上传并加载新的插件文件
     *
     * @param file 插件文件
     * @return 插件标识符
     */
    String uploadAndLoadNewPlugin(MultipartFile file);

    /**
     * 更新插件状态
     *
     * @param pluginConfigDO 插件配置
     * @param status       新状态
     */
    void updatePluginStatus(IotPluginConfigDO pluginConfigDO, Integer status);

    // ========== 设备与插件的映射操作 ==========

    /**
     * 更新设备对应的插件实例的进程编号
     *
     * @param deviceKey 设备 Key
     * @param processId 进程编号
     */
    void updateDevicePluginInstanceProcessIdAsync(String deviceKey, String processId);

    /**
     * 获得设备对应的插件实例
     *
     * @param deviceKey 设备 Key
     * @return 插件实例
     */
    IotPluginInstanceDO getPluginInstanceByDeviceKey(String deviceKey);

}