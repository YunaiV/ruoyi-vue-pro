package cn.iocoder.yudao.module.iot.service.plugin;

import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import org.springframework.web.multipart.MultipartFile;

/**
 * IoT 插件实例 Service 接口
 *
 * @author 芋道源码
 */
public interface PluginInstanceService {

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
    void deletePluginFile(PluginInfoDO pluginInfoDo);

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
    void updatePluginStatus(PluginInfoDO pluginInfoDo, Integer status);

}