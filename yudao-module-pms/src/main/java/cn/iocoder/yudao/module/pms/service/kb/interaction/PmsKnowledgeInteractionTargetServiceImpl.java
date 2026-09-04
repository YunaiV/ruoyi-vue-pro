package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeFolderDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryService;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_INTERACTION_OBJECT_INVALID;

/**
 * PMS 知识互动对象 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeInteractionTargetServiceImpl implements PmsKnowledgeInteractionTargetService {

    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @Resource
    private PmsKnowledgeLibraryService libraryService;
    @Resource
    private PmsKnowledgeFolderReadService folderReadService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PmsKnowledgeDocumentService documentService;
    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;

    @Override
    public Long validateTargetReadable(Integer type, Long entityId, Long userId) {
        // 1. 校验对象类型
        PmsKnowledgeObjectTypeEnum objectType = PmsKnowledgeObjectTypeEnum.valueOf(type);
        if (objectType == null) {
            throw exception(KNOWLEDGE_INTERACTION_OBJECT_INVALID);
        }

        // 2. 校验对象可读
        if (PmsKnowledgeObjectTypeEnum.LIBRARY.equals(objectType)) {
            return libraryMemberService.validateLibraryReadable(entityId, userId).getId();
        }
        if (PmsKnowledgeObjectTypeEnum.FOLDER.equals(objectType)) {
            return folderReadService.getReadableFolder(entityId, userId).getLibraryId();
        }
        PmsKnowledgeDocumentDO document = documentService.getDocument(entityId, userId);
        if (ObjectUtil.notEqual(type, document.getType())) {
            throw exception(KNOWLEDGE_INTERACTION_OBJECT_INVALID);
        }
        return document.getLibraryId();
    }

    @Override
    public List<PmsKnowledgeInteractionItemRespVO> getReadableItemList(
            Collection<PmsKnowledgeInteractionItemRespVO> targets, Long userId) {
        if (CollUtil.isEmpty(targets)) {
            return Collections.emptyList();
        }

        // 1. 批量收集知识库、文件夹和文档编号
        Set<Long> libraryIds = new LinkedHashSet<>();
        Set<Long> folderIds = new LinkedHashSet<>();
        Set<Long> documentIds = new LinkedHashSet<>();
        for (PmsKnowledgeInteractionItemRespVO target : targets) {
            libraryIds.add(target.getLibraryId());
            if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(target.getType())) {
                folderIds.add(target.getEntityId());
            } else if (PmsKnowledgeObjectTypeEnum.DOCUMENT.getType().equals(target.getType())
                    || PmsKnowledgeObjectTypeEnum.FILE.getType().equals(target.getType())) {
                documentIds.add(target.getEntityId());
            }
        }

        // 2. 批量查询对象，并计算当前用户的可读范围
        libraryIds.retainAll(libraryMemberService.getReadableLibraryIdList(userId));
        Map<Long, PmsKnowledgeLibraryDO> libraryMap = convertMap(libraryService.getLibraryList(libraryIds),
                PmsKnowledgeLibraryDO::getId);
        Map<Long, PmsKnowledgeFolderDO> folderMap = convertMap(folderReadService.getFolderList(folderIds),
                PmsKnowledgeFolderDO::getId);
        Map<Long, PmsKnowledgeDocumentDO> documentMap = convertMap(documentService.getDocumentList(documentIds),
                PmsKnowledgeDocumentDO::getId);
        Set<Long> readablePermissionIds = contentPermissionService.getReadableContentPermissionIdSet(libraryIds, userId);

        // 3. 按原始顺序组装可读对象
        List<PmsKnowledgeInteractionItemRespVO> items = new ArrayList<>();
        for (PmsKnowledgeInteractionItemRespVO target : targets) {
            PmsKnowledgeInteractionItemRespVO item = buildReadableItem(target, libraryMap, folderMap, documentMap,
                    readablePermissionIds);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * 将互动目标转换为当前用户可读的展示对象
     *
     * @param target 互动目标
     * @param libraryMap 知识库 Map
     * @param folderMap 文件夹 Map
     * @param documentMap 文档 Map
     * @param readablePermissionIds 可读内容权限编号集合
     * @return 可读的互动展示对象；不可读时返回 {@code null}
     */
    private PmsKnowledgeInteractionItemRespVO buildReadableItem(
            PmsKnowledgeInteractionItemRespVO target, Map<Long, PmsKnowledgeLibraryDO> libraryMap,
            Map<Long, PmsKnowledgeFolderDO> folderMap, Map<Long, PmsKnowledgeDocumentDO> documentMap,
            Set<Long> readablePermissionIds) {
        // 1. 仅处理仍处于正常状态的所属知识库
        PmsKnowledgeLibraryDO library = libraryMap.get(target.getLibraryId());
        if (library == null || ObjectUtil.notEqual(
                PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus(), library.getStatus())) {
            return null;
        }
        // 2. 组装三类对象共用的互动展示字段
        PmsKnowledgeInteractionItemRespVO item = new PmsKnowledgeInteractionItemRespVO()
                .setId(target.getId()).setType(target.getType()).setEntityId(target.getEntityId())
                .setLibraryId(target.getLibraryId()).setLibraryName(library.getName())
                .setCreateTime(target.getCreateTime());
        // 3. 知识库对象必须与互动记录的知识库编号一致
        if (PmsKnowledgeObjectTypeEnum.LIBRARY.getType().equals(target.getType())) {
            return target.getEntityId().equals(target.getLibraryId())
                    ? item.setName(library.getName()).setDescription(library.getDescription())
                            .setTargetUpdateTime(library.getUpdateTime()) : null;
        }
        // 4. 文件夹对象必须归属当前知识库、状态正常且具有内容读取权限
        if (PmsKnowledgeObjectTypeEnum.FOLDER.getType().equals(target.getType())) {
            PmsKnowledgeFolderDO folder = folderMap.get(target.getEntityId());
            return folder != null && target.getLibraryId().equals(folder.getLibraryId())
                    && PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus().equals(folder.getStatus())
                    && readablePermissionIds.contains(folder.getPermissionId())
                    ? item.setName(folder.getTitle()).setFolderId(folder.getId())
                            .setTargetUpdateTime(folder.getUpdateTime()) : null;
        }
        // 5. 文档对象必须归属当前知识库、类型匹配、未回收且具有内容读取权限
        PmsKnowledgeDocumentDO document = documentMap.get(target.getEntityId());
        if (document == null || !target.getLibraryId().equals(document.getLibraryId())
                || PmsKnowledgeDocumentStatusEnum.RECYCLED.getStatus().equals(document.getStatus())
                || !target.getType().equals(document.getType())
                || !readablePermissionIds.contains(document.getPermissionId())) {
            return null;
        }
        return item.setName(document.getTitle()).setFolderId(document.getFolderId())
                .setDocumentId(document.getId()).setFileType(document.getFileType()).setFileSize(document.getFileSize())
                .setTargetUpdateTime(document.getUpdateTime());
    }

}
