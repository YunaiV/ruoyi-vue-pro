package cn.iocoder.yudao.module.oa.controller.admin.meetingroom;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.*;
import cn.iocoder.yudao.module.oa.service.meetingroom.*;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 会议室")
@RestController
@RequestMapping("/oa/meeting-room")
@Validated
public class OaMeetingRoomController {

    @Resource
    private OaMeetingRoomService meetingRoomService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建会议室")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room:create')")
    public CommonResult<Long> createMeetingRoom(@Valid @RequestBody OaMeetingRoomSaveReqVO reqVO) {
        return success(meetingRoomService.createMeetingRoom(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会议室")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room:update')")
    public CommonResult<Boolean> updateMeetingRoom(@Valid @RequestBody OaMeetingRoomSaveReqVO reqVO) {
        meetingRoomService.updateMeetingRoom(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会议室")
    @Parameter(name = "id", description = "会议室编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room:delete')")
    public CommonResult<Boolean> deleteMeetingRoom(@RequestParam("id") Long id) {
        meetingRoomService.deleteMeetingRoom(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会议室详情")
    @Parameter(name = "id", description = "会议室编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room:query')")
    public CommonResult<OaMeetingRoomRespVO> getMeetingRoom(@RequestParam("id") Long id) {
        OaMeetingRoomDO room = meetingRoomService.validateMeetingRoomExists(id);
        return success(buildMeetingRoomRespVO(room));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会议室分页")
    @PreAuthorize("@ss.hasPermission('oa:meeting-room:query')")
    public CommonResult<PageResult<OaMeetingRoomRespVO>> getMeetingRoomPage(@Valid OaMeetingRoomPageReqVO reqVO) {
        PageResult<OaMeetingRoomDO> pageResult = meetingRoomService.getMeetingRoomPage(reqVO);
        return success(new PageResult<>(buildMeetingRoomRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/bookable-page")
    @Operation(summary = "获得可预定会议室分页")
    public CommonResult<PageResult<OaMeetingRoomRespVO>> getMeetingRoomPageByBookingUserId(@Valid OaMeetingRoomPageReqVO reqVO) {
        PageResult<OaMeetingRoomDO> pageResult = meetingRoomService.getMeetingRoomPageByBookingUserId(reqVO, getLoginUserId());
        List<OaMeetingRoomRespVO> list = buildMeetingRoomRespVOList(pageResult.getList());
        return success(new PageResult<>(convertList(list, room -> new OaMeetingRoomRespVO()
                .setId(room.getId()).setName(room.getName()).setLocation(room.getLocation())
                .setType(room.getType()).setStatus(room.getStatus()).setSeatCount(room.getSeatCount())
                .setManagerName(room.getManagerName())), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 构建会议室响应
     *
     * @param room 会议室
     * @return 会议室响应
     */
    private OaMeetingRoomRespVO buildMeetingRoomRespVO(OaMeetingRoomDO room) {
        AdminUserRespDTO user = adminUserApi.getUser(room.getManagerUserId());
        OaMeetingRoomRespVO respVO = BeanUtils.toBean(room, OaMeetingRoomRespVO.class);
        if (user != null) {
            respVO.setManagerName(user.getNickname()).setManagerPhone(user.getMobile());
        }
        return respVO;
    }

    /**
     * 构建会议室响应列表
     *
     * @param rooms 会议室列表
     * @return 响应列表
     */
    private List<OaMeetingRoomRespVO> buildMeetingRoomRespVOList(List<OaMeetingRoomDO> rooms) {
        if (CollUtil.isEmpty(rooms)) {
            return Collections.emptyList();
        }
        // 1. 批量查询管理员
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(rooms, OaMeetingRoomDO::getManagerUserId));
        // 2. 转换并拼接管理员姓名、电话
        return convertList(rooms, room -> {
            OaMeetingRoomRespVO vo = BeanUtils.toBean(room, OaMeetingRoomRespVO.class);
            MapUtils.findAndThen(userMap, room.getManagerUserId(), user ->
                    vo.setManagerName(user.getNickname()).setManagerPhone(user.getMobile()));
            return vo;
        });
    }

}
