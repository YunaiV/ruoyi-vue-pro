package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeFolderMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeViewRecordService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.pms.service.kb.recycle.PmsKnowledgeRecycleService;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_FOLDER_MOVE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_FOLDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_FOLDER_PARENT_INVALID;

/**
 * PMS 知识库文件夹 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeFolderServiceImpl implements PmsKnowledgeFolderService {

    @Resource
    private PmsKnowledgeFolderMapper folderMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeDocumentService documentService;
    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;
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
    public Long createFolder(PmsKnowledgeFolderSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验知识库读取权限
        libraryMemberService.validateLibraryReadable(saveReqVO.getLibraryId(), userId);
        // 1.2 校验父文件夹存在
        PmsKnowledgeFolderDO parent = validateFolderParent(saveReqVO.getLibraryId(), saveReqVO.getParentId());
        Long permissionId;
        if (parent == null) {
            // 1.3 根目录下校验知识库编辑权限
            libraryMemberService.validateLibraryWritable(saveReqVO.getLibraryId(), userId);
            permissionId = contentPermissionService.createDefaultContentPermission(saveReqVO.getLibraryId(), userId);
        } else {
            // 1.3 子目录下校验父文件夹编辑权限
            contentPermissionService.validateContentPermissionWritable(parent.getPermissionId(), parent.getLibraryId(), userId);
            permissionId = parent.getPermissionId();
        }

        // 2. 创建文件夹
        PmsKnowledgeFolderDO folder = BeanUtils.toBean(saveReqVO, PmsKnowledgeFolderDO.class)
                .setPermissionId(permissionId)
                .setStatus(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
        folderMapper.insert(folder);
        return folder.getId();
    }

    @Override
    public void updateFolder(PmsKnowledgeFolderSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验文件夹存在
        PmsKnowledgeFolderDO folder = validateFolderExists(saveReqVO.getId());
        // 1.2 校验所属知识库可读
        libraryMemberService.validateLibraryReadable(folder.getLibraryId(), userId);
        // 1.3 校验内容编辑权限
        contentPermissionService.validateContentPermissionWritable(folder.getPermissionId(), folder.getLibraryId(), userId);
        // 1.4 校验文件夹属于请求知识库
        if (ObjectUtil.notEqual(folder.getLibraryId(), saveReqVO.getLibraryId())) {
            throw exception(KNOWLEDGE_FOLDER_PARENT_INVALID);
        }

        // 2. 更新文件夹名称；目录位置通过移动接口维护
        folderMapper.updateById(BeanUtils.toBean(saveReqVO, PmsKnowledgeFolderDO.class)
                .setId(folder.getId()).setLibraryId(null).setParentId(null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long id, Long userId) {
        // 1.1 校验文件夹存在
        PmsKnowledgeFolderDO folder = validateFolderExists(id);
        // 1.2 校验所属知识库可读
        libraryMemberService.validateLibraryReadable(folder.getLibraryId(), userId);
        // 1.3 校验内容删除权限
        contentPermissionService.validateContentPermissionDeletable(folder.getPermissionId(), folder.getLibraryId(), userId);

        // 2. 获得文件夹、子文件夹及其中的文档
        List<PmsKnowledgeFolderDO> folders = getFolderDescendants(folder);
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByFolderIds(
                convertSet(folders, PmsKnowledgeFolderDO::getId));

        // 3. 将文件夹子树移入回收站
        recycleService.recycleFolder(folder, folders, documents, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFolder(PmsKnowledgeFolderMoveReqVO moveReqVO, Long userId) {
        // 1.1 校验源文件夹存在
        PmsKnowledgeFolderDO folder = validateFolderExists(moveReqVO.getId());
        // 1.2 校验源知识库和目标知识库可读
        libraryMemberService.validateLibraryReadable(folder.getLibraryId(), userId);
        libraryMemberService.validateLibraryReadable(moveReqVO.getTargetLibraryId(), userId);
        // 1.3 移动会改变目录结构，校验源文件夹管理权限
        contentPermissionService.validateContentPermissionManageable(
                folder.getPermissionId(), folder.getLibraryId(), userId);
        // 1.4 校验目标父文件夹存在
        PmsKnowledgeFolderDO targetParent = validateFolderParent(moveReqVO.getTargetLibraryId(),
                moveReqVO.getTargetParentId());
        if (targetParent == null) {
            // 1.5 根目录下校验目标知识库管理权限
            libraryMemberService.validateLibraryAdmin(moveReqVO.getTargetLibraryId(), userId);
        } else {
            // 1.5 子目录下校验目标文件夹管理权限
            contentPermissionService.validateContentPermissionManageable(
                    targetParent.getPermissionId(), targetParent.getLibraryId(), userId);
        }

        // 2. 禁止移动到自身或子文件夹
        List<PmsKnowledgeFolderDO> folders = getFolderDescendants(folder);
        Set<Long> folderIds = convertSet(folders, PmsKnowledgeFolderDO::getId);
        if (folderIds.contains(moveReqVO.getTargetParentId())) {
            throw exception(KNOWLEDGE_FOLDER_MOVE_INVALID);
        }

        // 3. 跨知识库移动时复制权限，避免与原知识库中复用同一权限的内容相互影响
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentListByFolderIds(folderIds);
        Set<Long> sourcePermissionIds = new LinkedHashSet<>(convertSet(folders, PmsKnowledgeFolderDO::getPermissionId));
        sourcePermissionIds.addAll(convertSet(documents, PmsKnowledgeDocumentDO::getPermissionId));
        Map<Long, Long> clonedPermissionIdMap = ObjectUtil.equal(folder.getLibraryId(), moveReqVO.getTargetLibraryId())
                ? Collections.emptyMap()
                : contentPermissionService.cloneContentPermissions(sourcePermissionIds, moveReqVO.getTargetLibraryId());
        boolean crossLibrary = ObjectUtil.notEqual(folder.getLibraryId(), moveReqVO.getTargetLibraryId());

        // 4. 更新目录位置，并同步子文件夹和文档所属知识库与权限
        for (PmsKnowledgeFolderDO item : folders) {
            item.setLibraryId(moveReqVO.getTargetLibraryId());
            if (CollUtil.isNotEmpty(clonedPermissionIdMap)) {
                item.setPermissionId(clonedPermissionIdMap.get(item.getPermissionId()));
            }
            if (item.getId().equals(folder.getId())) {
                item.setParentId(moveReqVO.getTargetParentId());
            }
        }
        if (CollUtil.isNotEmpty(folders)) {
            folderMapper.updateBatch(folders);
        }
        documentService.moveDocumentList(documents, moveReqVO.getTargetLibraryId(), clonedPermissionIdMap);

        // 5. 跨知识库移动时同步关注和最近浏览记录的当前归属
        if (crossLibrary) {
            Set<Long> documentIds = convertSet(documents, PmsKnowledgeDocumentDO::getId);
            favoriteService.updateFavoriteLibraryIdByEntityIds(folderIds, documentIds,
                    moveReqVO.getTargetLibraryId());
            viewRecordService.updateViewRecordLibraryIdByEntityIds(folderIds, documentIds,
                    moveReqVO.getTargetLibraryId());
        }
        if (CollUtil.isNotEmpty(clonedPermissionIdMap)) {
            contentPermissionService.deleteUnusedContentPermissions(sourcePermissionIds);
        }
    }

    @Override
    public PmsKnowledgeFolderDO getFolder(Long id, Long userId) {
        // 1.1 校验文件夹存在
        PmsKnowledgeFolderDO folder = validateFolderExists(id);
        // 1.2 校验所属知识库可读
        libraryMemberService.validateLibraryReadable(folder.getLibraryId(), userId);
        // 1.3 校验内容读取权限
        contentPermissionService.validateContentPermissionReadable(folder.getPermissionId(), folder.getLibraryId(), userId);
        return folder;
    }

    @Override
    public PmsKnowledgeFolderDO getFolder(Long id) {
        return folderMapper.selectById(id);
    }

    @Override
    public List<PmsKnowledgeFolderDO> getFolderList(Collection<Long> ids) {
        return CollUtil.isEmpty(ids) ? Collections.emptyList() : folderMapper.selectByIds(ids);
    }

    @Override
    public List<PmsKnowledgeFolderDO> getFolderListByLibraryId(Long libraryId) {
        return folderMapper.selectListByLibraryId(libraryId);
    }

    @Override
    public void updateFolderList(Collection<PmsKnowledgeFolderDO> folders) {
        if (CollUtil.isEmpty(folders)) {
            return;
        }
        folderMapper.updateBatch(folders);
    }

    @Override
    public void restoreFolderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        folderMapper.updateToRestoreByIds(ids, PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
    }

    @Override
    public void deleteFolderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        folderMapper.deleteByIds(ids);
    }

    @Override
    public List<PmsKnowledgeFolderDO> getFolderList(Long libraryId, Long userId) {
        // 1. 校验知识库可读
        libraryMemberService.validateLibraryReadable(libraryId, userId);
        // 2. 查询全部正常文件夹
        List<PmsKnowledgeFolderDO> folders = folderMapper.selectListByLibraryIdAndStatus(
                libraryId, PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus());
        // 3. 按内容权限过滤，只返回当前用户有读取权限的文件夹
        Set<Long> readablePermissionIds = new LinkedHashSet<>();
        contentPermissionService.getCurrentUserContentPermissionLevelMap(convertSet(folders, PmsKnowledgeFolderDO::getPermissionId),
                libraryId, userId).forEach((permissionId, level) -> {
                    if (level != null) {
                        readablePermissionIds.add(permissionId);
                    }
                });
        folders.removeIf(folder -> !readablePermissionIds.contains(folder.getPermissionId()));
        return folders;
    }

    private PmsKnowledgeFolderDO validateFolderExists(Long id) {
        PmsKnowledgeFolderDO folder = folderMapper.selectById(id);
        if (folder == null || PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus().equals(folder.getStatus())) {
            throw exception(KNOWLEDGE_FOLDER_NOT_EXISTS);
        }
        return folder;
    }

    private PmsKnowledgeFolderDO validateFolderParent(Long libraryId, Long parentId) {
        if (parentId == null || PmsKnowledgeFolderDO.PARENT_ID_ROOT.equals(parentId)) {
            return null;
        }
        PmsKnowledgeFolderDO parent = folderMapper.selectById(parentId);
        if (parent == null || ObjectUtil.notEqual(libraryId, parent.getLibraryId())
                || !PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus().equals(parent.getStatus())) {
            throw exception(KNOWLEDGE_FOLDER_PARENT_INVALID);
        }
        return parent;
    }

    /**
     * 获得根文件夹及其全部子文件夹，使用 visited 集合避免异常环形数据导致循环
     */
    private List<PmsKnowledgeFolderDO> getFolderDescendants(PmsKnowledgeFolderDO root) {
        List<PmsKnowledgeFolderDO> folders = new ArrayList<>();
        folders.add(root);
        Set<Long> visitedFolderIds = new LinkedHashSet<>();
        visitedFolderIds.add(root.getId());
        Set<Long> parentIds = Collections.singleton(root.getId());
        while (CollUtil.isNotEmpty(parentIds)) {
            List<PmsKnowledgeFolderDO> childFolders = folderMapper.selectListByParentIds(parentIds);
            List<PmsKnowledgeFolderDO> newChildFolders = new ArrayList<>();
            for (PmsKnowledgeFolderDO childFolder : childFolders) {
                if (visitedFolderIds.add(childFolder.getId())) {
                    newChildFolders.add(childFolder);
                }
            }
            if (CollUtil.isEmpty(newChildFolders)) {
                break;
            }
            folders.addAll(newChildFolders);
            parentIds = convertSet(newChildFolders, PmsKnowledgeFolderDO::getId);
        }
        return folders;
    }

    @Override
    public Set<Long> getExistingContentPermissionIdSet(Collection<Long> permissionIds) {
        if (CollUtil.isEmpty(permissionIds)) {
            return Collections.emptySet();
        }
        return folderMapper.selectExistingPermissionIdSet(permissionIds);
    }

}
