package cn.iocoder.yudao.module.iot.service.plugin;

import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginInfoDO;
import org.springframework.web.multipart.MultipartFile;

/**
 * IoT 插件实例 Service 接口
 *
 * @author 芋道源码
 */
public interface IotPluginInstanceService {

    // TODO @芋艿：这个是否应该放到 plugin 主动心跳，而是 server 自己心跳
    /**
     * 上报插件实例（心跳）
     */
    void reportPluginInstances();

    /**
     * 停止并卸载插件
     *
     * @param pluginKey 插件标识符
     */
    void stopAndUnloadPlugin(String pluginKey);

    /**
     * 删除插件文件
     *
     * @param pluginInfoDo 插件信息
     */
    void deletePluginFile(IotPluginInfoDO pluginInfoDo);

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
     * @param pluginInfoDo 插件信息
     * @param status       新状态
     */
    void updatePluginStatus(IotPluginInfoDO pluginInfoDo, Integer status);

    // ========== 设备与插件的映射操作 ==========

    /**
     * 更新设备对应的插件实例的进程编号
     *
     * @param deviceKey 设备 Key
     * @param processId 进程编号
     */
    void updateDevicePluginInstanceProcessIdAsync(String deviceKey, String processId);

}