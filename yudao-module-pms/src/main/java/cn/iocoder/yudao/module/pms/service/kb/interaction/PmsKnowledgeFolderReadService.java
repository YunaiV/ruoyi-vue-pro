package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 知识库文件夹读取 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeFolderReadService {

    /**
     * 获得当前用户可读的文件夹
     *
     * @param id 文件夹编号
     * @param userId 用户编号
     * @return 文件夹
     */
    PmsKnowledgeFolderDO getReadableFolder(Long id, Long userId);

    /**
     * 获得文件夹列表
     *
     * @param ids 文件夹编号集合
     * @return 文件夹列表
     */
    List<PmsKnowledgeFolderDO> getFolderList(Collection<Long> ids);

}
