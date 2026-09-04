package cn.iocoder.yudao.module.pms.controller.admin.kb.content;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentSearchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentUpdateReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentLikeDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeContentLevelEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeContentPermissionService;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentLikeService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeViewRecordService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识库文档")
@RestController
@RequestMapping("/pms/kb/document")
@Validated
public class PmsKnowledgeDocumentController {

    @Resource
    private PmsKnowledgeDocumentService documentService;
    @Resource
    private PmsKnowledgeContentPermissionService contentPermissionService;
    @Resource
    private PmsKnowledgeFavoriteService favoriteService;
    @Resource
    private PmsKnowledgeDocumentLikeService documentLikeService;
    @Resource
    private PmsKnowledgeViewRecordService viewRecordService;
    @Resource
    private PmsKnowledgeLibraryService libraryService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建文档")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Long> createDocument(@Valid @RequestBody PmsKnowledgeDocumentCreateReqVO createReqVO) {
        return success(documentService.createDocument(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文档内容")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateDocument(@Valid @RequestBody PmsKnowledgeDocumentUpdateReqVO updateReqVO) {
        documentService.updateDocument(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "将文档移入回收站")
    @Parameter(name = "id", description = "文档编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:delete')")
    public CommonResult<Boolean> deleteDocument(@RequestParam("id") Long id) {
        documentService.deleteDocument(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/move")
    @Operation(summary = "移动文档")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> moveDocument(@Valid @RequestBody PmsKnowledgeDocumentMoveReqVO moveReqVO) {
        documentService.moveDocument(moveReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文档详情")
    @Parameter(name = "id", description = "文档编号", required = true, example = "1024")
    @Parameter(name = "view", description = "是否记录本次浏览", example = "true")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PmsKnowledgeDocumentRespVO> getDocument(
            @RequestParam("id") Long id,
            @RequestParam(value = "view", defaultValue = "false") Boolean view) {
        // 查询文档
        Long userId = getLoginUserId();
        PmsKnowledgeDocumentDO document = documentService.getDocument(id, userId);
        // 记录浏览
        if (Boolean.TRUE.equals(view)) {
            viewRecordService.createViewRecord(document.getLibraryId(), document.getType(), document.getId(), userId);
        }
        // 构建文档 VO
        return success(buildDocumentRespVO(document, userId));
    }

    @GetMapping("/search-page")
    @Operation(summary = "使用 MySQL 全局搜索文档")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PageResult<PmsKnowledgeDocumentRespVO>> getDocumentSearchPage(
            @Valid PmsKnowledgeDocumentSearchPageReqVO pageReqVO) {
        PageResult<PmsKnowledgeDocumentDO> pageResult = documentService.getDocumentSearchPage(
                pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildDocumentRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private PmsKnowledgeDocumentRespVO buildDocumentRespVO(PmsKnowledgeDocumentDO document, Long userId) {
        // 1.1 拼接当前用户的内容权限和互动状态
        Integer currentUserLevel = contentPermissionService.getCurrentUserContentPermissionLevel(
                document.getPermissionId(), document.getLibraryId(), userId);
        Long creatorUserId = NumberUtils.parseLong(document.getCreator());
        PmsKnowledgeDocumentRespVO documentVO = BeanUtils.toBean(document, PmsKnowledgeDocumentRespVO.class)
                .setCreatorUserId(creatorUserId).setCurrentUserLevel(currentUserLevel)
                .setDownloadStatus(PmsKnowledgeContentLevelEnum.canDownload(currentUserLevel))
                .setPreviewUrl(document.getContent())
                .setFavoriteStatus(favoriteService.isFavorite(document.getType(), document.getId(), userId));
        // 1.2 文件不可下载时，隐藏实际文件地址
        if (PmsKnowledgeDocumentTypeEnum.FILE.getType().equals(document.getType())
                && Boolean.FALSE.equals(documentVO.getDownloadStatus())) {
            documentVO.setContent(null);
        }

        // 2. 一次查询并拼接创建人和点赞用户信息
        List<PmsKnowledgeDocumentLikeDO> likes = documentLikeService.getDocumentLikeList(document.getId());
        Set<Long> relatedUserIds = convertSet(likes, PmsKnowledgeDocumentLikeDO::getUserId);
        relatedUserIds.add(creatorUserId);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(relatedUserIds);
        findAndThen(userMap, creatorUserId, user -> documentVO.setCreatorUserName(user.getNickname()));
        documentVO.setLikeStatus(CollUtil.anyMatch(likes, like -> userId.equals(like.getUserId())))
                .setLikeUsers(convertList(likes, like -> {
                    PmsKnowledgeDocumentRespVO.LikeUser likeUser = new PmsKnowledgeDocumentRespVO.LikeUser()
                            .setId(like.getUserId());
                    findAndThen(userMap, like.getUserId(), user ->
                            likeUser.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
                    return likeUser;
                }));
        return documentVO;
    }

    private List<PmsKnowledgeDocumentRespVO> buildDocumentRespVOList(List<PmsKnowledgeDocumentDO> documents) {
        if (CollUtil.isEmpty(documents)) {
            return Collections.emptyList();
        }
        Map<Long, PmsKnowledgeLibraryDO> libraryMap = libraryService.getLibraryMap(
                convertSet(documents, PmsKnowledgeDocumentDO::getLibraryId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(documents,
                document -> NumberUtils.parseLong(document.getCreator())));
        return convertList(documents, document -> {
            PmsKnowledgeDocumentRespVO documentVO = BeanUtils.toBean(document, PmsKnowledgeDocumentRespVO.class)
                    .setContent(null).setContentSummary(buildContentSummary(document.getContent()))
                    .setCreatorUserId(NumberUtils.parseLong(document.getCreator()));
            findAndThen(libraryMap, document.getLibraryId(),
                    library -> documentVO.setLibraryName(library.getName()));
            findAndThen(userMap, documentVO.getCreatorUserId(),
                    user -> documentVO.setCreatorUserName(user.getNickname()));
            return documentVO;
        });
    }

    /**
     * 构造搜索结果摘要，避免直接返回完整正文
     */
    private String buildContentSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        String summary = content.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        return summary.length() > 120 ? summary.substring(0, 120) + "…" : summary;
    }

}
