package cn.iocoder.yudao.module.pms.service.kb.recycle;

import cn.iocoder.yudao.module.pms.controller.admin.kb.recycle.vo.PmsKnowledgeRecycleDetailRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.recycle.PmsKnowledgeRecycleRecordDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PMS 知识库回收站 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeRecycleService {

    /**
     * 将知识库及其全部内容移入回收站，只记录本次显式删除的知识库
     *
     * @param library 知识库
     * @param userId 用户编号
     */
    void recycleLibrary(PmsKnowledgeLibraryDO library, Long userId);

    /**
     * 将文件夹子树移入回收站，只记录本次显式删除的根文件夹
     *
     * @param folder 根文件夹
     * @param folders 根文件夹及其子文件夹列表
     * @param documents 文件夹子树中的文档列表
     * @param userId 用户编号
     */
    void recycleFolder(PmsKnowledgeFolderDO folder, List<PmsKnowledgeFolderDO> folders,
                       List<PmsKnowledgeDocumentDO> documents, Long userId);

    /**
     * 将文档及全部子文档移入回收站，只记录本次显式删除的根文档
     *
     * @param document 根文档
     * @param documents 根文档及其子文档列表
     * @param userId 用户编号
     */
    void recycleDocument(PmsKnowledgeDocumentDO document, List<PmsKnowledgeDocumentDO> documents, Long userId);

    /**
     * 获得当前用户删除的知识库回收站记录
     *
     * @param userId 用户编号
     * @return 回收站记录列表
     */
    List<PmsKnowledgeRecycleRecordDO> getLibraryRecycleList(Long userId);

    /**
     * 获得知识库的内容回收站记录，只返回当前用户有内容删除权限的记录
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 回收站记录列表
     */
    List<PmsKnowledgeRecycleRecordDO> getContentRecycleList(Long libraryId, Long userId);

    /**
     * 获得知识库回收站对象及其级联删除内容
     *
     * @param recordId 回收站记录编号
     * @param userId 用户编号
     * @return 回收站详情
     */
    PmsKnowledgeRecycleDetailRespVO getContentRecycleDetail(Long recordId, Long userId);

    /**
     * 获得回收站内容预览数据
     *
     * @param recordId 回收站记录编号
     * @param entityId 预览的级联内容编号，可为空
     * @param userId 用户编号
     * @return 内容预览数据
     */
    PmsKnowledgeDocumentDO getContentRecyclePreview(Long recordId, Long entityId, Long userId);

    /**
     * 按类型恢复回收站记录对应的知识库或内容，保留仍被单独删除的子树
     *
     * @param recordId 回收站记录编号
     * @param userId 用户编号
     */
    void restoreContentRecycle(Long recordId, Long userId);

    /**
     * 按类型彻底删除回收站记录对应的知识库或内容及其关联数据
     *
     * @param recordId 回收站记录编号
     * @param userId 用户编号
     */
    void deleteContentRecycle(Long recordId, Long userId);

    /**
     * 彻底清理指定时间前的回收站记录，不校验用户权限
     *
     * @param deleteTime 删除截止时间
     * @return 处理的回收站记录数量
     */
    int deleteExpiredRecycleRecords(LocalDateTime deleteTime);

}
