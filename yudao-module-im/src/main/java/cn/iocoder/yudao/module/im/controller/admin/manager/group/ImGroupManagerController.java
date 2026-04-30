package cn.iocoder.yudao.module.im.controller.admin.manager.group;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerBanReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 群聊管理")
@RestController
@RequestMapping("/im/manager/group")
@Validated
public class ImGroupManagerController {

    @Resource
    private ImGroupService groupService;

    @GetMapping("/page")
    @Operation(summary = "获得群分页")
    @PreAuthorize("@ss.hasPermission('im:manager:group:query')")
    public CommonResult<PageResult<ImGroupManagerRespVO>> getGroupPage(@Valid ImGroupManagerPageReqVO pageReqVO) {
        return success(groupService.getGroupPage(pageReqVO));
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
        groupService.banGroup(reqVO);
        return success(true);
    }

    @PutMapping("/unban")
    @Operation(summary = "解封群")
    @Parameter(name = "id", description = "群编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:group:ban')")
    public CommonResult<Boolean> unbanGroup(@RequestParam("id") Long id) {
        groupService.unbanGroup(id);
        return success(true);
    }

}
