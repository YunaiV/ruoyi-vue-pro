package cn.iocoder.yudao.module.pms.service.kb.recycle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.recycle.vo.PmsKnowledgeRecycleDetailRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.recycle.vo.PmsKnowledgeRecycleRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.recycle.PmsKnowledgeRecycleRecordDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.recycle.PmsKnowledgeRecycleRecordMapper;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeContentLevelEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeLibraryMemberLevelEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeFolderService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentCommentService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentLikeService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentShareService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeViewRecordService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeGroupService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.equalsAny;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_ADMIN_REQUIRED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_RECYCLE_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_RECYCLE_TYPE_INVALID;

/**
 * PMS 知识库回收站 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeRecycleServiceImpl implements PmsKnowledgeRecycleService {

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeLibraryService libraryService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeFolderService folderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeDocumentService documentService;
    @Resource
    private PmsKnowledgeRecycleRecordMapper recycleRecordMapper;
    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @Resource
    private PmsKnowledgeGroupService knowledgeGroupService;
    @Resource
    private PmsKnowledgeFavoriteService favoriteService;
    @Resource
    private PmsKnowledgeDocumentLikeService documentLikeService;
    @Resource
    private PmsKnowledgeViewRecordService viewRecordService;
    @Resource
    private PmsKnowledgeDocumentShareService documentShareService;
    @Resource
    private PmsKnowledgeDocumentCommentService documentCommentService;
    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;

    @Resource
    private PermissionApi permissionApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleLibrary(PmsKnowledgeLibraryDO library, Long userId) {
        LocalDateTime deleteTime = LocalDateTime.now();
        // 1.1 查询知识库范围内的文件夹和文档
        List<PmsKnowledgeFolderDO> folders = folderService.getFolderListByLibraryId(library.getId());
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByLibraryId(library.getId());

        // 2.1 将知识库主记录移入回收站
        libraryService.updateLibraryToRecycled(library.getId(), userId, deleteTime);
        // 2.2 将文件夹及文档移入回收站
        if (CollUtil.isNotEmpty(folders)) {
            folderService.updateFolderList(convertList(folders, folder -> new PmsKnowledgeFolderDO()
                    .setId(folder.getId()).setStatus(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus())
                    .setDeleteUserId(userId).setDeleteTime(deleteTime)));
        }
        if (CollUtil.isNotEmpty(documents)) {
            documentService.updateDocumentList(convertList(documents, document -> new PmsKnowledgeDocumentDO()
                    .setId(document.getId()).setStatus(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus())
                    .setDeleteUserId(userId).setDeleteTime(deleteTime)));
        }

        // 3. 只记录本次显式删除的知识库，级联内容不重复出现在回收站
        insertRecycleRecord(library.getId(), PmsKnowledgeObjectTypeEnum.LIBRARY.getType(),
                library.getId(), library.getName(), userId, deleteTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleFolder(PmsKnowledgeFolderDO folder, List<PmsKnowledgeFolderDO> folders,
                              List<PmsKnowledgeDocumentDO> documents, Long userId) {
        LocalDateTime deleteTime = LocalDateTime.now();
        // 1. 将文件夹、子文件夹及其中的文档移入回收站
        if (CollUtil.isNotEmpty(documents)) {
            documentService.updateDocumentList(convertList(documents, document ->
                    new PmsKnowledgeDocumentDO().setId(document.getId())
                            .setStatus(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus())
                            .setDeleteUserId(userId).setDeleteTime(deleteTime)));
        }
        if (CollUtil.isNotEmpty(folders)) {
            folderService.updateFolderList(convertList(folders, item ->
                    new PmsKnowledgeFolderDO().setId(item.getId())
                            .setStatus(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus())
                            .setDeleteUserId(userId).setDeleteTime(deleteTime)));
        }

        // 2. 只记录本次显式删除的根文件夹
        insertRecycleRecord(folder.getLibraryId(), PmsKnowledgeObjectTypeEnum.FOLDER.getType(),
                folder.getId(), folder.getTitle(), userId, deleteTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleDocument(PmsKnowledgeDocumentDO document, List<PmsKnowledgeDocumentDO> documents,
                                Long userId) {
        LocalDateTime deleteTime = LocalDateTime.now();
        // 1. 将文档及全部子文档移入回收站
        if (CollUtil.isNotEmpty(documents)) {
            documentService.updateDocumentList(convertList(documents, item ->
                    new PmsKnowledgeDocumentDO().setId(item.getId())
                            .setStatus(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus())
                            .setDeleteUserId(userId).setDeleteTime(deleteTime)));
        }

        // 2. 只记录本次显式删除的根文档
        insertRecycleRecord(document.getLibraryId(), getDocumentRecycleType(document),
                document.getId(), document.getTitle(), userId, deleteTime);
    }

    @Override
    public List<PmsKnowledgeRecycleRecordDO> getLibraryRecycleList(Long userId) {
        return recycleRecordMapper.selectListByTypeAndDeleteUserId(PmsKnowledgeObjectTypeEnum.LIBRARY.getType(), userId);
    }

    @Override
    public List<PmsKnowledgeRecycleRecordDO> getContentRecycleList(Long libraryId, Long userId) {
        // 1.1 校验知识库可读
        libraryMemberService.validateLibraryReadable(libraryId, userId);
        // 1.2 查询知识库回收站记录，并排除知识库自身记录
        List<PmsKnowledgeRecycleRecordDO> records = recycleRecordMapper.selectListByLibraryId(libraryId);
        records.removeIf(record -> PmsKnowledgeObjectTypeEnum.LIBRARY.getType().equals(record.getType()));

        // 2.1 批量解析内容权限
        Map<Long, Long> recordPermissionIdMap = getRecordPermissionIdMap(records);
        Set<Long> permissionIds = new LinkedHashSet<>(recordPermissionIdMap.values());
        Map<Long, Integer> levelMap = contentPermissionService.getCurrentUserContentPermissionLevelMap(permissionIds, libraryId, userId);
        // 2.2 只展示当前用户仍可删除的回收站记录
        records.removeIf(record ->
                !PmsKnowledgeContentLevelEnum.canDelete(levelMap.get(recordPermissionIdMap.get(record.getId()))));
        return records;
    }

    @Override
    public PmsKnowledgeRecycleDetailRespVO getContentRecycleDetail(Long recordId, Long userId) {
        // 1. 复用回收站列表的库级与内容删除权限校验，避免详情接口绕过权限边界
        PmsKnowledgeRecycleRecordDO record = validateContentRecycleRecord(recordId, userId);

        // 2. 查询仍处于回收状态的内容，按根对象关系筛出本次级联删除的子树
        List<PmsKnowledgeFolderDO> folders = folderService.getFolderListByLibraryId(record.getLibraryId());
        folders.removeIf(folder -> ObjectUtil.notEqual(folder.getStatus(),
                PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus()));
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByLibraryId(record.getLibraryId());
        documents.removeIf(document -> ObjectUtil.notEqual(document.getStatus(),
                PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus()));
        Set<Long> folderIds = collectFolderDescendantIds(record, folders);
        Set<Long> documentIds = collectDocumentDescendantIds(record, documents, folderIds);

        // 3. 组装平铺详情，前端按 parentId/folderId 渲染递归树
        // 3.1 组装级联文件夹节点，并排除回收站根对象
        List<PmsKnowledgeRecycleDetailRespVO.Item> children = convertList(folders,
                folder -> new PmsKnowledgeRecycleDetailRespVO.Item()
                        .setId(folder.getId()).setType(PmsKnowledgeObjectTypeEnum.FOLDER.getType())
                        .setName(folder.getTitle()).setParentId(folder.getParentId())
                        .setDeleteTime(folder.getDeleteTime()),
                folder -> folderIds.contains(folder.getId())
                        && ObjectUtil.notEqual(folder.getId(), record.getEntityId()));
        // 3.2 组装级联文档节点，并排除回收站根对象
        children.addAll(convertList(documents,
                document -> new PmsKnowledgeRecycleDetailRespVO.Item()
                        .setId(document.getId()).setType(getDocumentRecycleType(document))
                        .setName(document.getTitle()).setParentId(document.getParentId())
                        .setFolderId(document.getFolderId()).setDeleteTime(document.getDeleteTime()),
                document -> documentIds.contains(document.getId())
                        && ObjectUtil.notEqual(document.getId(), record.getEntityId())));
        return new PmsKnowledgeRecycleDetailRespVO()
                .setRoot(BeanUtils.toBean(record, PmsKnowledgeRecycleRespVO.class))
                .setChildren(children);
    }

    @Override
    public PmsKnowledgeDocumentDO getContentRecyclePreview(Long recordId, Long entityId, Long userId) {
        // 1. 复用回收站内容权限校验，避免通过预览接口读取无权限内容
        PmsKnowledgeRecycleRecordDO record = validateContentRecycleRecord(recordId, userId);

        // 2. 回收站保留原文档数据，正文或文件地址仅用于只读预览
        Long targetEntityId = entityId != null ? entityId : record.getEntityId();
        Integer targetType = record.getType();
        if (entityId != null && ObjectUtil.notEqual(entityId, record.getEntityId())) {
            PmsKnowledgeRecycleDetailRespVO detail = getContentRecycleDetail(recordId, userId);
            PmsKnowledgeRecycleDetailRespVO.Item item = CollUtil.findOne(detail.getChildren(), child -> child.getId().equals(entityId));
            if (item == null) {
                throw exception(KNOWLEDGE_RECYCLE_RECORD_NOT_EXISTS);
            }
            targetType = item.getType();
        }
        if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(targetType)) {
            throw exception(KNOWLEDGE_RECYCLE_TYPE_INVALID);
        }
        PmsKnowledgeDocumentDO document = documentService.getDocument(targetEntityId);
        if (document == null) {
            throw exception(KNOWLEDGE_RECYCLE_RECORD_NOT_EXISTS);
        }
        return document;
    }

    /**
     * 收集回收站记录对应的文件夹及其后代文件夹
     *
     * @param record 回收站记录
     * @param folders 知识库文件夹列表
     * @return 文件夹编号集合
     */
    private Set<Long> collectFolderDescendantIds(PmsKnowledgeRecycleRecordDO record,
                                                 List<PmsKnowledgeFolderDO> folders) {
        if (ObjectUtil.notEqual(PmsKnowledgeObjectTypeEnum.FOLDER.getType(), record.getType())) {
            return Collections.emptySet();
        }
        return getFolderDescendantIds(folders, Collections.singleton(record.getEntityId()));
    }

    /**
     * 收集回收站记录对应的文档及其后代文档
     *
     * @param record 回收站记录
     * @param documents 知识库文档列表
     * @param folderIds 回收站文件夹及其后代编号
     * @return 文档编号集合
     */
    private Set<Long> collectDocumentDescendantIds(PmsKnowledgeRecycleRecordDO record,
                                                   List<PmsKnowledgeDocumentDO> documents,
                                                   Set<Long> folderIds) {
        // 1. 文档回收记录：收集根文档及其全部子文档
        if (isDocumentRecycleType(record.getType())) {
            return getDocumentDescendantIds(documents, Collections.singleton(record.getEntityId()));
        }

        // 2. 非文件夹回收记录：没有可关联的级联文档
        if (ObjectUtil.notEqual(PmsKnowledgeObjectTypeEnum.FOLDER.getType(), record.getType())) {
            return Collections.emptySet();
        }
        // 3. 文件夹回收记录：按级联文件夹编号筛选文档
        if (CollUtil.isEmpty(documents) || CollUtil.isEmpty(folderIds)) {
            return Collections.emptySet();
        }
        Set<Long> result = new LinkedHashSet<>();
        for (PmsKnowledgeDocumentDO document : documents) {
            if (folderIds.contains(document.getFolderId())) {
                result.add(document.getId());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreContentRecycle(Long recordId, Long userId) {
        // 1.1 校验回收站记录存在
        PmsKnowledgeRecycleRecordDO record = validateRecycleRecord(recordId);
        validateRecycleType(record.getType());

        // 2. 按记录类型校验权限并恢复：知识库要求管理员权限，文件夹和文档要求内容删除权限
        if (PmsKnowledgeObjectTypeEnum.LIBRARY.getType().equals(record.getType())) {
            validateRecycledLibraryAdmin(record, userId);
            restoreLibrary(record);
        } else if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(record.getType())) {
            validateRecycleContentDeletable(record, userId);
            restoreFolder(record);
        } else if (isDocumentRecycleType(record.getType())) {
            validateRecycleContentDeletable(record, userId);
            restoreDocument(record);
        }

        // 3. 删除本次回收站记录
        recycleRecordMapper.deleteById(recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContentRecycle(Long recordId, Long userId) {
        // 1. 校验回收站记录存在
        PmsKnowledgeRecycleRecordDO record = validateRecycleRecord(recordId);
        validateRecycleType(record.getType());

        // 2.A 知识库类型：要求管理员权限，并彻底删除知识库及其全部关联数据
        if (PmsKnowledgeObjectTypeEnum.LIBRARY.getType().equals(record.getType())) {
            validateRecycledLibraryAdmin(record, userId);
            permanentDeleteLibrary(record.getLibraryId());
            return;
        }

        // 2.B 内容类型：校验内容删除权限后，按文件夹和文档类型分别彻底删除
        validateRecycleContentDeletable(record, userId);
        if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(record.getType())) {
            permanentDeleteFolder(record);
        } else if (isDocumentRecycleType(record.getType())) {
            permanentDeleteDocument(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteExpiredRecycleRecords(LocalDateTime deleteTime) {
        // 1. 按删除时间从早到晚查询到期记录
        List<PmsKnowledgeRecycleRecordDO> records = recycleRecordMapper.selectListByDeleteTimeBefore(deleteTime);
        if (CollUtil.isEmpty(records)) {
            return 0;
        }

        // 2. 按记录类型执行物理清理；上级对象清理后，跳过已被级联删除的记录
        for (PmsKnowledgeRecycleRecordDO record : records) {
            if (recycleRecordMapper.selectById(record.getId()) == null) {
                continue;
            }
            validateRecycleType(record.getType());
            if (PmsKnowledgeObjectTypeEnum.LIBRARY.getType().equals(record.getType())) {
                permanentDeleteLibrary(record.getLibraryId());
            } else if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(record.getType())) {
                permanentDeleteFolder(record);
            } else if (isDocumentRecycleType(record.getType())) {
                permanentDeleteDocument(record);
            }
        }
        return records.size();
    }

    /**
     * 恢复知识库，并恢复本次知识库删除所级联回收的内容
     *
     * @param record 知识库回收站记录
     */
    private void restoreLibrary(PmsKnowledgeRecycleRecordDO record) {
        // 1.1 查询并校验仍处于回收站状态的知识库
        PmsKnowledgeLibraryDO library = libraryService.getLibrary(record.getLibraryId());
        if (library == null || ObjectUtil.notEqual(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus(),
                library.getStatus())) {
            throw exception(KNOWLEDGE_LIBRARY_NOT_EXISTS);
        }
        // 1.2 查询知识库回收站记录，供后续排除独立删除的子树
        List<PmsKnowledgeRecycleRecordDO> records = recycleRecordMapper.selectListByLibraryId(library.getId());

        // 2.1 恢复知识库主记录
        libraryService.restoreLibrary(library.getId());
        // 2.2 恢复随知识库级联删除的内容，保留此前被单独删除的子树
        restoreLibraryContent(library.getId(), records, null);
    }

    /**
     * 恢复文件夹及其子树，并在父文件夹仍被删除时挂载到知识库根目录
     *
     * @param record 文件夹回收站记录
     */
    private void restoreFolder(PmsKnowledgeRecycleRecordDO record) {
        // 1. 校验根文件夹仍处于回收站状态
        PmsKnowledgeFolderDO root = folderService.getFolder(record.getEntityId());
        if (root == null || ObjectUtil.notEqual(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus(),
                root.getStatus())) {
            throw exception(KNOWLEDGE_RECYCLE_RECORD_NOT_EXISTS);
        }
        // 1.2 查询知识库回收站记录和原父文件夹
        List<PmsKnowledgeRecycleRecordDO> records = recycleRecordMapper.selectListByLibraryId(record.getLibraryId());
        PmsKnowledgeFolderDO parent = root.getParentId() != null
                && ObjectUtil.notEqual(PmsKnowledgeFolderDO.PARENT_ID_ROOT, root.getParentId())
                ? folderService.getFolder(root.getParentId()) : null;

        // 2.1 恢复当前文件夹树，同时跳过其中独立删除的子树
        restoreLibraryContent(record.getLibraryId(), records, root.getId());
        // 2.2 被删除的父文件夹尚未恢复时，将当前根文件夹恢复到知识库根目录
        if (root.getParentId() != null
                && ObjectUtil.notEqual(PmsKnowledgeFolderDO.PARENT_ID_ROOT, root.getParentId())) {
            if (parent == null || PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus().equals(parent.getStatus())) {
                folderService.updateFolderList(Collections.singleton(new PmsKnowledgeFolderDO()
                        .setId(root.getId()).setParentId(PmsKnowledgeFolderDO.PARENT_ID_ROOT)));
            }
        }
    }

    /**
     * 恢复文档及其子文档，并修正失效的目录或父文档引用
     *
     * @param record 文档回收站记录
     */
    private void restoreDocument(PmsKnowledgeRecycleRecordDO record) {
        // 1. 校验根文档仍处于回收站状态
        PmsKnowledgeDocumentDO root = documentService.getDocument(record.getEntityId());
        if (root == null || ObjectUtil.notEqual(PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus(),
                root.getStatus())) {
            throw exception(KNOWLEDGE_RECYCLE_RECORD_NOT_EXISTS);
        }
        // 1.2 查询知识库文档全集和回收站记录
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByLibraryId(record.getLibraryId());
        List<PmsKnowledgeRecycleRecordDO> records = recycleRecordMapper.selectListByLibraryId(record.getLibraryId());
        // 1.3 查询文档原目录和父文档
        PmsKnowledgeFolderDO folder = equalsAny(root.getFolderId(), null, PmsKnowledgeDocumentDO.FOLDER_ID_ROOT) ? null
                : folderService.getFolder(root.getFolderId());
        PmsKnowledgeDocumentDO parent = equalsAny(root.getParentId(), null, PmsKnowledgeDocumentDO.PARENT_ID_ROOT) ? null
                : documentService.getDocument(root.getParentId());

        // 2.1 恢复文档树，同时保留独立删除的子文档树
        Set<Long> restoreIds = getDocumentDescendantIds(documents, Collections.singleton(root.getId()));
        excludeProtectedDocuments(restoreIds, documents, records, record.getId());
        restoreDocuments(restoreIds);
        // 2.2 原目录或父文档尚未恢复时，将当前根文档恢复到知识库根目录
        boolean invalidFolder = root.getFolderId() != null
                && ObjectUtil.notEqual(PmsKnowledgeDocumentDO.FOLDER_ID_ROOT, root.getFolderId())
                && (folder == null || PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus().equals(folder.getStatus()));
        boolean invalidParent = root.getParentId() != null
                && ObjectUtil.notEqual(PmsKnowledgeDocumentDO.PARENT_ID_ROOT, root.getParentId())
                && (parent == null || PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus().equals(parent.getStatus()));
        if (invalidFolder || invalidParent) {
            documentService.updateDocumentList(Collections.singleton(new PmsKnowledgeDocumentDO()
                    .setId(root.getId()).setFolderId(PmsKnowledgeDocumentDO.FOLDER_ID_ROOT)
                    .setParentId(PmsKnowledgeDocumentDO.PARENT_ID_ROOT)));
        }
    }

    /**
     * 恢复知识库范围内的文件夹和文档，排除仍由其他回收站记录保护的子树
     *
     * @param libraryId 知识库编号
     * @param records   知识库回收站记录
     * @param folderRootId 仅恢复指定文件夹子树时的根编号，为 {@code null} 表示整个知识库
     */
    private void restoreLibraryContent(Long libraryId, List<PmsKnowledgeRecycleRecordDO> records, Long folderRootId) {
        // 1.1 查询知识库范围内的文件夹和文档
        List<PmsKnowledgeFolderDO> folders = folderService.getFolderListByLibraryId(libraryId);
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByLibraryId(libraryId);
        // 1.2 解析目标文件夹树，并排除被单独删除的文件夹子树
        Set<Long> restoreFolderIds = folderRootId == null ? convertSet(folders, PmsKnowledgeFolderDO::getId)
                : getFolderDescendantIds(folders, Collections.singleton(folderRootId));
        excludeProtectedFolders(restoreFolderIds, folders, records, folderRootId);
        // 1.3 解析需要随文件夹恢复的文档树，并排除被单独删除的文档子树
        Set<Long> documentRoots = new LinkedHashSet<>();
        for (PmsKnowledgeDocumentDO document : documents) {
            if ((folderRootId == null && equalsAny(document.getFolderId(), null,
                    PmsKnowledgeDocumentDO.FOLDER_ID_ROOT)
                    && equalsAny(document.getParentId(), null, PmsKnowledgeDocumentDO.PARENT_ID_ROOT))
                    || restoreFolderIds.contains(document.getFolderId())) {
                documentRoots.add(document.getId());
            }
        }
        Set<Long> restoreDocumentIds = getDocumentDescendantIds(documents, documentRoots);
        excludeProtectedDocuments(restoreDocumentIds, documents, records, null);

        // 2.1 恢复文件夹树
        if (CollUtil.isNotEmpty(restoreFolderIds)) {
            folderService.restoreFolderList(restoreFolderIds);
        }
        // 2.2 恢复文档树
        restoreDocuments(restoreDocumentIds);
    }

    /**
     * 批量恢复文档状态
     *
     * @param restoreIds 待恢复文档编号
     */
    private void restoreDocuments(Set<Long> restoreIds) {
        if (CollUtil.isEmpty(restoreIds)) {
            return;
        }
        documentService.restoreDocumentList(restoreIds);
    }

    /**
     * 从恢复集合中排除仍处于独立回收状态的文件夹子树
     *
     * @param restoreIds 待恢复文件夹编号集合
     * @param folders 所属知识库文件夹
     * @param records 回收站记录
     * @param currentFolderId 当前正在恢复的根文件夹编号
     */
    private void excludeProtectedFolders(Set<Long> restoreIds, List<PmsKnowledgeFolderDO> folders,
                                         List<PmsKnowledgeRecycleRecordDO> records, Long currentFolderId) {
        Set<Long> protectedRoots = new LinkedHashSet<>();
        for (PmsKnowledgeRecycleRecordDO record : records) {
            if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(record.getType())
                    && ObjectUtil.notEqual(record.getEntityId(), currentFolderId)) {
                protectedRoots.add(record.getEntityId());
            }
        }
        restoreIds.removeAll(getFolderDescendantIds(folders, protectedRoots));
    }

    /**
     * 从恢复集合中排除仍处于独立回收状态的文档子树
     *
     * @param restoreIds 待恢复文档编号集合
     * @param documents 所属知识库文档
     * @param records 回收站记录
     * @param currentRecordId 当前正在恢复的回收站记录编号
     */
    private void excludeProtectedDocuments(Set<Long> restoreIds, List<PmsKnowledgeDocumentDO> documents,
                                           List<PmsKnowledgeRecycleRecordDO> records, Long currentRecordId) {
        Set<Long> protectedRoots = new LinkedHashSet<>();
        for (PmsKnowledgeRecycleRecordDO record : records) {
            if (isDocumentRecycleType(record.getType())
                    && ObjectUtil.notEqual(record.getId(), currentRecordId)) {
                protectedRoots.add(record.getEntityId());
            }
        }
        restoreIds.removeAll(getDocumentDescendantIds(documents, protectedRoots));
    }

    /**
     * 彻底删除知识库及其全部内容、成员和关联数据
     *
     * @param libraryId 知识库编号
     */
    private void permanentDeleteLibrary(Long libraryId) {
        // 1.1 查询知识库范围内的文档
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByLibraryId(libraryId);
        Set<Long> documentIds = convertSet(documents, PmsKnowledgeDocumentDO::getId);
        // 1.2 查询知识库范围内的文件夹
        Set<Long> folderIds = convertSet(folderService.getFolderListByLibraryId(libraryId),
                PmsKnowledgeFolderDO::getId);

        // 2.1 删除文档收藏、点赞、浏览、分享和评论
        favoriteService.deleteFavoritesByLibraryId(libraryId);
        documentLikeService.deleteLikesByDocumentIds(documentIds);
        viewRecordService.deleteViewRecordsByLibraryId(libraryId);
        deleteDocuments(documentIds);
        // 2.2 删除文件夹主记录
        folderService.deleteFolderList(folderIds);
        // 2.3 删除内容权限、回收站记录和知识库分组关系
        contentPermissionService.deleteContentPermissionsByLibraryId(libraryId);
        recycleRecordMapper.deleteByLibraryId(libraryId);
        knowledgeGroupService.deleteKnowledgeGroupRelationsByLibraryId(libraryId);

        // 3.1 删除知识库成员关系
        libraryMemberService.deleteLibraryMembersByLibraryId(libraryId);
        // 3.2 最后删除知识库主记录
        libraryService.deleteLibraryPermanently(libraryId);
    }

    /**
     * 彻底删除文件夹子树及其中的文档和关联数据
     *
     * @param record 文件夹回收站记录
     */
    private void permanentDeleteFolder(PmsKnowledgeRecycleRecordDO record) {
        // 1.1 查询文件夹和文档全集
        List<PmsKnowledgeFolderDO> folders = folderService.getFolderListByLibraryId(record.getLibraryId());
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByLibraryId(record.getLibraryId());
        // 1.2 解析文件夹子树和其中的文档编号
        Set<Long> folderIds = getFolderDescendantIds(folders, Collections.singleton(record.getEntityId()));
        Set<Long> documentRoots = new LinkedHashSet<>();
        for (PmsKnowledgeDocumentDO document : documents) {
            if (folderIds.contains(document.getFolderId())) {
                documentRoots.add(document.getId());
            }
        }
        Set<Long> documentIds = getDocumentDescendantIds(documents, documentRoots);
        // 1.3 收集文件夹和文档对应的内容权限编号
        Set<Long> permissionIds = new LinkedHashSet<>();
        for (PmsKnowledgeFolderDO folder : folders) {
            if (folderIds.contains(folder.getId())) {
                permissionIds.add(folder.getPermissionId());
            }
        }
        for (PmsKnowledgeDocumentDO document : documents) {
            if (documentIds.contains(document.getId())) {
                permissionIds.add(document.getPermissionId());
            }
        }

        // 2.1 删除收藏、点赞、浏览、分享和评论
        favoriteService.deleteFavoritesByEntityIds(folderIds, documentIds);
        documentLikeService.deleteLikesByDocumentIds(documentIds);
        viewRecordService.deleteViewRecordsByEntityIds(folderIds, documentIds);
        deleteDocuments(documentIds);
        // 2.2 删除文档和文件夹主记录
        folderService.deleteFolderList(folderIds);
        // 2.3 删除不再被引用的内容权限和回收站记录
        contentPermissionService.deleteUnusedContentPermissions(permissionIds);
        recycleRecordMapper.deleteByTypeAndEntityIds(
                Collections.singleton(PmsKnowledgeObjectTypeEnum.FOLDER.getType()), folderIds);
        if (CollUtil.isNotEmpty(documentIds)) {
            recycleRecordMapper.deleteByTypeAndEntityIds(Arrays.asList(
                    PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(),
                    PmsKnowledgeObjectTypeEnum.FILE.getType()), documentIds);
        }
    }

    /**
     * 彻底删除文档子树及其互动、权限和回收站记录
     *
     * @param record 文档回收站记录
     */
    private void permanentDeleteDocument(PmsKnowledgeRecycleRecordDO record) {
        // 1.1 查询知识库文档全集
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByLibraryId(record.getLibraryId());
        // 1.2 解析文档子树编号
        Set<Long> documentIds = getDocumentDescendantIds(documents, Collections.singleton(record.getEntityId()));
        // 1.3 收集文档对应的内容权限编号
        Set<Long> permissionIds = new LinkedHashSet<>();
        for (PmsKnowledgeDocumentDO document : documents) {
            if (documentIds.contains(document.getId())) {
                permissionIds.add(document.getPermissionId());
            }
        }

        // 2.1 删除收藏、点赞、浏览、分享和评论
        favoriteService.deleteFavoritesByEntityIds(Collections.emptySet(), documentIds);
        documentLikeService.deleteLikesByDocumentIds(documentIds);
        viewRecordService.deleteViewRecordsByEntityIds(Collections.emptySet(), documentIds);
        deleteDocuments(documentIds);
        // 2.2 删除不再被引用的内容权限和回收站记录
        contentPermissionService.deleteUnusedContentPermissions(permissionIds);
        recycleRecordMapper.deleteByTypeAndEntityIds(Arrays.asList(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(),
                PmsKnowledgeObjectTypeEnum.FILE.getType()), documentIds);
    }

    /**
     * 删除文档及其分享、评论等互动数据
     *
     * @param documentIds 文档编号集合
     */
    private void deleteDocuments(Set<Long> documentIds) {
        if (CollUtil.isEmpty(documentIds)) {
            return;
        }
        // 1. 先删除依赖文档编号的分享和评论
        documentShareService.deleteSharesByDocumentIds(documentIds);
        documentCommentService.deleteCommentsByDocumentIds(documentIds);
        // 2. 再删除文档主记录
        documentService.deleteDocumentList(documentIds);
    }

    /**
     * 获得文件夹根节点及其全部后代编号
     *
     * @param folders 所属知识库文件夹
     * @param rootIds 根文件夹编号
     * @return 根节点及后代编号集合
     */
    private Set<Long> getFolderDescendantIds(List<PmsKnowledgeFolderDO> folders, Collection<Long> rootIds) {
        Set<Long> descendantIds = new LinkedHashSet<>(rootIds);
        // 层级深度设上限，避免异常循环父子关系导致无限遍历
        for (int level = 0; level < Short.MAX_VALUE; level++) {
            boolean changed = false;
            for (PmsKnowledgeFolderDO folder : folders) {
                if (descendantIds.contains(folder.getParentId()) && descendantIds.add(folder.getId())) {
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        return descendantIds;
    }

    /**
     * 获得文档根节点及其全部后代编号
     *
     * @param documents 所属知识库文档
     * @param rootIds 根文档编号
     * @return 根节点及后代编号集合
     */
    private Set<Long> getDocumentDescendantIds(List<PmsKnowledgeDocumentDO> documents, Collection<Long> rootIds) {
        Set<Long> descendantIds = new LinkedHashSet<>(rootIds);
        // 层级深度设上限，避免异常循环父子关系导致无限遍历
        for (int level = 0; level < Short.MAX_VALUE; level++) {
            boolean changed = false;
            for (PmsKnowledgeDocumentDO document : documents) {
                if (descendantIds.contains(document.getParentId()) && descendantIds.add(document.getId())) {
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        return descendantIds;
    }

    private void insertRecycleRecord(Long libraryId, Integer type, Long entityId, String name,
                                     Long userId, LocalDateTime deleteTime) {
        recycleRecordMapper.insert(new PmsKnowledgeRecycleRecordDO().setLibraryId(libraryId).setType(type)
                .setEntityId(entityId).setName(name).setDeleteUserId(userId).setDeleteTime(deleteTime));
    }

    private PmsKnowledgeRecycleRecordDO validateRecycleRecord(Long id) {
        PmsKnowledgeRecycleRecordDO record = recycleRecordMapper.selectById(id);
        if (record == null) {
            throw exception(KNOWLEDGE_RECYCLE_RECORD_NOT_EXISTS);
        }
        return record;
    }

    /**
     * 校验内容回收站记录存在且当前用户仍具备内容删除权限
     *
     * @param recordId 回收站记录编号
     * @param userId 当前用户编号
     * @return 已校验的回收站记录
     */
    private PmsKnowledgeRecycleRecordDO validateContentRecycleRecord(Long recordId, Long userId) {
        PmsKnowledgeRecycleRecordDO record = validateRecycleRecord(recordId);
        if (PmsKnowledgeObjectTypeEnum.LIBRARY.getType().equals(record.getType())) {
            throw exception(KNOWLEDGE_RECYCLE_TYPE_INVALID);
        }
        validateRecycleContentDeletable(record, userId);
        return record;
    }

    /**
     * 批量解析回收站记录对应实体的内容权限编号
     *
     * @param records 回收站记录
     * @return 回收站记录编号到权限编号的映射
     */
    private Map<Long, Long> getRecordPermissionIdMap(List<PmsKnowledgeRecycleRecordDO> records) {
        // 1. 按实体类型批量收集编号
        Set<Long> folderIds = new LinkedHashSet<>();
        Set<Long> documentIds = new LinkedHashSet<>();
        for (PmsKnowledgeRecycleRecordDO record : records) {
            if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(record.getType())) {
                folderIds.add(record.getEntityId());
            } else if (isDocumentRecycleType(record.getType())) {
                documentIds.add(record.getEntityId());
            }
        }
        Map<Long, Long> folderPermissionIdMap = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(folderIds)) {
            folderService.getFolderList(folderIds).forEach(folder ->
                    folderPermissionIdMap.put(folder.getId(), folder.getPermissionId()));
        }
        Map<Long, Long> documentPermissionIdMap = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(documentIds)) {
            documentService.getDocumentList(documentIds).forEach(document ->
                    documentPermissionIdMap.put(document.getId(), document.getPermissionId()));
        }

        // 2. 将实体权限编号映射回回收站记录
        Map<Long, Long> recordPermissionIdMap = new LinkedHashMap<>();
        records.forEach(record -> {
            Map<Long, Long> permissionIdMap = PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(record.getType())
                    ? folderPermissionIdMap : documentPermissionIdMap;
            recordPermissionIdMap.put(record.getId(), permissionIdMap.get(record.getEntityId()));
        });
        return recordPermissionIdMap;
    }

    /**
     * 校验当前用户是否拥有回收站内容的删除权限
     *
     * @param record 回收站记录
     * @param userId 当前用户编号
     */
    private void validateRecycleContentDeletable(PmsKnowledgeRecycleRecordDO record, Long userId) {
        // 1.1 先校验知识库可读
        libraryMemberService.validateLibraryReadable(record.getLibraryId(), userId);

        // 1.2 再解析回收对象对应的内容权限
        Long permissionId;
        if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(record.getType())) {
            PmsKnowledgeFolderDO folder = folderService.getFolder(record.getEntityId());
            permissionId = folder != null ? folder.getPermissionId() : null;
        } else {
            PmsKnowledgeDocumentDO document = documentService.getDocument(record.getEntityId());
            permissionId = document != null ? document.getPermissionId() : null;
        }
        if (permissionId == null) {
            throw exception(KNOWLEDGE_RECYCLE_RECORD_NOT_EXISTS);
        }
        // 2. 校验当前用户具备内容删除权限
        contentPermissionService.validateContentPermissionDeletable(permissionId, record.getLibraryId(), userId);
    }

    /**
     * 校验当前用户是否为知识库创建人、管理员或系统超级管理员
     *
     * @param record 知识库回收站记录
     * @param userId 当前用户编号
     */
    private void validateRecycledLibraryAdmin(PmsKnowledgeRecycleRecordDO record, Long userId) {
        if (permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode())) {
            return;
        }
        PmsKnowledgeLibraryMemberDO member =
                libraryMemberService.getMemberByLibraryIdAndUserId(record.getLibraryId(), userId);
        if (member == null) {
            AdminUserRespDTO user = adminUserApi.getUser(userId);
            if (user != null && user.getDeptId() != null) {
                member = libraryMemberService.getMemberByLibraryIdAndDeptId(record.getLibraryId(), user.getDeptId());
            }
        }
        if (member == null || !equalsAny(member.getLevel(), PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel(),
                PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel())) {
            throw exception(KNOWLEDGE_LIBRARY_ADMIN_REQUIRED);
        }
    }

    /**
     * 将文档类型映射为回收站对象类型
     *
     * @param document 文档
     * @return 文件或普通文档对应的回收站类型
     */
    private Integer getDocumentRecycleType(PmsKnowledgeDocumentDO document) {
        return PmsKnowledgeDocumentTypeEnum.FILE.getType().equals(document.getType())
                ? PmsKnowledgeObjectTypeEnum.FILE.getType() : PmsKnowledgeObjectTypeEnum.DOCUMENT.getType();
    }

    /**
     * 判断回收站记录是否为文档类对象
     *
     * @param type 回收站对象类型
     * @return 是否为文档或文件
     */
    private boolean isDocumentRecycleType(Integer type) {
        return equalsAny(type, PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(),
                PmsKnowledgeObjectTypeEnum.FILE.getType());
    }

    /**
     * 校验回收站记录类型，避免未知类型进入内容权限和实体查询分支
     *
     * @param type 回收站对象类型
     */
    private void validateRecycleType(Integer type) {
        if (equalsAny(type, PmsKnowledgeObjectTypeEnum.LIBRARY.getType(),
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(),
                PmsKnowledgeObjectTypeEnum.FILE.getType())) {
            return;
        }
        throw exception(KNOWLEDGE_RECYCLE_TYPE_INVALID);
    }

}
