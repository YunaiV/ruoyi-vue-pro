package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * PMS 知识库文件夹 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeFolderService {

    /**
     * 创建文件夹
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @return 文件夹编号
     */
    Long createFolder(PmsKnowledgeFolderSaveReqVO saveReqVO, Long userId);

    /**
     * 更新文件夹名称
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     */
    void updateFolder(PmsKnowledgeFolderSaveReqVO saveReqVO, Long userId);

    /**
     * 删除文件夹，并将文件夹子树移入回收站
     *
     * @param id 文件夹编号
     * @param userId 用户编号
     */
    void deleteFolder(Long id, Long userId);

    /**
     * 移动文件夹到目标目录，跨知识库移动时复制内容权限
     *
     * @param moveReqVO 移动信息
     * @param userId 用户编号
     */
    void moveFolder(PmsKnowledgeFolderMoveReqVO moveReqVO, Long userId);

    /**
     * 获得文件夹，校验当前用户有内容读取权限
     *
     * @param id 文件夹编号
     * @param userId 用户编号
     * @return 文件夹
     */
    PmsKnowledgeFolderDO getFolder(Long id, Long userId);

    /**
     * 获得文件夹
     *
     * @param id 文件夹编号
     * @return 文件夹
     */
    PmsKnowledgeFolderDO getFolder(Long id);

    /**
     * 获得指定编号的文件夹列表
     *
     * @param ids 文件夹编号集合
     * @return 文件夹列表
     */
    List<PmsKnowledgeFolderDO> getFolderList(Collection<Long> ids);

    /**
     * 获得知识库的全部文件夹列表
     *
     * @param libraryId 知识库编号
     * @return 文件夹列表
     */
    List<PmsKnowledgeFolderDO> getFolderListByLibraryId(Long libraryId);

    /**
     * 批量更新文件夹
     *
     * @param folders 文件夹列表
     */
    void updateFolderList(Collection<PmsKnowledgeFolderDO> folders);

    /**
     * 批量恢复文件夹
     *
     * @param ids 文件夹编号集合
     */
    void restoreFolderList(Collection<Long> ids);

    /**
     * 批量彻底删除文件夹
     *
     * @param ids 文件夹编号集合
     */
    void deleteFolderList(Collection<Long> ids);

    /**
     * 获得知识库的文件夹列表，仅返回当前用户有内容读取权限的文件夹
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 文件夹列表
     */
    List<PmsKnowledgeFolderDO> getFolderList(Long libraryId, Long userId);

    /**
     * 获得仍被文件夹引用的内容权限编号集合
     *
     * @param permissionIds 内容权限编号集合
     * @return 被引用的内容权限编号集合
     */
    Set<Long> getExistingContentPermissionIdSet(Collection<Long> permissionIds);

}
