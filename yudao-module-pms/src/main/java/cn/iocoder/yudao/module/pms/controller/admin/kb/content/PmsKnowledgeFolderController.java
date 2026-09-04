package cn.iocoder.yudao.module.pms.controller.admin.kb.content;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeFolderSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.folder.PmsKnowledgeTreeRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeFolderService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeViewRecordService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识库文件夹")
@RestController
@RequestMapping("/pms/kb/folder")
@Validated
public class PmsKnowledgeFolderController {

    @Resource
    private PmsKnowledgeFolderService folderService;
    @Resource
    private PmsKnowledgeDocumentService documentService;
    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @Resource
    private PmsKnowledgeFavoriteService favoriteService;
    @Resource
    private PmsKnowledgeViewRecordService viewRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建文件夹")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Long> createFolder(@Valid @RequestBody PmsKnowledgeFolderSaveReqVO saveReqVO) {
        return success(folderService.createFolder(saveReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文件夹")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateFolder(@Valid @RequestBody PmsKnowledgeFolderSaveReqVO saveReqVO) {
        folderService.updateFolder(saveReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "将文件夹移入回收站")
    @Parameter(name = "id", description = "文件夹编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:delete')")
    public CommonResult<Boolean> deleteFolder(@RequestParam("id") Long id) {
        folderService.deleteFolder(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/move")
    @Operation(summary = "移动文件夹")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> moveFolder(@Valid @RequestBody PmsKnowledgeFolderMoveReqVO moveReqVO) {
        folderService.moveFolder(moveReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文件夹详情")
    @Parameter(name = "id", description = "文件夹编号", required = true, example = "1024")
    @Parameter(name = "view", description = "是否记录本次浏览", example = "true")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PmsKnowledgeFolderRespVO> getFolder(
            @RequestParam("id") Long id,
            @RequestParam(value = "view", defaultValue = "false") Boolean view) {
        Long userId = getLoginUserId();
        PmsKnowledgeFolderDO folder = folderService.getFolder(id, userId);
        if (Boolean.TRUE.equals(view)) {
            viewRecordService.createViewRecord(folder.getLibraryId(), PmsKnowledgeObjectTypeEnum.FOLDER.getType(),
                    folder.getId(), userId);
        }
        return success(buildFolderRespVO(folder, userId));
    }

    @GetMapping("/tree")
    @Operation(summary = "获得知识库目录树")
    @Parameter(name = "libraryId", description = "知识库编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PmsKnowledgeTreeRespVO> getFolderTree(@RequestParam("libraryId") Long libraryId) {
        Long userId = getLoginUserId();
        List<PmsKnowledgeFolderDO> folders = folderService.getFolderList(libraryId, userId);
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentList(libraryId, userId);
        return success(buildTreeRespVO(libraryId, userId,
                libraryMemberService.isLibraryWritable(libraryId, userId),
                libraryMemberService.isLibraryAdmin(libraryId, userId), folders, documents));
    }

    // ==================== 拼接 VO ====================

    private PmsKnowledgeFolderRespVO buildFolderRespVO(PmsKnowledgeFolderDO folder, Long userId) {
        List<PmsKnowledgeFolderDO> folders = folderService.getFolderList(folder.getLibraryId(), userId);
        List<PmsKnowledgeDocumentDO> documents = documentService.getDocumentList(folder.getLibraryId(), userId);
        int childFolderCount = CollUtil.count(folders,
                item -> folder.getId().equals(item.getParentId()));
        int documentCount = (int) CollUtil.count(documents,
                item -> folder.getId().equals(item.getFolderId())
                        && (item.getParentId() == null || item.getParentId() == 0L));
        return BeanUtils.toBean(folder, PmsKnowledgeFolderRespVO.class)
                .setChildFolderCount(childFolderCount).setDocumentCount(documentCount)
                .setCurrentUserLevel(contentPermissionService.getCurrentUserContentPermissionLevel(folder.getPermissionId(), folder.getLibraryId(), userId))
                .setFavoriteStatus(favoriteService.isFavorite(PmsKnowledgeObjectTypeEnum.FOLDER.getType(), folder.getId(), userId));
    }

    private PmsKnowledgeTreeRespVO buildTreeRespVO(Long libraryId, Long userId, boolean writeStatus,
                                                   boolean manageStatus,
                                                   List<PmsKnowledgeFolderDO> folders, List<PmsKnowledgeDocumentDO> documents) {
        // 1. 批量获得当前用户的内容权限等级
        Set<Long> permissionIds = convertSet(folders, PmsKnowledgeFolderDO::getPermissionId);
        permissionIds.addAll(convertSet(documents, PmsKnowledgeDocumentDO::getPermissionId));
        Map<Long, Integer> levelMap = contentPermissionService.getCurrentUserContentPermissionLevelMap(permissionIds, libraryId, userId);

        // 2. 批量转换文件夹和文档节点
        Map<Long, PmsKnowledgeTreeRespVO.FolderNode> folderNodeMap = convertMap(folders,
                PmsKnowledgeFolderDO::getId, folder -> BeanUtils.toBean(folder, PmsKnowledgeTreeRespVO.FolderNode.class)
                        .setCurrentUserLevel(levelMap.get(folder.getPermissionId())).setChildren(new ArrayList<>()).setDocuments(new ArrayList<>()));
        Map<Long, PmsKnowledgeTreeRespVO.DocumentNode> documentNodeMap = convertMap(documents,
                PmsKnowledgeDocumentDO::getId, document -> BeanUtils.toBean(document, PmsKnowledgeTreeRespVO.DocumentNode.class)
                        .setCurrentUserLevel(levelMap.get(document.getPermissionId())).setChildren(new ArrayList<>()));

        // 3. 按父文档或所属文件夹组装文档树
        List<PmsKnowledgeTreeRespVO.DocumentNode> rootDocuments = new ArrayList<>();
        for (PmsKnowledgeDocumentDO document : documents) {
            PmsKnowledgeTreeRespVO.DocumentNode node = documentNodeMap.get(document.getId());
            PmsKnowledgeTreeRespVO.DocumentNode parentNode = documentNodeMap.get(document.getParentId());
            PmsKnowledgeTreeRespVO.FolderNode folderNode = folderNodeMap.get(document.getFolderId());
            if (parentNode != null) {
                parentNode.getChildren().add(node);
            } else if (folderNode != null) {
                folderNode.getDocuments().add(node);
            } else {
                rootDocuments.add(node);
            }
        }

        // 4. 按父文件夹组装文件夹树
        List<PmsKnowledgeTreeRespVO.FolderNode> rootFolders = new ArrayList<>();
        for (PmsKnowledgeFolderDO folder : folders) {
            PmsKnowledgeTreeRespVO.FolderNode node = folderNodeMap.get(folder.getId());
            PmsKnowledgeTreeRespVO.FolderNode parentNode = folderNodeMap.get(folder.getParentId());
            if (parentNode != null) {
                parentNode.getChildren().add(node);
            } else {
                rootFolders.add(node);
            }
        }
        return new PmsKnowledgeTreeRespVO().setLibraryId(libraryId).setWriteStatus(writeStatus)
                .setManageStatus(manageStatus)
                .setFolders(rootFolders).setDocuments(rootDocuments);
    }

}
