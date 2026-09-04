package cn.iocoder.yudao.module.pms.controller.admin.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.announcement.PmsProjectAnnouncementRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.announcement.PmsProjectAnnouncementSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectAnnouncementDO;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectAnnouncementService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 项目公告")
@RestController
@RequestMapping("/pms/pm/project-announcement")
@Validated
public class PmsProjectAnnouncementController {

    @Resource
    private PmsProjectAnnouncementService projectAnnouncementService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建项目公告")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:update')")
    public CommonResult<Long> createProjectAnnouncement(@Valid @RequestBody PmsProjectAnnouncementSaveReqVO saveReqVO) {
        return success(projectAnnouncementService.createProjectAnnouncement(saveReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目公告")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:update')")
    public CommonResult<Boolean> updateProjectAnnouncement(
            @Valid @RequestBody PmsProjectAnnouncementSaveReqVO saveReqVO) {
        projectAnnouncementService.updateProjectAnnouncement(saveReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目公告")
    @Parameter(name = "id", description = "公告编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:update')")
    public CommonResult<Boolean> deleteProjectAnnouncement(@RequestParam("id") Long id) {
        projectAnnouncementService.deleteProjectAnnouncement(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目公告详情")
    @Parameter(name = "id", description = "公告编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:query')")
    public CommonResult<PmsProjectAnnouncementRespVO> getProjectAnnouncement(@RequestParam("id") Long id) {
        PmsProjectAnnouncementDO announcement = projectAnnouncementService
                .getProjectAnnouncement(id, getLoginUserId());
        return success(buildProjectAnnouncementRespVO(announcement));
    }

    @GetMapping("/list")
    @Operation(summary = "获得项目公告列表")
    @Parameter(name = "projectId", description = "项目编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project:query')")
    public CommonResult<List<PmsProjectAnnouncementRespVO>> getProjectAnnouncementList(
            @RequestParam("projectId") Long projectId) {
        List<PmsProjectAnnouncementDO> announcements = projectAnnouncementService
                .getProjectAnnouncementList(projectId, getLoginUserId());
        List<PmsProjectAnnouncementRespVO> announcementVOList = buildProjectAnnouncementRespVOList(announcements);
        return success(announcementVOList);
    }

    // ==================== 拼接 VO ====================

    /**
     * 构建项目公告响应
     *
     * @param announcement 项目公告
     * @return 项目公告响应
     */
    private PmsProjectAnnouncementRespVO buildProjectAnnouncementRespVO(PmsProjectAnnouncementDO announcement) {
        return CollUtil.getFirst(buildProjectAnnouncementRespVOList(Collections.singletonList(announcement)));
    }

    /**
     * 构建项目公告响应列表
     *
     * @param announcements 项目公告列表
     * @return 项目公告响应列表
     */
    private List<PmsProjectAnnouncementRespVO> buildProjectAnnouncementRespVOList(
            List<PmsProjectAnnouncementDO> announcements) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(announcements,
                announcement -> NumberUtils.parseLong(announcement.getCreator())));
        return convertList(announcements, announcement -> {
            PmsProjectAnnouncementRespVO announcementVO = BeanUtils.toBean(
                    announcement, PmsProjectAnnouncementRespVO.class)
                    .setCreatorUserId(NumberUtils.parseLong(announcement.getCreator()));
            findAndThen(userMap, announcementVO.getCreatorUserId(),
                    user -> announcementVO.setCreatorUserName(user.getNickname()));
            return announcementVO;
        });
    }

}
