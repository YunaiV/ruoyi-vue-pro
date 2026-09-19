package cn.iocoder.yudao.module.oa.controller.admin.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.schedule.vo.OaSchedulePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.schedule.vo.OaScheduleRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.schedule.vo.OaScheduleSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.schedule.OaScheduleDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.schedule.OaScheduleParticipantDO;
import cn.iocoder.yudao.module.oa.service.schedule.OaScheduleService;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 日程")
@RestController
@RequestMapping("/oa/schedule")
@Validated
public class OaScheduleController {

    @Resource
    private OaScheduleService scheduleService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建日程")
    @PreAuthorize("@ss.hasPermission('oa:schedule:create')")
    public CommonResult<Long> createSchedule(@Valid @RequestBody OaScheduleSaveReqVO createReqVO) {
        return success(scheduleService.createSchedule(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新日程")
    @PreAuthorize("@ss.hasPermission('oa:schedule:update')")
    public CommonResult<Boolean> updateSchedule(@Valid @RequestBody OaScheduleSaveReqVO updateReqVO) {
        scheduleService.updateSchedule(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除日程")
    @Parameter(name = "id", description = "日程编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:schedule:delete')")
    public CommonResult<Boolean> deleteSchedule(@RequestParam("id") Long id) {
        scheduleService.deleteSchedule(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得日程详情")
    @Parameter(name = "id", description = "日程编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:schedule:query')")
    public CommonResult<OaScheduleRespVO> getSchedule(@RequestParam("id") Long id) {
        // 1. 查询日程及参与人
        OaScheduleDO schedule = scheduleService.getSchedule(id, getLoginUserId());
        List<OaScheduleParticipantDO> participants = scheduleService.getScheduleParticipantList(id);
        // 2. 转换详情并拼接参与人阅读信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(participants, OaScheduleParticipantDO::getUserId));
        OaScheduleRespVO respVO = CollUtil.getFirst(buildScheduleRespVOList(Collections.singletonList(schedule)));
        respVO.setParticipants(convertList(participants, participant -> {
            OaScheduleRespVO.Participant participantVO = BeanUtils.toBean(participant, OaScheduleRespVO.Participant.class);
            MapUtils.findAndThen(userMap, participant.getUserId(), user -> participantVO.setUserName(user.getNickname()));
            return participantVO;
        }));
        return success(respVO);
    }

    @PutMapping("/update-read-status")
    @Operation(summary = "标记本人已阅读日程")
    @Parameter(name = "id", description = "日程编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:schedule:query')")
    public CommonResult<Boolean> updateScheduleReadStatus(@RequestParam("id") Long id) {
        scheduleService.updateScheduleReadStatus(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得日程分页")
    @PreAuthorize("@ss.hasPermission('oa:schedule:query')")
    public CommonResult<PageResult<OaScheduleRespVO>> getSchedulePage(@Valid OaSchedulePageReqVO pageReqVO) {
        PageResult<OaScheduleDO> pageResult = scheduleService.getSchedulePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildScheduleRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得我的日程分页")
    @PreAuthorize("@ss.hasPermission('oa:schedule:query')")
    public CommonResult<PageResult<OaScheduleRespVO>> getMySchedulePage(@Valid OaSchedulePageReqVO pageReqVO) {
        PageResult<OaScheduleDO> pageResult = scheduleService.getMySchedulePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildScheduleRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/received-page")
    @Operation(summary = "获得共享给我的日程分页")
    @PreAuthorize("@ss.hasPermission('oa:schedule:query')")
    public CommonResult<PageResult<OaScheduleRespVO>> getReceivedSchedulePage(@Valid OaSchedulePageReqVO pageReqVO) {
        PageResult<OaScheduleDO> pageResult = scheduleService.getReceivedSchedulePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildScheduleRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接日程响应列表
     *
     * @param schedules 日程列表
     * @return 日程响应列表
     */
    private List<OaScheduleRespVO> buildScheduleRespVOList(List<OaScheduleDO> schedules) {
        if (CollUtil.isEmpty(schedules)) {
            return Collections.emptyList();
        }
        // 1.1 查询日程参与人
        Map<Long, List<Long>> participantUserIdListMap = scheduleService.getScheduleParticipantUserIdListMap(
                convertSet(schedules, OaScheduleDO::getId));
        // 1.2 批量查询创建人和参与人
        Collection<Long> userIds = convertSet(schedules, schedule -> NumberUtils.parseLong(schedule.getCreator()));
        participantUserIdListMap.values().forEach(userIds::addAll);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 1.3 批量查询部门
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 2. 转换日程并拼接展示字段
        return convertList(schedules, scheduleDO -> {
            OaScheduleRespVO schedule = BeanUtils.toBean(scheduleDO, OaScheduleRespVO.class);
            MapUtils.findAndThen(userMap, NumberUtils.parseLong(schedule.getCreator()), user -> {
                schedule.setCreatorName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> schedule.setCreatorDeptName(dept.getName()));
            });
            schedule.setParticipantUserIds(Collections.emptyList()).setParticipantUserNames(Collections.emptyList());
            MapUtils.findAndThen(participantUserIdListMap, schedule.getId(), participantUserIds -> {
                schedule.setParticipantUserIds(participantUserIds)
                        .setParticipantUserNames(convertList(convertList(participantUserIds, userMap::get), AdminUserRespDTO::getNickname));
            });
            return schedule;
        });
    }

}
