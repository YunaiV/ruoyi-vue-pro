package cn.iocoder.yudao.adminserver.modules.infra.service.file;

import cn.iocoder.yudao.adminserver.modules.infra.controller.file.vo.InfFilePageReqVO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 文件 Service 接口
 *
 * @author 芋道源码
 */
public interface InfFileService {

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

    /**
     * 获得文件分页
     *
     * @param pageReqVO 分页查询
     * @return 文件分页
     */
    PageResult<InfFileDO> getFilePage(InfFilePageReqVO pageReqVO);

}
