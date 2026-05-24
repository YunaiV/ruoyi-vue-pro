package cn.iocoder.yudao.module.im.controller.admin.manager.group;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerBanReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
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

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - IM 群聊管理")
@RestController
@RequestMapping("/im/manager/group")
@Validated
public class ImGroupManagerController {

    @Resource
    private ImGroupService groupService;
    @Resource
    private ImGroupMemberService groupMemberService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得群分页")
    @PreAuthorize("@ss.hasPermission('im:manager:group:query')")
    public CommonResult<PageResult<ImGroupManagerRespVO>> getGroupPage(@Valid ImGroupManagerPageReqVO pageReqVO) {
        // 1. 分页查询群
        PageResult<ImGroupDO> pageResult = groupService.getGroupPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 2.1 批量查询相关数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), ImGroupDO::getOwnerUserId));
        Map<Long, Long> memberCountMap = groupMemberService.getActiveMemberCountMap(
                convertSet(pageResult.getList(), ImGroupDO::getId));
        // 2.2 转换为 VO，填充群主昵称、群成员数量
        return success(BeanUtils.toBean(pageResult, ImGroupManagerRespVO.class, vo -> {
            MapUtils.findAndThen(userMap, vo.getOwnerUserId(),
                    user -> vo.setOwnerNickname(user.getNickname()));
            vo.setMemberCount(memberCountMap.getOrDefault(vo.getId(), 0L).intValue());
        }));
    }

    @GetMapping("/get")
    @Operation(summary = "获得群详情")
    @Parameter(name = "id", description = "群编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:group:query')")
    public CommonResult<ImGroupManagerRespVO> getGroup(@RequestParam("id") Long id) {
        ImGroupDO group = groupService.getGroup(id);
        return success(BeanUtils.toBean(group, ImGroupManagerRespVO.class));
    }

    @PutMapping("/ban")
    @Operation(summary = "封禁群")
    @PreAuthorize("@ss.hasPermission('im:manager:group:ban')")
    public CommonResult<Boolean> banGroup(@Valid @RequestBody ImGroupManagerBanReqVO reqVO) {
        groupService.banGroup(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/unban")
    @Operation(summary = "解封群")
    @Parameter(name = "id", description = "群编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:group:ban')")
    public CommonResult<Boolean> unbanGroup(@RequestParam("id") Long id) {
        groupService.unbanGroup(getLoginUserId(), id);
        return success(true);
    }

    @DeleteMapping("/dissolve")
    @Operation(summary = "解散群")
    @Parameter(name = "id", description = "群编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:group:dissolve')")
    public CommonResult<Boolean> dissolveGroup(@RequestParam("id") Long id) {
        groupService.dissolveGroupByManager(getLoginUserId(), id);
        return success(true);
    }

}
