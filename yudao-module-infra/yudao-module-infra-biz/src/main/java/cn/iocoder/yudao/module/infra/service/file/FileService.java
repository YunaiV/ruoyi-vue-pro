package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;

/**
 * 文件 Service 接口
 *
 * @author 芋道源码
 */
public interface FileService {

    /**
     * 获得文件分页
     *
     * @param pageReqVO 分页查询
     * @return 文件分页
     */
    PageResult<FileDO> getFilePage(FilePageReqVO pageReqVO);

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param name 文件名称
     * @param path 文件路径
     * @param content 文件内容
     * @return 文件路径
     */
    String createFile(String name, String path, byte[] content);

    /**
     * 删除文件
     *
     * @param id 编号
     */
    void deleteFile(Long id) throws Exception;

    /**
     * 获得文件内容
     *
     * @param configId 配置编号
     * @param path 文件路径
     * @return 文件内容
     */
    byte[] getFileContent(Long configId, String path) throws Exception;

}
