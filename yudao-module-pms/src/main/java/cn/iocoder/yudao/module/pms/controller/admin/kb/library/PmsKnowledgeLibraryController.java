package cn.iocoder.yudao.module.pms.controller.admin.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibraryPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibraryRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibrarySaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeLibraryMemberLevelEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeFavoriteService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
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

@Tag(name = "管理后台 - PMS 知识库")
@RestController
@RequestMapping("/pms/kb/library")
@Validated
public class PmsKnowledgeLibraryController {

    @Resource
    private PmsKnowledgeLibraryService libraryService;
    @Resource
    private PmsKnowledgeLibraryMemberService memberService;
    @Resource
    private PmsKnowledgeDocumentService documentService;
    @Resource
    private PmsKnowledgeFavoriteService favoriteService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PermissionApi permissionApi;

    @PostMapping("/create")
    @Operation(summary = "创建知识库")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:create')")
    public CommonResult<Long> createLibrary(@Valid @RequestBody PmsKnowledgeLibrarySaveReqVO saveReqVO) {
        return success(libraryService.createLibrary(saveReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新知识库")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateLibrary(@Valid @RequestBody PmsKnowledgeLibrarySaveReqVO saveReqVO) {
        libraryService.updateLibrary(saveReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除知识库")
    @Parameter(name = "id", description = "知识库编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:delete')")
    public CommonResult<Boolean> deleteLibrary(@RequestParam("id") Long id) {
        libraryService.deleteLibrary(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得知识库详情")
    @Parameter(name = "id", description = "知识库编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PmsKnowledgeLibraryRespVO> getLibrary(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        PmsKnowledgeLibraryDO library = memberService.validateLibraryReadable(id, userId);
        return success(CollUtil.getFirst(buildLibraryRespVOList(Collections.singletonList(library), userId)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得知识库分页")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PageResult<PmsKnowledgeLibraryRespVO>> getLibraryPage(
            @Valid PmsKnowledgeLibraryPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        PageResult<PmsKnowledgeLibraryDO> pageResult = libraryService.getLibraryPage(pageReqVO, userId);
        return success(new PageResult<>(buildLibraryRespVOList(pageResult.getList(), userId), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<PmsKnowledgeLibraryRespVO> buildLibraryRespVOList(
            List<PmsKnowledgeLibraryDO> libraries, Long userId) {
        if (CollUtil.isEmpty(libraries)) {
            return Collections.emptyList();
        }

        // 1. 批量查询知识库成员、内容数量、创建人和当前用户权限信息
        Set<Long> libraryIds = convertSet(libraries, PmsKnowledgeLibraryDO::getId);
        Map<Long, List<PmsKnowledgeLibraryMemberDO>> memberListMap = memberService.getLibraryMemberListMap(libraryIds);
        Map<Long, Map<Integer, Long>> documentTypeCountMap = documentService.getDocumentTypeCountMap(libraryIds);
        Set<Long> userIds = convertSet(libraries, library -> NumberUtils.parseLong(library.getCreator()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        AdminUserRespDTO currentUser = adminUserApi.getUser(userId);
        Long deptId = currentUser != null ? currentUser.getDeptId() : null;
        boolean superAdmin = permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode());
        Set<Long> favoriteLibraryIds = favoriteService.getFavoriteEntityIdSet(
                PmsKnowledgeObjectTypeEnum.LIBRARY.getType(), libraryIds, userId);

        // 2. 拼接每个知识库的成员权限、统计数量、收藏状态和创建人名称
        return convertList(libraries, library -> {
            List<PmsKnowledgeLibraryMemberDO> members = memberListMap.getOrDefault(library.getId(), Collections.emptyList());
            PmsKnowledgeLibraryMemberDO directMember = CollUtil.findOne(members, member -> userId.equals(member.getUserId()));
            PmsKnowledgeLibraryMemberDO currentMember = directMember;
            if (currentMember == null && deptId != null) {
                currentMember = CollUtil.findOne(members, member -> deptId.equals(member.getDeptId()));
            }
            boolean adminStatus = superAdmin || currentMember != null
                    && (PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel().equals(currentMember.getLevel())
                    || PmsKnowledgeLibraryMemberLevelEnum.ADMIN.getLevel().equals(currentMember.getLevel()));
            PmsKnowledgeLibraryRespVO libraryVO = BeanUtils.toBean(library, PmsKnowledgeLibraryRespVO.class)
                    .setCreatorUserId(NumberUtils.parseLong(library.getCreator())).setMemberCount(members.size())
                    .setWriteStatus(superAdmin || currentMember != null).setAdminStatus(adminStatus)
                    .setExitStatus(directMember != null && !PmsKnowledgeLibraryMemberLevelEnum.CREATOR.getLevel().equals(directMember.getLevel()))
                    .setFavoriteStatus(favoriteLibraryIds.contains(library.getId()));
            Map<Integer, Long> typeCountMap = documentTypeCountMap.getOrDefault(library.getId(), Collections.emptyMap());
            libraryVO.setDocumentCount(typeCountMap.getOrDefault(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType(), 0L))
                    .setFileCount(typeCountMap.getOrDefault(PmsKnowledgeDocumentTypeEnum.FILE.getType(), 0L));
            findAndThen(userMap, libraryVO.getCreatorUserId(), user -> libraryVO.setCreatorUserName(user.getNickname()));
            return libraryVO;
        });
    }

}
