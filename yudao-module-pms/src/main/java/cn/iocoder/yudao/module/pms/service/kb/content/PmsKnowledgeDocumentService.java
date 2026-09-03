package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentSearchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentUpdateReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * PMS 知识库文档 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeDocumentService {

    /**
     * 创建文档
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 文档编号
     */
    Long createDocument(PmsKnowledgeDocumentCreateReqVO createReqVO, Long userId);

    /**
     * 更新文档内容
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateDocument(PmsKnowledgeDocumentUpdateReqVO updateReqVO, Long userId);

    /**
     * 删除文档，并将文档和全部子文档移入回收站
     *
     * @param id 文档编号
     * @param userId 用户编号
     */
    void deleteDocument(Long id, Long userId);

    /**
     * 移动文档到目标目录，跨知识库移动时复制内容权限
     *
     * @param moveReqVO 移动信息
     * @param userId 用户编号
     */
    void moveDocument(PmsKnowledgeDocumentMoveReqVO moveReqVO, Long userId);

    /**
     * 获得文档，校验当前用户有内容读取权限
     *
     * @param id 文档编号
     * @param userId 用户编号
     * @return 文档
     */
    PmsKnowledgeDocumentDO getDocument(Long id, Long userId);

    /**
     * 获得文档
     *
     * @param id 文档编号
     * @return 文档
     */
    PmsKnowledgeDocumentDO getDocument(Long id);

    /**
     * 获得文档列表
     *
     * @param ids 文档编号集合
     * @return 文档列表
     */
    List<PmsKnowledgeDocumentDO> getDocumentList(Collection<Long> ids);

    /**
     * 获得文档 Map
     *
     * @param ids 文档编号集合
     * @return 文档编号到文档的 Map
     */
    default Map<Long, PmsKnowledgeDocumentDO> getDocumentMap(Collection<Long> ids) {
        return convertMap(getDocumentList(ids), PmsKnowledgeDocumentDO::getId);
    }

    /**
     * 获得知识库的全部文档列表
     *
     * @param libraryId 知识库编号
     * @return 文档列表
     */
    List<PmsKnowledgeDocumentDO> getDocumentListByLibraryId(Long libraryId);

    /**
     * 批量更新文档
     *
     * @param documents 文档列表
     */
    void updateDocumentList(Collection<PmsKnowledgeDocumentDO> documents);

    /**
     * 批量恢复文档
     *
     * @param ids 文档编号集合
     */
    void restoreDocumentList(Collection<Long> ids);

    /**
     * 批量彻底删除文档
     *
     * @param ids 文档编号集合
     */
    void deleteDocumentList(Collection<Long> ids);

    /**
     * 获得文件夹及其子文件夹下的全部文档
     *
     * @param folderIds 文件夹编号集合
     * @return 文档及其子文档列表
     */
    List<PmsKnowledgeDocumentDO> getDocumentListByFolderIds(Collection<Long> folderIds);

    /**
     * 更新文档移动到其他知识库时的归属和内容权限
     *
     * @param documents 文档列表
     * @param targetLibraryId 目标知识库编号
     * @param permissionIdMap 原权限编号到新权限编号的映射
     */
    void moveDocumentList(Collection<PmsKnowledgeDocumentDO> documents, Long targetLibraryId,
                          Map<Long, Long> permissionIdMap);

    /**
     * 使用 MySQL 搜索当前用户可读的文档（用户可按需二次开发，使用 Elasticsearch 检索）
     *
     * @param pageReqVO 分页搜索条件
     * @param userId 用户编号
     * @return 文档分页
     */
    PageResult<PmsKnowledgeDocumentDO> getDocumentSearchPage(
            PmsKnowledgeDocumentSearchPageReqVO pageReqVO, Long userId);

    /**
     * 获得知识库的正常文档列表，仅返回当前用户有内容读取权限的文档
     *
     * @param libraryId 知识库编号
     * @param userId 用户编号
     * @return 文档列表
     */
    List<PmsKnowledgeDocumentDO> getDocumentList(Long libraryId, Long userId);

    /**
     * 获得知识库文档类型数量 Map
     *
     * @param libraryIds 知识库编号集合
     * @return 知识库编号到文档类型数量 Map
     */
    Map<Long, Map<Integer, Long>> getDocumentTypeCountMap(Collection<Long> libraryIds);

    /**
     * 获得仍被文档引用的内容权限编号集合
     *
     * @param permissionIds 内容权限编号集合
     * @return 被引用的内容权限编号集合
     */
    Set<Long> getExistingContentPermissionIdSet(Collection<Long> permissionIds);

}
