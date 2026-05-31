package cn.iocoder.yudao.module.im.controller.admin.manager.rtc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.rtc.vo.ImRtcCallManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.rtc.vo.ImRtcCallManagerRespVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.rtc.vo.ImRtcParticipantManagerRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.rtc.ImRtcCallService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - IM 通话记录")
@RestController
@RequestMapping("/im/manager/rtc")
@Validated
public class ImRtcCallManagerController {

    @Resource
    private ImRtcCallService rtcCallService;
    @Resource
    private ImGroupService groupService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得通话记录分页")
    @PreAuthorize("@ss.hasPermission('im:manager:rtc:query')")
    public CommonResult<PageResult<ImRtcCallManagerRespVO>> getCallPage(@Valid ImRtcCallManagerPageReqVO pageReqVO) {
        PageResult<ImRtcCallDO> pageResult = rtcCallService.getCallPage(pageReqVO);
        return success(buildCallRespVOPage(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "获得通话记录详情")
    @Parameter(name = "id", description = "通话编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:rtc:query')")
    public CommonResult<ImRtcCallManagerRespVO> getCall(@RequestParam("id") Long id) {
        ImRtcCallDO call = rtcCallService.getCall(id);
        if (call == null) {
            return success(null);
        }
        return success(CollUtil.getFirst(buildCallRespVOList(Collections.singletonList(call))));
    }

    @GetMapping("/participant-list")
    @Operation(summary = "获得通话参与者列表")
    @Parameter(name = "id", description = "通话编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:rtc:query')")
    public CommonResult<List<ImRtcParticipantManagerRespVO>> getCallParticipantList(@RequestParam("id") Long id) {
        List<ImRtcParticipantDO> participants = rtcCallService.getCallParticipantListByCallId(id);
        if (CollUtil.isEmpty(participants)) {
            return success(Collections.emptyList());
        }
        // 查询用户信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(participants, ImRtcParticipantDO::getUserId));
        // 组装返回
        return success(BeanUtils.toBean(participants, ImRtcParticipantManagerRespVO.class, vo ->
                MapUtils.findAndThen(userMap, vo.getUserId(),
                        user -> vo.setUserNickname(user.getNickname()))));
    }

    // ========== 私有方法：VO 组装 ==========

    private PageResult<ImRtcCallManagerRespVO> buildCallRespVOPage(PageResult<ImRtcCallDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(buildCallRespVOList(pageResult.getList()), pageResult.getTotal());
    }

    private List<ImRtcCallManagerRespVO> buildCallRespVOList(List<ImRtcCallDO> calls) {
        // 查询用户信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(calls, ImRtcCallDO::getInviterUserId));
        Map<Long, ImGroupDO> groupMap = groupService.getGroupMap(
                convertSet(calls, ImRtcCallDO::getGroupId));
        // 组装返回
        return BeanUtils.toBean(calls, ImRtcCallManagerRespVO.class, vo -> {
            MapUtils.findAndThen(userMap, vo.getInviterUserId(),
                    user -> vo.setInviterNickname(user.getNickname()));
            MapUtils.findAndThen(groupMap, vo.getGroupId(),
                    group -> vo.setGroupName(group.getName()));
        });
    }

}
