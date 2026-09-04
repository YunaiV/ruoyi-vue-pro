package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.viewrecord.PmsKnowledgeRecentListRespVO;

import java.util.Collection;

/**
 * PMS 知识浏览记录 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeViewRecordService {

    /**
     * 记录对象的最近浏览时间
     *
     * @param libraryId 知识库编号
     * @param type 对象类型
     * @param entityId 对象编号
     * @param userId 用户编号
     */
    void createViewRecord(Long libraryId, Integer type, Long entityId, Long userId);

    /**
     * 获得最近浏览列表
     *
     * @param libraryId 知识库编号，为空时查询全部可读知识库
     * @param userId 用户编号
     * @return 最近浏览列表
     */
    PmsKnowledgeRecentListRespVO getRecentViewRecordList(Long libraryId, Long userId);

    /**
     * 删除知识库的浏览记录
     *
     * @param libraryId 知识库编号
     */
    void deleteViewRecordsByLibraryId(Long libraryId);

    /**
     * 删除文件夹和文档的浏览记录
     *
     * @param folderIds 文件夹编号集合
     * @param documentIds 文档编号集合
     */
    void deleteViewRecordsByEntityIds(Collection<Long> folderIds, Collection<Long> documentIds);

    /**
     * 更新文件夹和文档浏览记录的所属知识库
     *
     * @param folderIds 文件夹编号集合
     * @param documentIds 文档编号集合
     * @param libraryId 目标知识库编号
     */
    void updateViewRecordLibraryIdByEntityIds(Collection<Long> folderIds, Collection<Long> documentIds,
                                              Long libraryId);

}
