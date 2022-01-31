package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;

/**
 * core service 文件接口
 *
 * @author 宋天
 */
public interface InfFileCoreService {

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param path 文件路径
     * @param content 文件内容
     * @return 文件路径
     */
    String createFile(String path, byte[] content);

    /**
     * 删除文件
     *
     * @param id 编号
     */
    void deleteFile(String id);

    /**
     * 获得文件
     *
     * @param path 文件路径
     * @return 文件
     */
    InfFileDO getFile(String path);
}
