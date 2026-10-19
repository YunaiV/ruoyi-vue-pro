package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentSearchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentUpdateReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeViewRecordService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.pms.service.kb.recycle.PmsKnowledgeRecycleService;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_FOLDER_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_MOVE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_MOVE_TARGET_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_PARENT_INVALID;

/**
 * PMS 知识库文档 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeDocumentServiceImpl implements PmsKnowledgeDocumentService {

    @Resource
    private PmsKnowledgeDocumentMapper documentMapper;

    @Resource
    private PmsKnowledgeFolderService folderService;
    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @Resource
    private PmsKnowledgeDocumentLabelService documentLabelService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeFavoriteService favoriteService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeViewRecordService viewRecordService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeRecycleService recycleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDocument(PmsKnowledgeDocumentCreateReqVO createReqVO, Long userId) {
        // 1.1 校验知识库读取权限
        libraryMemberService.validateLibraryReadable(createReqVO.getLibraryId(), userId);
        // 1.2 规范并校验目录位置，同时确定文档权限
        PmsKnowledgeDocumentDO document = BeanUtils.toBean(createReqVO, PmsKnowledgeDocumentDO.class);
        Long permissionId = normalizeAndValidateDocumentPlacement(document, null, userId);
        if (permissionId == null) {
            permissionId = contentPermissionService.createDefaultContentPermission(createReqVO.getLibraryId(), userId);
        }

        // 2. 创建文档
        document.setPermissionId(permissionId).setType(createReqVO.getType())
                .setStatus(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
        documentMapper.insert(document);
        return document.getId();
    }

    @Override
    public void updateDocument(PmsKnowledgeDocumentUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验文档存在
        PmsKnowledgeDocumentDO document = validateDocumentExists(updateReqVO.getId());
        // 1.2 校验所属知识库可读
        libraryMemberService.validateLibraryReadable(document.getLibraryId(), userId);
        // 1.3 校验内容编辑权限
        contentPermissionService.validateContentPermissionWritable(document.getPermissionId(), document.getLibraryId(), userId);
        // 1.4 校验标签均存在
        documentLabelService.validateDocumentLabelList(updateReqVO.getLabelIds());

        // 2. 更新文档内容
        documentMapper.updateById(BeanUtils.toBean(updateReqVO, PmsKnowledgeDocumentDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id, Long userId) {
        // 1.1 校验文档存在
        PmsKnowledgeDocumentDO document = validateDocumentExists(id);
        // 1.2 校验所属知识库可读
        libraryMemberService.validateLibraryReadable(document.getLibraryId(), userId);
        // 1.3 校验文档内容删除权限
        contentPermissionService.validateContentPermissionDeletable(document.getPermissionId(), document.getLibraryId(), userId);

        // 2. 将文档和全部子文档移入回收站
        recycleService.recycleDocument(document, getDocumentDescendantList(document), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveDocument(PmsKnowledgeDocumentMoveReqVO moveReqVO, Long userId) {
        // 1.1 校验源文档存在
        PmsKnowledgeDocumentDO document = validateDocumentExists(moveReqVO.getId());
        // 1.2 校验移动目标只能选择一种容器
        if (notEqual(PmsKnowledgeDocumentDO.FOLDER_ID_ROOT, moveReqVO.getTargetFolderId())
                && notEqual(PmsKnowledgeDocumentDO.PARENT_ID_ROOT, moveReqVO.getTargetParentId())) {
            throw exception(KNOWLEDGE_DOCUMENT_MOVE_TARGET_INVALID);
        }
        // 1.3 校验源知识库和目标知识库可读
        libraryMemberService.validateLibraryReadable(document.getLibraryId(), userId);
        libraryMemberService.validateLibraryReadable(moveReqVO.getTargetLibraryId(), userId);
        // 1.4 移动会改变目录结构，校验源文档管理权限
        contentPermissionService.validateContentPermissionManageable(
                document.getPermissionId(), document.getLibraryId(), userId);
        // 1.5 规范并校验目标位置，禁止移动到自身或子文档
        PmsKnowledgeDocumentDO placement = new PmsKnowledgeDocumentDO()
                .setLibraryId(moveReqVO.getTargetLibraryId()).setFolderId(moveReqVO.getTargetFolderId())
                .setParentId(moveReqVO.getTargetParentId());
        normalizeAndValidateDocumentPlacement(placement, document, userId);

        // 2.1 获得文档子树
        List<PmsKnowledgeDocumentDO> documents = getDocumentDescendantList(document);
        // 2.2 跨知识库移动时复制权限，避免影响原知识库内容
        Set<Long> sourcePermissionIds = convertSet(documents, PmsKnowledgeDocumentDO::getPermissionId);
        boolean crossLibrary = notEqual(document.getLibraryId(), moveReqVO.getTargetLibraryId());
        Map<Long, Long> clonedPermissionIdMap = !crossLibrary
                ? Collections.emptyMap()
                : contentPermissionService.cloneContentPermissions(sourcePermissionIds, moveReqVO.getTargetLibraryId());

        // 3. 更新文档位置，并同步全部子文档所属知识库与权限
        for (PmsKnowledgeDocumentDO item : documents) {
            item.setLibraryId(moveReqVO.getTargetLibraryId());
            if (CollUtil.isNotEmpty(clonedPermissionIdMap)) {
                item.setPermissionId(clonedPermissionIdMap.get(item.getPermissionId()));
            }
            if (item.getId().equals(document.getId())) {
                item.setFolderId(placement.getFolderId()).setParentId(placement.getParentId());
            }
        }
        if (CollUtil.isNotEmpty(documents)) {
            documentMapper.updateBatch(documents);
        }

        // 4. 跨知识库移动时同步关注和最近浏览记录的当前归属
        if (crossLibrary) {
            Set<Long> documentIds = convertSet(documents, PmsKnowledgeDocumentDO::getId);
            favoriteService.updateFavoriteLibraryIdByEntityIds(Collections.emptySet(), documentIds, moveReqVO.getTargetLibraryId());
            viewRecordService.updateViewRecordLibraryIdByEntityIds(Collections.emptySet(), documentIds, moveReqVO.getTargetLibraryId());
        }
        if (CollUtil.isNotEmpty(clonedPermissionIdMap)) {
            contentPermissionService.deleteUnusedContentPermissions(sourcePermissionIds);
        }
    }

    @Override
    public PmsKnowledgeDocumentDO getDocument(Long id, Long userId) {
        // 1. 校验文档存在
        PmsKnowledgeDocumentDO document = validateDocumentExists(id);

        // 2.1 校验知识库读取权限
        libraryMemberService.validateLibraryReadable(document.getLibraryId(), userId);
        // 2.2 校验内容读取权限
        contentPermissionService.validateContentPermissionReadable(document.getPermissionId(), document.getLibraryId(), userId);
        return document;
    }

    @Override
    public PmsKnowledgeDocumentDO getDocument(Long id) {
        return documentMapper.selectById(id);
    }

    @Override
    public List<PmsKnowledgeDocumentDO> getDocumentList(Collection<Long> ids) {
        return CollUtil.isEmpty(ids) ? Collections.emptyList() : documentMapper.selectByIds(ids);
    }

    @Override
    public List<PmsKnowledgeDocumentDO> getDocumentListByLibraryId(Long libraryId) {
        return documentMapper.selectListByLibraryId(libraryId);
    }

    @Override
    public void updateDocumentList(Collection<PmsKnowledgeDocumentDO> documents) {
        if (CollUtil.isEmpty(documents)) {
            return;
        }
        documentMapper.updateBatch(documents);
    }

    @Override
    public void restoreDocumentList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        documentMapper.updateToRestoreByIds(ids, PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
    }

    @Override
    public void deleteDocumentList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        documentMapper.deleteByIds(ids);
    }

    @Override
    public List<PmsKnowledgeDocumentDO> getDocumentListByFolderIds(Collection<Long> folderIds) {
        // 1. 查询文件夹直接包含的根文档
        if (CollUtil.isEmpty(folderIds)) {
            return Collections.emptyList();
        }
        List<PmsKnowledgeDocumentDO> roots = documentMapper.selectListByFolderIds(folderIds);
        if (CollUtil.isEmpty(roots)) {
            return Collections.emptyList();
        }
        List<PmsKnowledgeDocumentDO> documents = new ArrayList<>(roots);
        Set<Long> visitedDocumentIds = new LinkedHashSet<>(convertSet(roots, PmsKnowledgeDocumentDO::getId));
        Set<Long> parentIds = convertSet(roots, PmsKnowledgeDocumentDO::getId);

        // 2. 逐层查询子文档；使用深度上限和已访问集合共同防御异常循环数据
        for (int depth = 0; depth < Short.MAX_VALUE && CollUtil.isNotEmpty(parentIds); depth++) {
            List<PmsKnowledgeDocumentDO> childDocuments = documentMapper.selectListByParentIds(parentIds);
            List<PmsKnowledgeDocumentDO> newChildDocuments = new ArrayList<>();
            for (PmsKnowledgeDocumentDO childDocument : childDocuments) {
                if (visitedDocumentIds.add(childDocument.getId())) {
                    newChildDocuments.add(childDocument);
                }
            }
            if (CollUtil.isEmpty(newChildDocuments)) {
                break;
            }
            documents.addAll(newChildDocuments);
            parentIds = convertSet(newChildDocuments, PmsKnowledgeDocumentDO::getId);
        }
        return documents;
    }

    @Override
    public void moveDocumentList(Collection<PmsKnowledgeDocumentDO> documents, Long targetLibraryId,
                                 Map<Long, Long> permissionIdMap) {
        if (CollUtil.isEmpty(documents)) {
            return;
        }
        for (PmsKnowledgeDocumentDO document : documents) {
            document.setLibraryId(targetLibraryId);
            if (CollUtil.isNotEmpty(permissionIdMap)) {
                document.setPermissionId(permissionIdMap.get(document.getPermissionId()));
            }
        }
        documentMapper.updateBatch(documents);
    }

    @Override
    public PageResult<PmsKnowledgeDocumentDO> getDocumentSearchPage(
            PmsKnowledgeDocumentSearchPageReqVO pageReqVO, Long userId) {
        // 1. 查询当前用户可读的知识库和内容权限
        List<Long> readableLibraryIds = libraryMemberService.getReadableLibraryIdList(userId);
        if (CollUtil.isEmpty(readableLibraryIds)
                || pageReqVO.getLibraryId() != null && !readableLibraryIds.contains(pageReqVO.getLibraryId())) {
            return PageResult.empty();
        }
        Set<Long> readablePermissionIds = contentPermissionService.getReadableContentPermissionIdSet(readableLibraryIds, userId);
        if (CollUtil.isEmpty(readablePermissionIds)) {
            return PageResult.empty();
        }

        // 2. 使用 MySQL 对标题和正文进行模糊搜索
        return documentMapper.selectPage(pageReqVO, readableLibraryIds, readablePermissionIds,
                PmsKnowledgeDocumentStatusEnum.ACTIVE_STATUSES);
    }

    @Override
    public List<PmsKnowledgeDocumentDO> getDocumentList(Long libraryId, Long userId) {
        // 1. 校验知识库可读
        libraryMemberService.validateLibraryReadable(libraryId, userId);

        // 2. 查询全部正常文档
        List<PmsKnowledgeDocumentDO> documents = documentMapper.selectListByLibraryIdAndStatus(
                libraryId, PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
        if (CollUtil.isEmpty(documents)) {
            return Collections.emptyList();
        }

        // 3. 按内容权限过滤，只返回当前用户有读取权限的文档
        Map<Long, Integer> permissionLevelMap = contentPermissionService.getCurrentUserContentPermissionLevelMap(
                convertSet(documents, PmsKnowledgeDocumentDO::getPermissionId), libraryId, userId);
        documents.removeIf(document -> permissionLevelMap.get(document.getPermissionId()) == null);

        // 4. 父文档不可见时，整条子文档分支均不可见
        Map<Long, List<Long>> childDocumentIdMap = new LinkedHashMap<>();
        Set<Long> visibleDocumentIds = new LinkedHashSet<>();
        Deque<Long> pendingDocumentIds = new ArrayDeque<>();
        for (PmsKnowledgeDocumentDO document : documents) {
            if (PmsKnowledgeDocumentDO.PARENT_ID_ROOT.equals(document.getParentId())) {
                visibleDocumentIds.add(document.getId());
                pendingDocumentIds.add(document.getId());
            } else {
                childDocumentIdMap.computeIfAbsent(document.getParentId(), key -> new ArrayList<>())
                        .add(document.getId());
            }
        }
        while (CollUtil.isNotEmpty(pendingDocumentIds)) {
            Long parentId = pendingDocumentIds.removeFirst();
            for (Long childId : childDocumentIdMap.getOrDefault(parentId, Collections.emptyList())) {
                if (visibleDocumentIds.add(childId)) {
                    pendingDocumentIds.addLast(childId);
                }
            }
        }
        documents.removeIf(document -> !visibleDocumentIds.contains(document.getId()));
        return documents;
    }

    @Override
    public Map<Long, Map<Integer, Long>> getDocumentTypeCountMap(Collection<Long> libraryIds) {
        return CollUtil.isEmpty(libraryIds) ? Collections.emptyMap()
                : documentMapper.selectTypeCountMapByLibraryIdsAndStatus(
                        libraryIds, PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
    }

    private PmsKnowledgeDocumentDO validateDocumentExists(Long id) {
        PmsKnowledgeDocumentDO document = documentMapper.selectById(id);
        if (document == null || PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus().equals(document.getStatus())) {
            throw exception(KNOWLEDGE_DOCUMENT_NOT_EXISTS);
        }
        return document;
    }

    /**
     * 规范并校验文档目录位置，返回继承的内容权限编号
     *
     * @param placement 文档位置
     * @param document 待移动文档；创建时为空
     * @param userId 用户编号
     * @return 继承的内容权限编号；位于知识库根目录时返回 {@code null}
     */
    private Long normalizeAndValidateDocumentPlacement(PmsKnowledgeDocumentDO placement,
                                                        PmsKnowledgeDocumentDO document,
                                                        Long userId) {
        // 1. 文件夹优先于父文档，文档放入文件夹根节点并继承文件夹权限
        if (placement.getFolderId() != null
                && notEqual(PmsKnowledgeDocumentDO.FOLDER_ID_ROOT, placement.getFolderId())) {
            placement.setParentId(PmsKnowledgeDocumentDO.PARENT_ID_ROOT);
            PmsKnowledgeFolderDO folder = folderService.getFolder(placement.getFolderId(), userId);
            if (notEqual(placement.getLibraryId(), folder.getLibraryId())
                    || notEqual(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus(), folder.getStatus())) {
                throw exception(KNOWLEDGE_DOCUMENT_FOLDER_INVALID);
            }
            if (document == null) {
                contentPermissionService.validateContentPermissionWritable(
                        folder.getPermissionId(), folder.getLibraryId(), userId);
            } else {
                contentPermissionService.validateContentPermissionManageable(
                        folder.getPermissionId(), folder.getLibraryId(), userId);
            }
            return folder.getPermissionId();
        }

        // 2. 没有指定文件夹和父文档时，规范到知识库根目录
        placement.setFolderId(PmsKnowledgeDocumentDO.FOLDER_ID_ROOT);
        if (placement.getParentId() == null
                || PmsKnowledgeDocumentDO.PARENT_ID_ROOT.equals(placement.getParentId())) {
            placement.setParentId(PmsKnowledgeDocumentDO.PARENT_ID_ROOT);
            if (document == null) {
                libraryMemberService.validateLibraryWritable(placement.getLibraryId(), userId);
            } else {
                libraryMemberService.validateLibraryAdmin(placement.getLibraryId(), userId);
            }
            return null;
        }

        // 3.1 校验父文档存在
        PmsKnowledgeDocumentDO parent = documentMapper.selectById(placement.getParentId());
        if (parent == null) {
            throw exception(KNOWLEDGE_DOCUMENT_PARENT_INVALID);
        }
        // 3.2 校验父文档属于目标知识库
        if (notEqual(placement.getLibraryId(), parent.getLibraryId())) {
            throw exception(KNOWLEDGE_DOCUMENT_PARENT_INVALID);
        }
        // 3.3 校验父文档处于正常状态
        if (notEqual(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus(), parent.getStatus())) {
            throw exception(KNOWLEDGE_DOCUMENT_PARENT_INVALID);
        }
        // 3.4 校验文档不能移动到自身或子文档下
        if (document != null) {
            List<PmsKnowledgeDocumentDO> documents = getDocumentDescendantList(document);
            Set<Long> documentIds = convertSet(documents, PmsKnowledgeDocumentDO::getId);
            if (documentIds.contains(placement.getParentId())) {
                throw exception(KNOWLEDGE_DOCUMENT_MOVE_INVALID);
            }
        }
        // 3.5 创建时校验可编辑，移动时校验目标父文档管理权限
        if (document == null) {
            contentPermissionService.validateContentPermissionWritable(
                    parent.getPermissionId(), parent.getLibraryId(), userId);
        } else {
            contentPermissionService.validateContentPermissionManageable(
                    parent.getPermissionId(), parent.getLibraryId(), userId);
        }
        return parent.getPermissionId();
    }

    /**
     * 获得根文档及全部子文档列表
     *
     * @param root 根文档
     * @return 根文档及全部子文档列表
     */
    private List<PmsKnowledgeDocumentDO> getDocumentDescendantList(PmsKnowledgeDocumentDO root) {
        // 1. 初始化结果、已访问文档编号和首层父文档编号
        List<PmsKnowledgeDocumentDO> documents = new ArrayList<>();
        documents.add(root);
        Set<Long> visitedDocumentIds = new LinkedHashSet<>();
        visitedDocumentIds.add(root.getId());
        Collection<Long> parentIds = Collections.singleton(root.getId());

        // 2. 逐层查询子文档，并通过已访问文档编号避免异常环路和重复数据
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            List<PmsKnowledgeDocumentDO> childDocuments = documentMapper.selectListByParentIds(parentIds);
            childDocuments.removeIf(child -> !visitedDocumentIds.add(child.getId()));
            if (CollUtil.isEmpty(childDocuments)) {
                break;
            }
            documents.addAll(childDocuments);
            parentIds = convertSet(childDocuments, PmsKnowledgeDocumentDO::getId);
        }
        return documents;
    }

    @Override
    public Set<Long> getExistingContentPermissionIdSet(Collection<Long> permissionIds) {
        if (CollUtil.isEmpty(permissionIds)) {
            return Collections.emptySet();
        }
        return documentMapper.selectExistingPermissionIdSet(permissionIds);
    }

}
