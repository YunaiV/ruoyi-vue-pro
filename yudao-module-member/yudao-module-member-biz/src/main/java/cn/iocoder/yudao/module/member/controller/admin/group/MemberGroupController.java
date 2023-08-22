package cn.iocoder.yudao.module.member.controller.admin.group;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.*;
import cn.iocoder.yudao.module.member.convert.group.MemberGroupConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.group.MemberGroupDO;
import cn.iocoder.yudao.module.member.service.group.MemberGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@Tag(name = "管理后台 - 用户分组")
@RestController
@RequestMapping("/member/group")
@Validated
public class MemberGroupController {

    @Resource
    private MemberGroupService groupService;

    @PostMapping("/create")
    @Operation(summary = "创建用户分组")
    @PreAuthorize("@ss.hasPermission('member:group:create')")
    public CommonResult<Long> createGroup(@Valid @RequestBody MemberGroupCreateReqVO createReqVO) {
        return success(groupService.createGroup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户分组")
    @PreAuthorize("@ss.hasPermission('member:group:update')")
    public CommonResult<Boolean> updateGroup(@Valid @RequestBody MemberGroupUpdateReqVO updateReqVO) {
        groupService.updateGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户分组")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:group:delete')")
    public CommonResult<Boolean> deleteGroup(@RequestParam("id") Long id) {
        groupService.deleteGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户分组")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:group:query')")
    public CommonResult<MemberGroupRespVO> getGroup(@RequestParam("id") Long id) {
        MemberGroupDO group = groupService.getGroup(id);
        return success(MemberGroupConvert.INSTANCE.convert(group));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取会员分组精简信息列表", description = "只包含被开启的会员分组，主要用于前端的下拉选项")
    public CommonResult<List<MemberGroupSimpleRespVO>> getSimpleGroupList() {
        // 获用户列表，只要开启状态的
        List<MemberGroupDO> list = groupService.getEnableGroupList();
        return success(MemberGroupConvert.INSTANCE.convertSimpleList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户分组分页")
    @PreAuthorize("@ss.hasPermission('member:group:query')")
    public CommonResult<PageResult<MemberGroupRespVO>> getGroupPage(@Valid MemberGroupPageReqVO pageVO) {
        PageResult<MemberGroupDO> pageResult = groupService.getGroupPage(pageVO);
        return success(MemberGroupConvert.INSTANCE.convertPage(pageResult));
    }

}
