package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.motion.AiDramaMotionReferenceRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaAssetService;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaProjectService;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaScriptService;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaStoryboardService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 短剧项目")
@RestController
@RequestMapping("/ai/drama/project")
public class AiDramaProjectController {

    @Resource
    private AiDramaProjectService projectService;
    @Resource
    private AiDramaScriptService scriptService;
    @Resource
    private AiDramaAssetService assetService;
    @Resource
    private AiDramaStoryboardService storyboardService;

    @Resource
    private AdminUserApi adminUserApi;

    // ================ 我的项目 ================

    @PostMapping("/create")
    @Operation(summary = "创建项目")
    public CommonResult<Long> createProject(@Valid @RequestBody AiDramaProjectSaveReqVO createReqVO) {
        return success(projectService.createProject(getLoginUserId(), createReqVO));
    }

    @PutMapping("/update-my")
    @Operation(summary = "修改【我的】项目")
    public CommonResult<Boolean> updateProjectMy(@Valid @RequestBody AiDramaProjectSaveReqVO updateReqVO) {
        projectService.updateProjectMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】项目")
    @Parameter(name = "id", required = true, description = "项目编号", example = "1024")
    public CommonResult<Boolean> deleteProjectMy(@RequestParam("id") Long id) {
        projectService.deleteProjectMy(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】项目分页")
    public CommonResult<PageResult<AiDramaProjectRespVO>> getProjectMyPage(@Valid AiDramaProjectMyPageReqVO pageReqVO) {
        PageResult<AiDramaProjectDO> pageResult = projectService.getProjectMyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildProjectList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get-detail-my")
    @Operation(summary = "获得【我的】项目详情", description = "包含剧本、资产、分镜，用于工作台")
    @Parameter(name = "id", required = true, description = "项目编号", example = "1024")
    public CommonResult<AiDramaProjectDetailRespVO> getProjectDetailMy(@RequestParam("id") Long id) {
        AiDramaProjectDO project = projectService.validateProjectMy(id, getLoginUserId());
        return success(buildProjectDetail(project));
    }

    @PostMapping("/compose")
    @Operation(summary = "合成成片", description = "异步合成，前端轮询项目详情的成片状态")
    public CommonResult<Boolean> composeProject(@Valid @RequestBody AiDramaComposeReqVO reqVO) {
        projectService.composeProjectMy(getLoginUserId(), reqVO);
        return success(true);
    }

    // ================ 公开广场 ================

    @GetMapping("/public-page")
    @Operation(summary = "获得公开项目分页", description = "同租户已公开、成片已完成的项目")
    public CommonResult<PageResult<AiDramaPublicRespVO>> getProjectPublicPage(@Valid AiDramaProjectPublicPageReqVO pageReqVO) {
        PageResult<AiDramaProjectDO> pageResult = projectService.getProjectPublicPage(pageReqVO);
        return success(new PageResult<>(buildPublicList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/public-detail")
    @Operation(summary = "获得公开项目详情", description = "只返回成片、梗概、角色等展示信息")
    @Parameter(name = "id", required = true, description = "项目编号", example = "1024")
    public CommonResult<AiDramaPublicRespVO> getProjectPublicDetail(@RequestParam("id") Long id) {
        AiDramaProjectDO project = projectService.getProjectPublic(id);
        AiDramaPublicRespVO respVO = CollUtil.getFirst(buildPublicList(Collections.singletonList(project)));
        // 梗概、角色（取第 1 个形象选用的图，没有时取参考图）
        AiDramaScriptDO script = scriptService.getScriptByProjectId(id);
        respVO.setOutline(script != null ? script.getOutline() : null);
        List<AiDramaCharacterDO> characters = assetService.getCharacterList(id);
        Map<Long, List<AiDramaCharacterAppearanceDO>> appearanceMap = assetService.getAppearanceMapByCharacterIds(
                convertList(characters, AiDramaCharacterDO::getId));
        respVO.setCharacters(convertList(characters, character -> {
            AiDramaCharacterAppearanceDO appearance = CollUtil.getFirst(appearanceMap.get(character.getId()));
            String imageUrl = appearance != null ? getSelectedUrl(appearance.getImageUrls(), appearance.getSelectedIndex()) : null;
            return new AiDramaPublicRespVO.Cast().setName(character.getName())
                    .setImageUrl(StrUtil.blankToDefault(imageUrl, character.getReferenceImageUrl()));
        }));
        return success(respVO);
    }

    // ================ 管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得项目分页", description = "用于【短剧制作管理】")
    @PreAuthorize("@ss.hasPermission('ai:drama:query')")
    public CommonResult<PageResult<AiDramaProjectRespVO>> getProjectPage(@Valid AiDramaProjectPageReqVO pageReqVO) {
        PageResult<AiDramaProjectDO> pageResult = projectService.getProjectPage(pageReqVO);
        return success(new PageResult<>(buildProjectList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得项目详情", description = "用于【短剧制作管理】只读查看")
    @Parameter(name = "id", required = true, description = "项目编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:drama:query')")
    public CommonResult<AiDramaProjectDetailRespVO> getProjectDetail(@RequestParam("id") Long id) {
        return success(buildProjectDetail(projectService.validateProjectExists(id)));
    }

    @PutMapping("/update-public")
    @Operation(summary = "修改项目的公开状态", description = "用于【短剧制作管理】")
    @PreAuthorize("@ss.hasPermission('ai:drama:update')")
    public CommonResult<Boolean> updateProjectPublicStatus(@Valid @RequestBody AiDramaProjectUpdatePublicReqVO updateReqVO) {
        projectService.updateProjectPublicStatus(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目", description = "用于【短剧制作管理】")
    @Parameter(name = "id", required = true, description = "项目编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:drama:delete')")
    public CommonResult<Boolean> deleteProject(@RequestParam("id") Long id) {
        projectService.deleteProject(id);
        return success(true);
    }

    // ================ 构建 ================

    private List<AiDramaProjectRespVO> buildProjectList(List<AiDramaProjectDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiDramaProjectDO::getUserId));
        Map<Long, List<AiDramaStoryboardDO>> storyboardMap = storyboardService.getStoryboardMapByProjectIds(
                convertSet(list, AiDramaProjectDO::getId));
        Map<Long, Long> scriptIdMap = scriptService.getLatestScriptIdMap(convertSet(list, AiDramaProjectDO::getId));
        return BeanUtils.toBean(list, AiDramaProjectRespVO.class, project -> {
            MapUtils.findAndThen(userMap, project.getUserId(), user -> project.setUserNickname(user.getNickname()));
            // 镜头数只统计当前剧本版本的分镜，与详情、合成一致
            Long scriptId = scriptIdMap.get(project.getId());
            List<AiDramaStoryboardDO> storyboards = filterList(storyboardMap.getOrDefault(project.getId(), Collections.emptyList()),
                    storyboard -> Objects.equals(storyboard.getScriptId(), scriptId));
            project.setStoryboardCount(storyboards.size()).setStoryboardSuccessCount((int) count(storyboards,
                    storyboard -> Objects.equals(storyboard.getVideoStatus(), AiDramaStatusEnum.SUCCESS.getStatus())));
        });
    }

    private List<AiDramaPublicRespVO> buildPublicList(List<AiDramaProjectDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Long loginUserId = getLoginUserId();
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiDramaProjectDO::getUserId));
        // 画幅、镜头数使用成片实际的，不使用项目当前的设置
        return convertList(list, project -> BeanUtils.toBean(project, AiDramaPublicRespVO.class, respVO -> {
            respVO.setAspectRatio(project.getComposeAspectRatio()).setStoryboardCount(project.getComposeStoryboardCount())
                    .setOwner(Objects.equals(project.getUserId(), loginUserId));
            MapUtils.findAndThen(userMap, project.getUserId(), user -> respVO.setUserNickname(user.getNickname()));
        }));
    }

    /**
     * 构建项目详情
     *
     * @param project 项目
     */
    private AiDramaProjectDetailRespVO buildProjectDetail(AiDramaProjectDO project) {
        // 1. 角色：带形象、样音
        List<AiDramaCharacterDO> characters = assetService.getCharacterList(project.getId());
        Map<Long, List<AiDramaCharacterAppearanceDO>> appearanceMap = assetService.getAppearanceMapByCharacterIds(
                convertList(characters, AiDramaCharacterDO::getId));
        Map<Long, List<AiDramaAudioDO>> audioMap = assetService.getAudioMapByProjectId(project.getId());
        // 1.2 剧本、分镜：分镜只取当前剧本版本的；历史版本的分镜保留，不展示
        AiDramaScriptDO script = scriptService.getScriptByProjectId(project.getId());
        Long scriptId = script != null ? script.getId() : null;
        List<AiDramaStoryboardDO> storyboards = storyboardService.getStoryboardList(project.getId());
        // 1.3 成片是否需要重新合成：按项目全部版本的分镜判断（成片可能使用旧版本剧本的镜头）
        String composeStaleReason = projectService.getComposeStaleReason(project, storyboards, scriptId);
        AiDramaProjectRespVO projectVO = CollUtil.getFirst(buildProjectList(Collections.singletonList(project)))
                .setComposeStale(composeStaleReason != null).setComposeStaleReason(composeStaleReason);

        // 2. 拼接
        return new AiDramaProjectDetailRespVO()
                .setProject(projectVO)
                .setScript(BeanUtils.toBean(script, AiDramaScriptRespVO.class))
                .setCharacters(BeanUtils.toBean(characters, AiDramaCharacterRespVO.class, character -> character
                        .setAppearances(BeanUtils.toBean(appearanceMap.getOrDefault(character.getId(), Collections.emptyList()),
                                AiDramaAppearanceRespVO.class))
                        .setAudios(BeanUtils.toBean(audioMap.getOrDefault(character.getId(), Collections.emptyList()),
                                AiDramaAudioRespVO.class))))
                .setLocations(BeanUtils.toBean(assetService.getLocationList(project.getId()), AiDramaLocationRespVO.class))
                .setVisualAssets(BeanUtils.toBean(assetService.getVisualAssetList(project.getId()), AiDramaVisualAssetRespVO.class))
                .setAudios(BeanUtils.toBean(filterList(assetService.getAudioList(project.getId()), audio ->
                        audio.getCharacterId() == null), AiDramaAudioRespVO.class))
                .setMotionReferences(BeanUtils.toBean(assetService.getMotionReferenceList(project.getId()),
                        AiDramaMotionReferenceRespVO.class))
                .setStoryboards(BeanUtils.toBean(filterList(storyboards,
                        storyboard -> Objects.equals(storyboard.getScriptId(), scriptId)), AiDramaStoryboardRespVO.class));
    }

    private static String getSelectedUrl(List<String> imageUrls, Integer selectedIndex) {
        if (CollUtil.isEmpty(imageUrls) || selectedIndex == null || selectedIndex < 0 || selectedIndex >= imageUrls.size()) {
            return null;
        }
        return imageUrls.get(selectedIndex);
    }

}
