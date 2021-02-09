package cn.iocoder.dashboard.modules.system.service.common;

import cn.iocoder.dashboard.modules.system.dal.dataobject.common.SysFileDO;

/**
 * 文件 Service 接口
 *
 * @author 芋道源码
 */
public interface SysFileService {

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param path 文件路径
     * @param content 文件内容
     * @return 文件路径
     */
    String createFile(String path, byte[] content);

    /**
     * 获得文件
     *
     * @param path 文件路径
     * @return 文件
     */
    SysFileDO getFile(String path);

}
