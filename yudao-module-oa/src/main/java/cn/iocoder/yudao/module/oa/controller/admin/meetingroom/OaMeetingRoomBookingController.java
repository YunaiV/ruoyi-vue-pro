package cn.iocoder.yudao.module.oa.controller.admin.meetingroom;

import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking.*;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.*;
import cn.iocoder.yudao.module.oa.service.meetingroom.*;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 会议室预定")
@RestController
@RequestMapping("/oa/meeting-room-booking")
@Validated
public class OaMeetingRoomBookingController {

    @Resource
    private OaMeetingRoomBookingService meetingRoomBookingService;
    @Resource
    private OaMeetingRoomService meetingRoomService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建会议室预定草稿")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:create')")
    public CommonResult<Long> createMeetingRoomBooking(@Valid @RequestBody OaMeetingRoomBookingSaveReqVO reqVO) {
        return success(meetingRoomBookingService.createMeetingRoomBooking(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会议室预定草稿")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:update')")
    public CommonResult<Boolean> updateMeetingRoomBooking(@Valid @RequestBody OaMeetingRoomBookingSaveReqVO reqVO) {
        meetingRoomBookingService.updateMeetingRoomBooking(reqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会议室预定草稿")
    @Parameter(name = "id", description = "预定编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:delete')")
    public CommonResult<Boolean> deleteMeetingRoomBooking(@RequestParam("id") Long id) {
        meetingRoomBookingService.deleteMeetingRoomBooking(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会议室预定详情")
    @Parameter(name = "id", description = "预定编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:query')")
    public CommonResult<OaMeetingRoomBookingRespVO> getMeetingRoomBooking(@RequestParam("id") Long id) {
        OaMeetingRoomBookingDO booking = meetingRoomBookingService.getMeetingRoomBooking(id);
        return success(buildMeetingRoomBookingRespVO(booking));
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人的会议室预定分页")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:query')")
    public CommonResult<PageResult<OaMeetingRoomBookingRespVO>> getMeetingRoomBookingPage(@Valid OaMeetingRoomBookingPageReqVO reqVO) {
        PageResult<OaMeetingRoomBookingDO> pageResult = meetingRoomBookingService.getMeetingRoomBookingPage(getLoginUserId(), reqVO);
        return success(new PageResult<>(buildMeetingRoomBookingRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交会议室预定")
    @Parameter(name = "id", description = "预定编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:create')")
    public CommonResult<Boolean> submitMeetingRoomBooking(@RequestParam("id") Long id) {
        meetingRoomBookingService.submitMeetingRoomBooking(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消会议室预定")
    @Parameter(name = "id", description = "预定编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:update')")
    public CommonResult<Boolean> cancelMeetingRoomBooking(@RequestParam("id") Long id) {
        meetingRoomBookingService.cancelMeetingRoomBooking(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/start")
    @Operation(summary = "开始使用会议室预定")
    @Parameter(name = "id", description = "预定编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:update')")
    public CommonResult<Boolean> startMeetingRoomBooking(@RequestParam("id") Long id) {
        meetingRoomBookingService.startMeetingRoomBooking(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成使用会议室预定")
    @Parameter(name = "id", description = "预定编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room-booking:update')")
    public CommonResult<Boolean> finishMeetingRoomBooking(@RequestParam("id") Long id) {
        meetingRoomBookingService.finishMeetingRoomBooking(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/schedule")
    @Operation(summary = "获得会议室日程")
    @PreAuthorize("@ss.hasAnyPermissions('oa:meeting-room:query', 'oa:meeting-room-booking:query')")
    public CommonResult<List<OaMeetingRoomBookingRespVO>> getMeetingRoomBookingListBySchedule(@Valid OaMeetingRoomBookingScheduleReqVO reqVO) {
        List<OaMeetingRoomBookingDO> bookings = meetingRoomBookingService.getMeetingRoomBookingListBySchedule(reqVO);
        return success(buildMeetingRoomBookingRespVOList(bookings));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接会议室预定详情
     *
     * @param booking 会议室预定
     * @return 会议室预定响应
     */
    private OaMeetingRoomBookingRespVO buildMeetingRoomBookingRespVO(OaMeetingRoomBookingDO booking) {
        if (booking == null) {
            return null;
        }
        return CollUtil.getFirst(buildMeetingRoomBookingRespVOList(Collections.singletonList(booking)));
    }

    /**
     * 构建会议室预定响应列表
     *
     * @param bookings 会议室预定列表
     * @return 响应列表
     */
    private List<OaMeetingRoomBookingRespVO> buildMeetingRoomBookingRespVOList(List<OaMeetingRoomBookingDO> bookings) {
        if (CollUtil.isEmpty(bookings)) {
            return Collections.emptyList();
        }
        // 1.1 批量查询申请人、主持人和参会人
        Set<Long> userIds = convertSet(bookings, OaMeetingRoomBookingDO::getModeratorUserId);
        userIds.addAll(convertSet(bookings, booking -> Long.valueOf(booking.getCreator()),
                booking -> NumberUtil.isLong(booking.getCreator())));
        userIds.addAll(convertSetByFlatMap(bookings, OaMeetingRoomBookingDO::getAttendeeUserIds, Collection::stream));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 1.2 批量查询房间及申请部门
        Map<Long, OaMeetingRoomDO> roomMap = meetingRoomService.getMeetingRoomMap(
                convertSet(bookings, OaMeetingRoomBookingDO::getRoomId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(bookings, OaMeetingRoomBookingDO::getDeptId));
        // 2. 转换并拼接展示字段
        return convertList(bookings, booking -> {
            OaMeetingRoomBookingRespVO vo = BeanUtils.toBean(booking, OaMeetingRoomBookingRespVO.class);
            MapUtils.findAndThen(roomMap, booking.getRoomId(), room ->
                    vo.setRoomName(room.getName()).setRoomLocation(room.getLocation()).setRoomType(room.getType()));
            MapUtils.findAndThen(userMap, booking.getModeratorUserId(), user -> vo.setModeratorName(user.getNickname()));
            if (NumberUtil.isLong(booking.getCreator())) {
                MapUtils.findAndThen(userMap, Long.valueOf(booking.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            }
            MapUtils.findAndThen(deptMap, booking.getDeptId(), dept -> vo.setDeptName(dept.getName()));
            vo.setAttendeeNames(convertList(booking.getAttendeeUserIds(), userId ->
                    userMap.containsKey(userId) ? userMap.get(userId).getNickname() : userId.toString()));
            return vo;
        });
    }

}
