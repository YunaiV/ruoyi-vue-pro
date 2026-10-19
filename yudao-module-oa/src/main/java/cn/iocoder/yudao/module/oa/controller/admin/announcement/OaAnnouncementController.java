package cn.iocoder.yudao.module.oa.controller.admin.announcement;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementReceiverDO;
import cn.iocoder.yudao.module.oa.service.announcement.OaAnnouncementService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 公告")
@RestController
@RequestMapping("/oa/announcement")
@Validated
public class OaAnnouncementController {

    @Resource
    private OaAnnouncementService announcementService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    // ==================== 我发布的 ====================

    @PostMapping("/create")
    @Operation(summary = "创建公告")
    @PreAuthorize("@ss.hasPermission('oa:announcement:create')")
    public CommonResult<Long> createAnnouncement(@Valid @RequestBody OaAnnouncementSaveReqVO createReqVO) {
        return success(announcementService.createAnnouncement(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新公告")
    @PreAuthorize("@ss.hasPermission('oa:announcement:update')")
    public CommonResult<Boolean> updateAnnouncement(@Valid @RequestBody OaAnnouncementSaveReqVO updateReqVO) {
        announcementService.updateAnnouncement(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除发布的公告")
    @Parameter(name = "id", description = "公告编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:announcement:delete')")
    public CommonResult<Boolean> deleteAnnouncement(@RequestParam("id") Long id) {
        announcementService.deleteAnnouncement(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/published-page")
    @Operation(summary = "获得我发布的公告分页")
    @PreAuthorize("@ss.hasPermission('oa:announcement:query')")
    public CommonResult<PageResult<OaAnnouncementRespVO>> getPublishedAnnouncementPage(
            @Valid OaAnnouncementPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        PageResult<OaAnnouncementDO> pageResult = announcementService
                .getPublishedAnnouncementPage(pageReqVO, userId);
        return success(new PageResult<>(buildAnnouncementRespVOList(pageResult.getList(), userId),
                pageResult.getTotal()));
    }

    // ==================== 我收到的 ====================

    @DeleteMapping("/delete-received")
    @Operation(summary = "删除接收的公告")
    @Parameter(name = "id", description = "公告编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteReceivedAnnouncement(@RequestParam("id") Long id) {
        announcementService.deleteReceivedAnnouncement(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/received-page")
    @Operation(summary = "获得接收的公告分页")
    @PreAuthorize("@ss.hasPermission('oa:announcement:query')")
    public CommonResult<PageResult<OaAnnouncementRespVO>> getReceivedAnnouncementPage(
            @Valid OaAnnouncementPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        PageResult<OaAnnouncementDO> pageResult = announcementService
                .getReceivedAnnouncementPage(pageReqVO, userId);
        return success(new PageResult<>(buildAnnouncementRespVOList(pageResult.getList(), userId),
                pageResult.getTotal()));
    }

    @PutMapping("/update-read-status")
    @Operation(summary = "标记公告为已读")
    @Parameter(name = "id", description = "公告编号", required = true, example = "1024")
    public CommonResult<Boolean> updateAnnouncementReadStatus(@RequestParam("id") Long id) {
        announcementService.updateAnnouncementReadStatus(id, getLoginUserId());
        return success(true);
    }

    @PostMapping("/forward")
    @Operation(summary = "转发公告给下属")
    @Parameter(name = "id", description = "公告编号", required = true, example = "1024")
    public CommonResult<Integer> forwardAnnouncement(@RequestParam("id") Long id) {
        return success(announcementService.forwardAnnouncement(id, getLoginUserId()));
    }

    // ==================== 公共详情 ====================

    @GetMapping("/get")
    @Operation(summary = "获得公告详情")
    @Parameter(name = "id", description = "公告编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:announcement:query')")
    public CommonResult<OaAnnouncementRespVO> getAnnouncement(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        OaAnnouncementDO announcement = announcementService.getAnnouncement(id, userId);
        return success(buildAnnouncementRespVO(announcement, userId));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接公告详情
     *
     * @param announcement 公告
     * @param loginUserId 当前登录用户编号
     * @return 公告响应
     */
    private OaAnnouncementRespVO buildAnnouncementRespVO(OaAnnouncementDO announcement, Long loginUserId) {
        if (announcement == null) {
            return null;
        }
        return CollUtil.getFirst(buildAnnouncementRespVOList(Collections.singletonList(announcement), loginUserId));
    }

    /**
     * 拼接公告响应列表
     *
     * @param announcements 公告列表
     * @param loginUserId 当前登录用户编号
     * @return 公告响应列表
     */
    private List<OaAnnouncementRespVO> buildAnnouncementRespVOList(List<OaAnnouncementDO> announcements,
                                                                    Long loginUserId) {
        if (CollUtil.isEmpty(announcements)) {
            return Collections.emptyList();
        }
        // 1. 查询公告接收关系、发布人、接收人和发布人部门信息
        Map<Long, List<OaAnnouncementReceiverDO>> receiverListMap = announcementService
                .getAnnouncementReceiverListMap(convertSet(announcements, OaAnnouncementDO::getId));
        Collection<Long> userIds = new ArrayList<>(convertSet(announcements,
                announcement -> NumberUtils.parseLong(announcement.getCreator())));
        receiverListMap.values().forEach(receivers -> userIds.addAll(
                convertSet(receivers, OaAnnouncementReceiverDO::getReceiverUserId)));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        Set<Long> subordinateUserIds = convertSet(adminUserApi.getUserListBySubordinate(loginUserId), AdminUserRespDTO::getId);
        // 2. 拼接公告、发布人、接收人和当前接收状态
        return convertList(announcements, announcementDO -> {
            OaAnnouncementRespVO announcement = BeanUtils.toBean(announcementDO, OaAnnouncementRespVO.class)
                    .setPublisherUserId(NumberUtils.parseLong(announcementDO.getCreator()));
            MapUtils.findAndThen(userMap, announcement.getPublisherUserId(), publisher -> {
                announcement.setPublisherUserName(publisher.getNickname());
                announcement.setPublisherDeptId(publisher.getDeptId());
                MapUtils.findAndThen(deptMap, publisher.getDeptId(),
                        dept -> announcement.setPublisherDeptName(dept.getName()));
            });
            List<OaAnnouncementReceiverDO> receivers = receiverListMap.getOrDefault(announcement.getId(),
                    Collections.emptyList());
            List<Long> receiverUserIds = convertList(receivers, OaAnnouncementReceiverDO::getReceiverUserId);
            announcement.setReceiverUserIds(receiverUserIds);
            announcement.setReceiverUserNames(convertList(receiverUserIds, receiverUserId -> {
                AdminUserRespDTO receiverUser = userMap.get(receiverUserId);
                return receiverUser != null ? receiverUser.getNickname() : null;
            }));
            OaAnnouncementReceiverDO loginUserReceiver = CollUtil.findOne(receivers,
                    receiver -> receiver.getReceiverUserId().equals(loginUserId));
            if (loginUserReceiver != null) {
                announcement.setReadStatus(loginUserReceiver.getReadStatus());
            }
            announcement.setForwarded(CollUtil.containsAny(receiverUserIds, subordinateUserIds));
            return announcement;
        });
    }

}
